val kamonCore     = "io.kamon"      %% "kamon-core"             % "1.1.3"
val kamonTestKit  = "io.kamon"      %% "kamon-testkit"          % "1.1.3"

lazy val root = (project in file("."))
  .settings(name := "kamon-mackerel")
  .settings(mavenCentral: _*)
  .settings(
    libraryDependencies ++=
      compileScope(kamonCore, awsCloudWatch, scalaCompact.value) ++
        testScope(scalatest, slf4jApi, slf4jnop, kamonCore, kamonTestKit)
  )

def scalaCompact = Def.setting {
  scalaBinaryVersion.value match {
    case "2.10" | "2.11" => "org.scala-lang.modules" %% "scala-java8-compat" % "0.5.0"
    case "2.12"          => "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0"
  }
}

lazy val mavenCentral = Seq(
  organization := "com.github.yoshiyoshifujii",
  sonatypeProfileName := "com.github.yoshiyoshifujii",
  publishMavenStyle := true,
  publishTo := sonatypePublishTo.value,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ =>
    false
  },
  pomExtra := {
    <url>https://github.com/yoshiyoshifujii/kamon-mackerel</url>
      <scm>
        <url>git@github.com:yoshiyoshifujii/kamon-mackerel.git</url>
        <connection>scm:git:github.com/yoshiyoshifujii/kamon-mackerel</connection>
        <developerConnection>scm:git:git@github.com:yoshiyoshifujii/kamon-mackerel.git</developerConnection>
      </scm>
      <developers>
        <developer>
          <id>yoshiyoshifujii</id>
          <name>Yoshitaka Fujii</name>
        </developer>
      </developers>
  }
)
