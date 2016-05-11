package com.looker.logDataWebinar

class LogParser {

  // specify regular expression patterns for log-line attributes
  val ip_address = "([0-9\\.]+)"
  val identifier, user_id, uri, status, size = "(\\S+)"
  val created_at = "(?:\\[)(.*)(?:\\])"
  val method = "(?:\\p{Punct})([A-Z]+)"
  val protocol = "(\\S+)(?:\\p{Punct})"
  val referer = "(?:\\p{Punct})(\\S+)(?:\\p{Punct})"
  val agent = "(?:\\p{Punct})([^\"]*)(?:\")"
  val user_meta_info = "(.*)"

  // string interpolate and create scala.util.matching.Regex (with trailing .r)
  val lineMatch = s"$ip_address\\s+$identifier\\s+$user_id\\s+$created_at\\s+$method\\s+$uri\\s+$protocol\\s+$status\\s+$size\\s+$referer\\s+$agent\\s+$user_meta_info".r

  // define function to match raw log lines and output LogLine class
  def extractValues(line: String): Option[LogLine] = {
    line match {
      case lineMatch(ip_address, identifier, user_id, created_at, method, uri, protocol, status, size, referer, agent, user_meta_info, _*) 
        => return Option(LogLine(ip_address, identifier, user_id, created_at, method, uri, protocol, status, size, referer, agent, user_meta_info))
      case _ 
        =>  None
    }
  }

}