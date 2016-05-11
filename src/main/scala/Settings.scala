package com.looker.logDataWebinar

import com.typesafe.config.Config

// define Settings class to get application.conf values
class Settings(config: Config) {
  val location    = config.getString("hdfs.location")
  val file_format = config.getString("hdfs.file_format")
  val output_type = config.getString("hdfs.output_type")
}