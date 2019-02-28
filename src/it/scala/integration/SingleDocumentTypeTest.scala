package integration

import java.io.ByteArrayInputStream

import common.DocumentType
import index.{Indexer, Parser}
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import search.Searcher

class SingleDocumentTypeTest extends FunSuite with BeforeAndAfterAll {

  object TestParser extends Parser

  object TestSearcher extends Searcher

  object TestIndexer extends Indexer

  override def beforeAll = {
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
        |    "due_at": "2016-07-31T02:37:50 -10:00",
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
        |      "Carolina North"
        |    ],
        |    "has_incidents": false,
        |    "due_at": "2016-08-15T05:37:32 -10:00",
        |    "via": "chat"
        |  },
        |  {
        |    "_id": "81bdd837-e955-4aa4-a971-ef1e3b373c6d",
        |    "url": "http://initech.zendesk.com/api/v2/tickets/81bdd837-e955-4aa4-a971-ef1e3b373c6d.json",
        |    "external_id": "dbf801cc-2d9e-403e-9210-4c870240d270",
        |    "created_at": "2016-01-13T05:42:04 -11:00",
        |    "type": "problem",
        |    "subject": "A Catastrophe in Pakistan",
        |    "description": "Velit Lorem laboris qui enim occaecat veniam. Qui quis voluptate qui incididunt commodo laborum dolor non anim consectetur incididunt id.",
        |    "priority": "normal",
        |    "status": "hold",
        |    "submitter_id": 74,
        |    "assignee_id": 40,
        |    "organization_id": 105,
        |    "tags": [
        |      "California",
        |      "Palau",
        |      "Kentucky",
        |      "North Carolina"
        |    ],
        |    "has_incidents": true,
        |    "via": "voice"
        |  }
        |]
      """.stripMargin

    val inputStream = new ByteArrayInputStream(input.getBytes())

    TestIndexer.open

    TestParser.parseStreamAndProcess(inputStream, TestIndexer.addDoc(DocumentType.TICKET))

    TestIndexer.close
  }

  test("should search for a document with a single value field") {
    assert(TestSearcher.search(Some(DocumentType.TICKET), "via", Some("web")).map(_.attributes.get("_id").get) === Seq("436bf9b0-1147-4c0a-8439-6f79833bff5b"))
  }

  test("should search for a document with multi valued field") {
    assert(TestSearcher.search(Some(DocumentType.TICKET), "tags", Some("Puerto Rico")).map(_.attributes.get("_id").get) === Seq("1a227508-9f39-427c-8f57-1b72f3fab87c"))
  }

  test("search is case insensitive for keyword fields") {
    assert(TestSearcher.search(Some(DocumentType.TICKET), "tags", Some("puerto rico")).map(_.attributes.get("_id").get) === Seq("1a227508-9f39-427c-8f57-1b72f3fab87c"))
  }

  test("should maintain token order for keyword fields") {
    assert(TestSearcher.search(Some(DocumentType.TICKET), "tags", Some("North Carolina")).map(_.attributes.get("_id").get) === Seq("81bdd837-e955-4aa4-a971-ef1e3b373c6d"))
  }

  test("should search for a document with a token for full text fields") {
    assert(TestSearcher.search(Some(DocumentType.TICKET), "subject", Some("korea")).map(_.attributes.get("_id").get) === Seq("436bf9b0-1147-4c0a-8439-6f79833bff5b"))
  }

  test("should search for document with exact match on keyword field") {
    assert(TestSearcher.search(Some(DocumentType.TICKET), "due_at", Some("2016-08-15T05:37:32 -10:00")).map(_.attributes.get("_id").get) === Seq("1a227508-9f39-427c-8f57-1b72f3fab87c"))
  }

  test("should be able to search for empty values on given field") {
    assert(TestSearcher.search(Some(DocumentType.TICKET), "due_at", None).map(_.attributes.get("_id").get) === Seq("81bdd837-e955-4aa4-a971-ef1e3b373c6d"))
  }

  test("should search case insensitive on full text field") {
    assert(TestSearcher.search(Some(DocumentType.TICKET), "subject", Some("micronesia")).map(_.attributes.get("_id").get) === Seq("1a227508-9f39-427c-8f57-1b72f3fab87c"))
  }

  test("should search for multiple terms on full text field matching all terms") {
    assert(TestSearcher.search(Some(DocumentType.TICKET), "subject", Some("Catastrophe Micronesia")).map(_.attributes.get("_id").get) === Seq("1a227508-9f39-427c-8f57-1b72f3fab87c"))
  }

  test("should ignore stop words on full text field") {
    assert(TestSearcher.search(Some(DocumentType.TICKET), "subject", Some("Catastrophe Micronesia")).map(_.attributes.get("_id").get) === Seq("1a227508-9f39-427c-8f57-1b72f3fab87c"))
  }

}