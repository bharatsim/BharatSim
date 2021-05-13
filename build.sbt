name := "engine"

version := "0.1"

scalaVersion := "2.13.3"

val AkkaVersion = "2.6.10"
libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-cluster-typed" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-serialization-jackson" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % AkkaVersion

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
libraryDependencies += "com.typesafe" % "config" % "1.4.1"
libraryDependencies += "org.neo4j.driver" % "neo4j-java-driver" % "4.1.1"
libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.3"
libraryDependencies += "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.0"
libraryDependencies += "org.apache.commons" % "commons-math3" % "3.6.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.0" % Test
libraryDependencies += "org.scalatest" %% "scalatest-funsuite" % "3.2.0" % Test
libraryDependencies += "org.mockito" %% "mockito-scala" % "1.15.0" % Test
libraryDependencies += "org.neo4j.test" % "neo4j-harness" % "4.0.0" % Test
libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.6"
libraryDependencies += "com.dimafeng" %% "testcontainers-scala-scalatest" % "0.39.3" % "test"
libraryDependencies += "com.dimafeng" %% "testcontainers-scala-neo4j" % "0.39.3" % "test"
libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-csv" % "2.0.2"

Compile / doc / scalacOptions := Seq("-skip-packages", "com.bharatsim.model")

Test / fork := true

coverageEnabled := true
coverageExcludedPackages := "com\\.bharatsim\\.model.*"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case "reference.conf"              => MergeStrategy.concat
  case x                             => MergeStrategy.first
}
