# Spark Log Parser

## Overview
This is a sample Spark Streaming application written in Scala, the purpose of which is to take a stream of logs from Flume, parse the raw logs, create a Spark dataframe, and write the data to Parquet in HDFS.

## Walkthrough
The following walkthrough is meant to get the user up and running with an example in localmode; however, there are a few minor changes—particularly with the Flume set up—that allows this to run over a network and on an entire Spark cluster.

### Preliminaries
- Download the latest release of [Flume](http://www.apache.org/dyn/closer.lua/flume/1.6.0/apache-flume-1.6.0-bin.tar.gz).
- Download jar for [spark-streaming-flume-sink](http://search.maven.org/remotecontent?filepath=org/apache/spark/spark-streaming-flume-sink_2.10/1.6.1/spark-streaming-flume-sink_2.10-1.6.1.jar).
- Download the [scala-lang](http://search.maven.org/remotecontent?filepath=org/scala-lang/scala-library/2.10.5/scala-library-2.10.5.jar) jar.
- Download the [common-lang](http://search.maven.org/remotecontent?filepath=org/apache/commons/commons-lang3/3.3.2/commons-lang3-3.3.2.jar) jar.

### Flume Setup
Because we're going to create a custom Flume configuration for Spark Streaming, we need to make sure the necessary jars are in the classpath. Flume has a convenient way of doing this using the `plugins.d` directory structure.
- Create the following directory setup within your Flume location, add the jars from above:
```shell
apache-flume-1.6.0-bin/
  plugins.d/
    spark/
      lib/
        libext/
        commons-lang3-3.3.2.jar
        scala-library-2.10.5.jar
        spark-assembly-1.5.2-hadoop2.6.0-amzn-2.jar
      spark-streaming-flume-assembly_2.10-1.6.1.jar
      spark-streaming-flume-sink_2.10-1.6.1.jar
```
- Configure the Flume agent (`conf/logdata.conf`):
```YAML
# name the components of agent
agent.sources = terminal
agent.sinks = logger spark
agent.channels = memory1 memory2

# describe source
agent.sources.terminal.type = exec
agent.sources.terminal.command = tail -f /home/hadoop/generator/logs/access.log

# describe logger sink (in production, pipe raw logs to HDFS)
agent.sinks.logger.type = logger

# describe spark sink
agent.sinks.spark.type = org.apache.spark.streaming.flume.sink.SparkSink
agent.sinks.spark.hostname = localhost
agent.sinks.spark.port = 9988
agent.sinks.spark.channel = memory1

# channel buffers events in memory (used with logger sink)
agent.channels.memory1.type = memory
agent.channels.memory1.capacity = 10000
agent.channels.memory1.transactionCapacity = 1000

# channel buffers events in memory (used with spark sink)
agent.channels.memory2.type = memory
agent.channels.memory2.capacity = 10000
agent.channels.memory2.transactionCapacity = 1000

# tie source and sinks with respective channels
agent.sources.terminal.channels = memory1 memory2
agent.sinks.logger.channel = memory1
agent.sinks.spark.channel = memory2
```
- Start Flume agent: `./bin/flume-ng agent --conf conf --conf-file conf/logdata.conf --name agent -Dflume.root.logger=INFO,console`

### Spark Application
- Clone the repo: `git@github.com:looker/spark_log_data.git`
- Open `/src/main/resources/application.conf` and set your HDFS output location.
- Compile into uber jar: `sbt assembly`
- Submit application to Spark: `./bin/spark-submit --master local[2] --class logDataWebinar /spark_log_data/target/scala-2.10/Log\ Data\ Webinar-assembly-1.0.jar localhost 9988 60`

### Hive
We're going to use the Hive Metastore to interface with our Parquet files by creating an [external table](https://cwiki.apache.org/confluence/display/Hive/LanguageManual+DDL#LanguageManualDDL-ExternalTables).
- Fire up Hive command-line client: `hive`
- Create database: `create database if not exists logdata;`
- Create table:
```sql
drop table if exists logdata.event;

create external table logdata.event (
    ip_address string
    , identifier string
    , user_id string
    , created_at timestamp
    , method string
    , uri string
    , protocol string
    , status string
    , size string
    , referer string
    , agent string
    , user_meta_info string)
stored as parquet
location 'hdfs://YOUR-HDFS-ENDPOINT:PORT/YOUR/PATH/loglines.parquet';
```

### Thrift Server and Beeline
- Start Thrift Server: `sudo -u spark HADOOP_USER_NAME=hadoop HIVE_SERVER2_THRIFT_PORT=10001 /usr/lib/spark/sbin/start-thriftserver.sh`
- Use Beeline to interface with external tables: `./bin/beeline --color=yes -u 'jdbc:hive2://localhost:10001/logdata' -n hadoop`
- Issue SQL: `select count(*) from logdata.event;`
- Stop Thrift Server: `sudo -u spark /usr/lib/spark/sbin/stop-thriftserver.sh`