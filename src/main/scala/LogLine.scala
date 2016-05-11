package com.looker.logDataWebinar

// create LogLine class with boilerplate getters and setters
case class LogLine (
  ip_address: String,
  identifier: String,
  user_id: String,
  created_at: String,
  method: String,
  uri: String,
  protocol: String,
  status: String,
  size: String,
  referer: String,
  agent: String,
  user_meta_info: String
)