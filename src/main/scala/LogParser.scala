package com.looker.logDataWebinar

/* 
 * The LogParser class defines a series of patterns that match a raw log line and output an instance of LogLine class.
 * Example log line:
 * 2.174.143.4 - - [09/May/2016:05:56:03 +0000]  "GET  /department HTTP/1.1" 200 1226  "-" "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; GTB6.3; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; InfoPath.2)" "USER=0;NUM=9"
 */
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