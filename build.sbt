lazy val root = (project in file(".")).
	settings(
		name := "Log Data Webinar",
		version := "1.0",
		scalaVersion := "2.10.4",
  	mainClass in Compile := Some("logDataWebinar")        
  )

libraryDependencies ++= Seq(
	"org.apache.spark" %% "spark-core" % "1.4.0" % "provided",
	"org.apache.spark" %% "spark-sql" % "1.4.0" % "provided",
	"org.apache.spark" % "spark-streaming_2.10" % "1.6.1" % "provided",
	"org.apache.spark" %% "spark-streaming-flume" % "1.6.1",
	"com.typesafe" % "config" % "1.2.1"
)

// META-INF discarding
mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
   {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case x => MergeStrategy.first
   }
}