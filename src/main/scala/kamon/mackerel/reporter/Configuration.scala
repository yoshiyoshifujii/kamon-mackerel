package kamon.mackerel.reporter

import java.time.Duration

import com.typesafe.config.Config

private[mackerel] case class Configuration(apiKey: String,
                         private val isKCPS: Boolean,
                         connectTimeout: Duration,
                         readTimeout: Duration,
                         writeTimeout: Duration,
                         hostId: String) {

  val apiUrl: String = if (isKCPS) "https://kcps-mackerel.io" else "https://api.mackerelio.com"
}

private[mackerel] object Configuration {

  def readConfiguration(config: Config): Configuration = {
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
