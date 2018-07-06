val kamonVersion = "1.1.3"
val circeVersion = "0.9.3"

val kamonCore       = "io.kamon"             %% "kamon-core"    % kamonVersion
val kamonTestKit    = "io.kamon"             %% "kamon-testkit" % kamonVersion
val asyncHttpClient = "com.squareup.okhttp3" % "okhttp"         % "3.10.0"
val circeCore       = "io.circe"             %% "circe-core"    % circeVersion
val circeGeneric    = "io.circe"             %% "circe-generic" % circeVersion
val circeParser     = "io.circe"             %% "circe-parser"  % circeVersion
val scalacheck      = "org.scalacheck"       %% "scalacheck"    % "1.13.4"

lazy val root = (project in file("."))
  .settings(name := "kamon-mackerel")
  .settings(mavenCentral: _*)
  .settings(
    libraryDependencies ++=
      compileScope(kamonCore, asyncHttpClient, circeCore, circeGeneric, circeParser, scalaCompact.value) ++
      testScope(scalatest, scalacheck, slf4jApi, slf4jnop, kamonCore, kamonTestKit)
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
