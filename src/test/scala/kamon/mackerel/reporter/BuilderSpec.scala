package kamon.mackerel.reporter

import org.scalatest.prop.PropertyChecks
import org.scalatest.{Matchers, WordSpec}

class BuilderSpec extends WordSpec with Matchers with PropertyChecks {

  "BuilderSpec" should {

    "mkTags" in {
      forAll(
        (key: String, value: String) => {
          Builder.mkTags(Map(key -> value)) shouldBe s"${key}_$value"
        }
      )

    }

  }

}
