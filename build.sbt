scalaVersion := "2.12.8"


lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "it,test",
    libraryDependencies += "org.apache.lucene" % "lucene-core" % "7.7.0",
    libraryDependencies += "org.apache.lucene" % "lucene-queryparser" % "7.7.0",
    libraryDependencies += "org.apache.lucene" % "lucene-analyzers-common" % "7.7.0",
    libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.6.5",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test",
    libraryDependencies += "com.typesafe" % "config" % "1.3.3",
    libraryDependencies += "com.concurrentthought.cla" %% "command-line-arguments" % "0.5.0",
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3",
    //same directory used for indexes, hence multiple parallel integration won't work.
    //We could always use a different index dir per test if we really need to do things in parallel
    parallelExecution in IntegrationTest := false,
    javaOptions in IntegrationTest ++= Seq("-Dconfig.resource=application-it.conf"),
    fork in IntegrationTest := true,
    assemblyJarName in assembly := "simple-search-fat.jar",
    assemblyOutputPath in assembly := baseDirectory.value / "simple-search-fat.jar"

  )
