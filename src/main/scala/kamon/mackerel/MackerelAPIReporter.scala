package kamon.mackerel

import java.time.Duration
import java.util.concurrent.TimeUnit

import com.typesafe.config.Config
import io.circe.syntax._
import kamon.metric.{ MetricDistribution, MetricValue, PeriodSnapshot }
import kamon.{ Kamon, MetricReporter, Tags }
import okhttp3.{ MediaType, OkHttpClient, Request, RequestBody }
import org.slf4j.LoggerFactory

class MackerelAPIReporter extends MetricReporter {
  import MackerelAPIReporter._

  private val logger   = LoggerFactory.getLogger(classOf[MackerelAPIReporter])
  private val jsonType = MediaType.parse("application/json; charset=utf-8")

  private var configuration            = readConfiguration(Kamon.config())
  private var httpClient: OkHttpClient = createHttpClient(configuration)

  override def start(): Unit = {
    logger.info("Started the Mackerel API reporter.")
  }

  override def stop(): Unit = {
    logger.info("Stopped the Mackerel API reporter.")
  }

  override def reconfigure(config: Config): Unit = {
    val newConfiguration = readConfiguration(config)
    httpClient = createHttpClient(readConfiguration(Kamon.config()))
    configuration = newConfiguration
  }

  override def reportPeriodSnapshot(snapshot: PeriodSnapshot): Unit =
    buildRequestBody(snapshot).grouped(10).foreach(send)

  private[mackerel] def send(posts: Seq[PostTSDBMetricValue]): Unit = {
    val body = RequestBody.create(jsonType, posts.asJson.noSpaces)
    val request = new Request.Builder()
      .url(postTSDBUrl(configuration))
      .header(headerApiKey, configuration.apiKey)
      .post(body)
      .build
    val response = httpClient.newCall(request).execute()

    if (!response.isSuccessful)
      logger.error(
        s"Failed to POST metrics to Mackerel with status code [${response.code()}], Body: [${response.body().string()}]"
      )

    response.close()
  }

  private[mackerel] def buildRequestBody(snapshot: PeriodSnapshot): Seq[PostTSDBMetricValue] = {
    val hostId    = configuration.hostId
    val timestamp = snapshot.from.getEpochSecond

    def mkTags(tags: Tags): String =
      tags.toStream.sortBy(_._1).map(t => s"${t._1}_${t._2}").mkString(".")

    def convertToValue(metricValue: MetricValue): PostTSDBMetricValue =
      PostTSDBMetricValue(
        hostId = hostId,
        name = s"custom.${metricValue.name}.${mkTags(metricValue.tags)}",
        time = timestamp,
        value = metricValue.value
      )

    def convertToDistribution(metricDistribution: MetricDistribution): Seq[PostTSDBMetricValue] = {

      def add(suffix: String, value: Long): PostTSDBMetricValue =
        PostTSDBMetricValue(
          hostId = hostId,
          name = s"custom.${metricDistribution.name}.$suffix.${mkTags(metricDistribution.tags)}",
          time = timestamp,
          value = value
        )

      if (metricDistribution.distribution.count > 0)
        Seq(
          add("count", metricDistribution.distribution.count),
          add("median", metricDistribution.distribution.percentile(50D).value),
          add("95percentile", metricDistribution.distribution.percentile(95D).value),
          add("max", metricDistribution.distribution.max),
          add("min", metricDistribution.distribution.min),
          add("sum", metricDistribution.distribution.sum)
        )
      else Seq.empty
    }

    snapshot.metrics.counters.map(convertToValue) ++
    snapshot.metrics.gauges.map(convertToValue) ++
    snapshot.metrics.histograms.flatMap(convertToDistribution) ++
    snapshot.metrics.rangeSamplers.flatMap(convertToDistribution)
  }

  private def createHttpClient(config: Configuration): OkHttpClient =
    new OkHttpClient.Builder()
      .connectTimeout(config.connectTimeout.toMillis, TimeUnit.MILLISECONDS)
      .readTimeout(config.readTimeout.toMillis, TimeUnit.MILLISECONDS)
      .writeTimeout(config.writeTimeout.toMillis, TimeUnit.MILLISECONDS)
      .build()

  private def readConfiguration(config: Config): Configuration = {
    val mackerelConfig = config.getConfig("kamon.mackerel")

    Configuration(
      apiKey = mackerelConfig.getString("http.api-key"),
      isKCPS = mackerelConfig.getBoolean("http.is-KCPS"),
      connectTimeout = mackerelConfig.getDuration("http.connect-timeout"),
      readTimeout = mackerelConfig.getDuration("http.read-timeout"),
      writeTimeout = mackerelConfig.getDuration("http.write-timeout"),
      hostId = mackerelConfig.getString("host.id")
    )
  }

}

private object MackerelAPIReporter {
  import io.circe._
  import io.circe.generic.semiauto._

  val headerApiKey = "X-Api-Key"
  val postTSDB     = "/api/v0/tsdb"

  case class Configuration(apiKey: String,
                           private val isKCPS: Boolean,
                           connectTimeout: Duration,
                           readTimeout: Duration,
                           writeTimeout: Duration,
                           hostId: String) {

    val apiUrl: String = if (isKCPS) "https://kcps-mackerel.io" else "https://api.mackerelio.com"
  }

  case class PostTSDBMetricValue(hostId: String, name: String, time: Long, value: Long)
  implicit val decodePostTSDBMetricValue: Decoder[PostTSDBMetricValue] = deriveDecoder[PostTSDBMetricValue]
  implicit val encodePostTSDBMetricValue: Encoder[PostTSDBMetricValue] = deriveEncoder[PostTSDBMetricValue]

  def postTSDBUrl(configuration: Configuration): String =
    s"${configuration.apiUrl}$postTSDB"
}
