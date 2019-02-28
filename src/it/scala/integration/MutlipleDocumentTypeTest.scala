package integration

import java.io.ByteArrayInputStream

import common._
import index.{Indexer, Parser}
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import search.Searcher

class MutlipleDocumentTypeTest extends FunSuite with BeforeAndAfterAll {

  object TestParser extends Parser

  object TestSearcher extends Searcher

  object TestIndexer extends Indexer

  override def beforeAll = {
    val ticketsJson =
      s"""
         |[
         |  {
         |    "_id": "436bf9b0-1147-4c0a-8439-6f79833bff5b",
         |    "type": "incident",
         |    "subject": "A Catastrophe in Korea (North)",
         |    "description": "Nostrud ad sit velit cupidatat laboris ipsum nisi amet laboris ex exercitation amet et proident. Ipsum fugiat aute dolore tempor nostrud velit ipsum.",
         |    "submitter_id": 1,
         |    "assignee_id": 2,
         |    "organization_id": 1
         |  },
         |  {
         |    "_id": "1a227508-9f39-427c-8f57-1b72f3fab87c",
         |    "type": "incident",
         |    "subject": "A Catastrophe in Micronesia",
         |    "description": "Aliquip excepteur fugiat ex minim ea aute eu labore. Sunt eiusmod esse eu non commodo est veniam consequat.",
         |    "assignee_id": 1
         |  },
         |  {
         |    "_id": "2217c7dc-7371-4401-8738-0a8a8aedc08d",
         |    "type": "incident",
         |    "subject": "A Catastrophe in Hungary",
         |    "description": "Aliquip excepteur fugiat ex minim ea aute eu labore. Sunt eiusmod esse eu non commodo est veniam consequat.",
         |    "assignee_id": 1
         |  }
         |]
      """.stripMargin

    val usersJson =
      s"""
         |[
         |  {
         |    "_id": 1,
         |    "name": "Francisca Rasmussen",
         |    "alias": "Miss Coffey",
         |    "email": "coffeyrasmussen@flotonic.com",
         |    "signature": "Don't Worry Be Happy!",
         |    "organization_id": 2,
         |    "role": "admin"
         |  },
         |  {
         |    "_id": 2,
         |    "name": "Cross Barlow",
         |    "alias": "Miss Joni",
         |    "email": "jonibarlow@flotonic.com",
         |    "signature": "Don't Worry Be Happy!",
         |    "organization_id": 1,
         |    "role": "admin"
         |  }
         |]
       """.stripMargin

    val organizationsJson =
      s"""
         |[
         |  {
         |    "_id": 1,
         |    "name": "Enthaze",
         |    "details": "MegaCorp"
         |  },
         |  {
         |    "_id": 2,
         |    "name": "Nutralab",
         |    "details": "Non profit"
         |  }
         |]
       """.stripMargin


    TestIndexer.open

    TestParser.parseStreamAndProcess(new ByteArrayInputStream(ticketsJson.getBytes()), TestIndexer.addDoc(DocumentType.TICKET))
    TestParser.parseStreamAndProcess(new ByteArrayInputStream(usersJson.getBytes()), TestIndexer.addDoc(DocumentType.USER))
    TestParser.parseStreamAndProcess(new ByteArrayInputStream(organizationsJson.getBytes()), TestIndexer.addDoc(DocumentType.ORGANIZATION))

    TestIndexer.close
  }

  test("can search for a ticket and retrieve associated entities") {
    val results = TestSearcher.search(Some(DocumentType.TICKET), "subject", Some("North Korea"))
    assert(results.flatMap(_.attributes.get(SearchConfig.fields.id)) === Seq("436bf9b0-1147-4c0a-8439-6f79833bff5b"))
    val ticket = results.head.asInstanceOf[Ticket]
    assert(ticket.organization.flatMap(_.get("name")).get === "Enthaze")
    assert(ticket.submitter.flatMap(_.get("name")).get === "Francisca Rasmussen")
    assert(ticket.assignee.flatMap(_.get("name")).get === "Cross Barlow")
  }

  test("can search for an user and retrieve associated entities") {
    val results = TestSearcher.search(Some(DocumentType.USER), "name", Some("Francisca"))
    assert(results.flatMap(_.attributes.get(SearchConfig.fields.id)) === Seq(1))
    val user = results.head.asInstanceOf[User]
    assert(user.organization.flatMap(_.get("name")).get === "Nutralab")
    assert(user.submitted_tickets.flatMap(_.get(SearchConfig.fields.id)) === Seq("436bf9b0-1147-4c0a-8439-6f79833bff5b"))
    assert(user.assigned_tickets.flatMap(_.get(SearchConfig.fields.id)) === Seq("1a227508-9f39-427c-8f57-1b72f3fab87c", "2217c7dc-7371-4401-8738-0a8a8aedc08d"))
  }

  test("can search for an organization and retrieve associated users and tickets") {
    val results = TestSearcher.search(Some(DocumentType.ORGANIZATION), "name", Some("Enthaze"))
    assert(results.flatMap(_.attributes.get(SearchConfig.fields.id)) === Seq(1))
    val organization = results.head.asInstanceOf[Organization]
    assert(organization.users.flatMap(_.get(SearchConfig.fields.id)) === Seq(2))
    assert(organization.tickets.flatMap(_.get(SearchConfig.fields.id)) === Seq("436bf9b0-1147-4c0a-8439-6f79833bff5b"))
  }
}
