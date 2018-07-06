package kamon.mackerel

import com.typesafe.config.Config
import kamon.mackerel.reporter.{ Builder, Configuration, MackerelHttpClient }
import kamon.metric.PeriodSnapshot
import kamon.{ Kamon, MetricReporter }
import org.slf4j.{ Logger, LoggerFactory }

class MackerelAPIReporter extends MetricReporter {
  import Configuration._

  private implicit val logger: Logger                 = LoggerFactory.getLogger(classOf[MackerelAPIReporter])
  private implicit var configuration: Configuration   = readConfiguration(Kamon.config())
  private implicit var httpClient: MackerelHttpClient = MackerelHttpClient(configuration)

  override def start(): Unit = {
    logger.info("Started the Mackerel API reporter.")
  }

  override def stop(): Unit = {
    logger.info("Stopped the Mackerel API reporter.")
  }

  override def reconfigure(config: Config): Unit = {
    configuration = readConfiguration(config)
    httpClient = MackerelHttpClient(configuration)
  }

  override def reportPeriodSnapshot(snapshot: PeriodSnapshot): Unit =
    Builder(snapshot).buildRequestBody
      .grouped(10)
      .foreach(httpClient.send)

}
