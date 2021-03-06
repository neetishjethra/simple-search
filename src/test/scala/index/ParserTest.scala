package index

import java.io.ByteArrayInputStream

import org.scalatest.FunSuite

class ParserTest extends FunSuite {

  object TestParser extends Parser

  test("can read and parse json input") {

    val input =
      """
        |[
        |  {
        |    "_id": "436bf9b0-1147-4c0a-8439-6f79833bff5b",
        |    "url": "http://initech.zendesk.com/api/v2/tickets/436bf9b0-1147-4c0a-8439-6f79833bff5b.json",
        |    "external_id": "9210cdc9-4bee-485f-a078-35396cd74063",
        |    "created_at": "2016-04-28T11:19:34 -10:00",
        |    "type": "incident",
        |    "subject": "A Catastrophe in Korea (North)",
        |    "description": "Nostrud ad sit velit cupidatat laboris ipsum nisi amet laboris ex exercitation amet et proident. Ipsum fugiat aute dolore tempor nostrud velit ipsum.",
        |    "priority": "high",
        |    "status": "pending",
        |    "submitter_id": 38,
        |    "assignee_id": 24,
        |    "organization_id": 116,
        |    "tags": [
        |      "Ohio",
        |      "Pennsylvania",
        |      "American Samoa",
        |      "Northern Mariana Islands"
        |    ],
        |    "has_incidents": false,
        |    "via": "web"
        |  },
        |  {
        |    "_id": "1a227508-9f39-427c-8f57-1b72f3fab87c",
        |    "url": "http://initech.zendesk.com/api/v2/tickets/1a227508-9f39-427c-8f57-1b72f3fab87c.json",
        |    "external_id": "3e5ca820-cd1f-4a02-a18f-11b18e7bb49a",
        |    "created_at": "2016-04-14T08:32:31 -10:00",
        |    "type": "incident",
        |    "subject": "A Catastrophe in Micronesia",
        |    "description": "Aliquip excepteur fugiat ex minim ea aute eu labore. Sunt eiusmod esse eu non commodo est veniam consequat.",
        |    "priority": "low",
        |    "status": "hold",
        |    "submitter_id": 71,
        |    "assignee_id": 38,
        |    "organization_id": 112,
        |    "tags": [
        |      "Puerto Rico",
        |      "Idaho",
        |      "Oklahoma",
        |      "Louisiana"
        |    ],
        |    "has_incidents": false,
        |    "due_at": "2016-08-15T05:37:32 -10:00",
        |    "via": "chat"
        |  }
        |]
      """.stripMargin

    val inputStream = new ByteArrayInputStream(input.getBytes())

    val buf = scala.collection.mutable.ArrayBuffer.empty[Map[String, Any]]

    val addToBuf: (Map[String, Any] => Unit) = { toAdd => buf += toAdd }

    val fields = TestParser.parseStreamAndProcess(inputStream, addToBuf)

    assert(buf.size === 2)
    assert(buf(0).get("_id").get === "436bf9b0-1147-4c0a-8439-6f79833bff5b")
    assert(buf(1).get("tags").get === Seq("Puerto Rico", "Idaho", "Oklahoma", "Louisiana"))

    assert(fields === Set("has_incidents", "assignee_id", "priority", "subject", "url", "description", "_id", "tags", "due_at", "status", "via", "organization_id", "external_id", "created_at", "type", "submitter_id"))
  }

}