import AssemblyKeys._

assemblySettings

name := "wiki2vec"

version := "1.0"

scalaVersion := "2.10.3"

resolvers ++= Seq(
  "opennlp sourceforge repo" at "http://opennlp.sourceforge.net/maven2"
)

libraryDependencies += "com.google.guava" % "guava" % "16.0.1"

libraryDependencies += "xerces" % "xercesImpl" % "2.11.0"

libraryDependencies += "com.github.jponge" % "lzma-java" % "1.3"

libraryDependencies += "org.apache.commons" % "commons-compress" % "1.5"

libraryDependencies += "commons-compress" % "commons-compress" % "20050911"

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.2.0" % "provided"

libraryDependencies += "com.bizo" % "mighty-csv_2.10" % "0.2"

libraryDependencies += "net.debasishg" %% "redisclient" % "2.13"

libraryDependencies += "org.scalanlp" %% "chalk" % "1.3.2"  exclude ("com.typesafe.sbt", "sbt-pgp")

libraryDependencies += "org.apache.opennlp" % "opennlp-tools" % "1.5.2-incubating"

libraryDependencies += "org.apache.lucene" % "lucene-analyzers-smartcn" % "5.4.1"





