Mackerel Integration
=============================

[![Build Status](https://travis-ci.org/yoshiyoshifujii/kamon-mackerel.svg?branch=master)](https://travis-ci.org/yoshiyoshifujii/kamon-mackerel)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.yoshiyoshifujii/kamon-mackerel_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.yoshiyoshifujii/kamon-mackerel_2.12)

Reporting Metrics to Mackerel
======================================

[Mackerel](https://mackerel.io/) is an online visualization and monitoring service for servers.

### Getting Started

Supported releases and dependencies are shown below.

| kamon-mackerel    | status | jdk  | scala            |
|:-----------------:|:------:|:----:|------------------|
|  0.1.0            | stable | 1.8+ | 2.12             |

To get started with SBT, simply add the following to your `build.sbt` file:

```scala
libraryDependencies += "com.github.yoshiyoshifujii" %% "kamon-mackerel" % "0.1.0"
```

And add the API reporter to Kamon:

```scala
Kamon.addReporter(new MackerelAPIReporter())
```

Configuration
-------------

```application.conf
kamon {
  mackerel {

    #
    # Settings relevant to the MackerelAPIReporter
    #
    http {

      # Mackerel API key to use to send metrics to mackerel directly over HTTPS.
      api-key = ""
    }

    host {
      id = ""
    }

  }
}
```

