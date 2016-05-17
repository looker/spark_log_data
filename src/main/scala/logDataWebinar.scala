package com.looker.logDataWebinar

import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SQLContext
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.streaming.flume.FlumeUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}


object logDataWebinar {  

  // define main for streaming parser
  def main(argv: Array[String]): Unit = {

    // check for correct number of command-line arguments
    if(argv.length != 3){
      println("Please provide 3 parameters: <host> <port> <batch_duration>")
      System.exit(1)
    }

    // capture command-line arguments
    val host = argv(0)
    val port = argv(1).toInt
    val batchDuration = argv(2).toInt

    // configure spark application
    val conf = new SparkConf().setAppName("Log Data Webinar")
    conf.set("spark.driver.allowMultipleContexts", "true")
    val config = new Settings(ConfigFactory.load())

    // load application.conf variables for hdfs parameters
    val location = config.location
    val file_format = config.file_format
    val output_type = config.output_type

    // fire up spark context
    val sc = new SparkContext(conf)

    // fire up spark streaming context
    val ssc = new StreamingContext(conf, Seconds(batchDuration))

    // fire up spark sql context and import implicits (for toDF())
    val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    import sqlContext.implicits._

    // create polling stream from Flume
    val stream = FlumeUtils.createPollingStream(ssc, host, port)

    // print to screen the number of events received from Flume
    stream.count().map(cnt => "Received " + cnt + " events from Flume").print()

    // traverse DStream and extract message body from avro-serialized event
    val mapStream = stream.map(event => new String(event.event.getBody().array(), "UTF-8"))

    // fire up instance of LogParser
    val parser = new LogParser()

    // traverse each rdd in mapStream, apply extractValues against raw log line, write to hdfs as parquet
    mapStream.foreachRDD{
      rdd => rdd.map{
        line => parser.extractValues(line).get
      }.toDF().coalesce(1).write.format(file_format).mode(output_type).save(location)
    }

    ssc.start()
    ssc.awaitTermination()

  }
}