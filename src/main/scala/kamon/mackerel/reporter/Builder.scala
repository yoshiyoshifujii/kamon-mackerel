package kamon.mackerel.reporter

import kamon.Tags
import kamon.metric.{MetricDistribution, MetricValue, PeriodSnapshot}

private[mackerel] case class Builder(snapshot: PeriodSnapshot)(implicit configuration: Configuration) {
  import Builder._

  private val hostId    = configuration.hostId
  private val timestamp = snapshot.from.getEpochSecond

  private def convertToValue(metricValue: MetricValue): PostTSDBMetricValue =
    PostTSDBMetricValue(
      hostId = hostId,
      name = s"custom.${metricValue.name}.${mkTags(metricValue.tags)}",
      time = timestamp,
      value = metricValue.value
    )

  private def convertToDistribution(metricDistribution: MetricDistribution): Seq[PostTSDBMetricValue] = {

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

  def buildRequestBody: Seq[PostTSDBMetricValue] =
    snapshot.metrics.counters.map(convertToValue) ++
    snapshot.metrics.gauges.map(convertToValue) ++
    snapshot.metrics.histograms.flatMap(convertToDistribution) ++
    snapshot.metrics.rangeSamplers.flatMap(convertToDistribution)

}

private[mackerel] object Builder {

  private[reporter] def mkTags(tags: Tags): String =
    tags.toStream.sortBy(_._1).map(t => s"${t._1}_${t._2}").mkString(".")

}
