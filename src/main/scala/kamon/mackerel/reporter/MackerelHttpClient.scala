package kamon.mackerel.reporter

import java.util.concurrent.TimeUnit

import okhttp3.{MediaType, OkHttpClient, Request, RequestBody}
import org.slf4j.Logger

private[mackerel] case class MackerelHttpClient(configuration: Configuration)(implicit logger: Logger) {
  import PostTSDBMetricValue._
  import io.circe.syntax._

  private val headerApiKey = "X-Api-Key"
  private val postTSDB     = "/api/v0/tsdb"

  private val httpClient: OkHttpClient =
    new OkHttpClient.Builder()
      .connectTimeout(configuration.connectTimeout.toMillis, TimeUnit.MILLISECONDS)
      .readTimeout(configuration.readTimeout.toMillis, TimeUnit.MILLISECONDS)
      .writeTimeout(configuration.writeTimeout.toMillis, TimeUnit.MILLISECONDS)
      .build()

  def send(posts: Seq[PostTSDBMetricValue]): Unit = {
    val url      = s"${configuration.apiUrl}$postTSDB"
    val jsonType = MediaType.parse("application/json; charset=utf-8")
    val body     = RequestBody.create(jsonType, posts.asJson.noSpaces)

    val request = new Request.Builder()
      .url(url)
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

}
