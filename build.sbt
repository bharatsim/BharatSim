name := "engine"

version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
libraryDependencies += "org.neo4j.driver" % "neo4j-java-driver" % "4.1.1"
libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.3"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.0" % Test
libraryDependencies += "org.scalatest" %% "scalatest-funsuite" % "3.2.0" % Test
libraryDependencies += "org.mockito" %% "mockito-scala" % "1.15.0" % Test
libraryDependencies += "org.neo4j.test" % "neo4j-harness" % "4.0.0" % Test
libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.6"

Compile / doc / scalacOptions := Seq("-skip-packages", "com.bharatsim.model")
