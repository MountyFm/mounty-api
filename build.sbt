name := "mounty-api"

version := "0.1"

scalaVersion := "2.12.12"

credentials += Credentials("Artifactory Realm", "mounty.jfrog.io", "sansyzbayevdaniyar3@gmail.com", "AKCp8k8iXkJUazq2J2CAa5uT4XvrDwf9Y9uzWsLuGcoq5C1pYix9DaP2CGsAUjgvH4mReFuoJ")

resolvers +=
  "Artifactory" at "https://mounty.jfrog.io/artifactory/mounty-domain-sbt-release-local"

resolvers += Resolver.bintrayRepo("akka", "snapshots")

libraryDependencies ++= Seq(
  "joda-time"  % "joda-time"  % "2.10.13",
  "kz.mounty" %% "mounty-domain" % "0.1.1-SNAPSHOT",
  "org.mongodb.scala" %% "mongo-scala-driver" % "4.4.0",
  "org.slf4j" % "slf4j-api" % "1.7.32",
  "org.slf4j" % "slf4j-simple" % "1.7.32",
  "com.typesafe.akka" %% "akka-http" % "10.2.6",
  "com.typesafe.akka" %% "akka-stream" % "2.6.17",
  "com.typesafe.akka" %% "akka-actor" % "2.6.17",
  "com.typesafe.akka" %% "akka-slf4j" % "2.6.17",
  "org.json4s" %% "json4s-jackson" % "4.0.3",
  "org.json4s" %% "json4s-native" % "4.0.3",
  "com.rabbitmq" % "amqp-client" % "5.14.0",
  "de.heikoseeberger" %% "akka-http-json4s" % "1.38.2",

)