package kamon.mackerel.reporter

private[mackerel] case class PostTSDBMetricValue(hostId: String, name: String, time: Long, value: Long)

private[mackerel] object PostTSDBMetricValue {
  import io.circe._
  import io.circe.generic.semiauto._

  implicit val decodePostTSDBMetricValue: Decoder[PostTSDBMetricValue] = deriveDecoder[PostTSDBMetricValue]
  implicit val encodePostTSDBMetricValue: Encoder[PostTSDBMetricValue] = deriveEncoder[PostTSDBMetricValue]
}
