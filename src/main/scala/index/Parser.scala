package index

import java.io.InputStream

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken._
import com.fasterxml.jackson.databind.MappingJsonFactory
import common.JsonSupport
import org.json4s._
import org.json4s.jackson.JsonMethods._

trait Parser extends JsonSupport {

  def parseStreamAndProcess(inputStream: InputStream, processFunc: (Map[String, Any] => _)) = {

    val jp: JsonParser = (new MappingJsonFactory).createParser(inputStream)

    jp.nextToken match {
      case START_ARRAY =>
        while (jp.nextToken != END_ARRAY) {
          val valueMap: Map[String, Any] = fromJsonNode(jp.readValueAsTree()).extract[Map[String, Any]]
          processFunc(valueMap)
        }
      case _ => throw new IllegalArgumentException("Cannot parse JSON - Not a valid JSON List")
    }
  }
}


