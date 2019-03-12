import java.nio.file.{Files, Path, Paths}

import common._
import index.{Indexer, Parser}
import search.Searcher
import scala.io.StdIn._

import scala.util.Try

object Main extends App with Logging with OutputFormatter {

  object Parser extends Parser

  object Searcher extends Searcher

  object Indexer extends Indexer

  val jsonPath: Path = Paths.get(Try {
    args(0)
  }.getOrElse("src/main/resources"))

  log.debug("started indexing")

  Indexer.open

  val ticketFields = Parser.parseStreamAndProcess(Files.newInputStream(jsonPath.resolve("tickets.json")), Indexer.addDoc(DocumentType.TICKET))
  log.debug("finished indexing tickets")
  val userFields = Parser.parseStreamAndProcess(Files.newInputStream(jsonPath.resolve("users.json")), Indexer.addDoc(DocumentType.USER))
  log.debug("finished indexing users")
  val organizationFields = Parser.parseStreamAndProcess(Files.newInputStream(jsonPath.resolve("organizations.json")), Indexer.addDoc(DocumentType.ORGANIZATION))
  log.debug("finished indexing organizations")

  Indexer.close

  log.debug("finished indexing")

  val fieldNames: Map[String, Seq[String]] = Map(
    DocumentType.USER.toString -> userFields.toList.sorted,
    DocumentType.TICKET.toString -> ticketFields.toList.sorted,
    DocumentType.ORGANIZATION.toString -> organizationFields.toList.sorted
  )

  do {

    println()
    DocumentType.values.foreach(enumVal => println(s"${enumVal.id} : ${enumVal.toString}"))
    print("Enter entity type to search from options above (or leave blank and hit ENTER to search across entities) : ")
    val documentType = Try {
      DocumentType(readInt())
    }.toOption

    println()
    println(prettyOutputLines(fieldNames).mkString(newLine))
    print(s"Enter field name to search from available fields above (defaults to ${SearchConfig.fields.id}) : ")
    val fieldNameInput: Option[String] = Option(readLine())
    val fieldName = fieldNameInput.filterNot(_.isEmpty).getOrElse(SearchConfig.fields.id)

    println()
    print("Enter search terms (or leave blank to search for empty values) : ")
    val searchTermsInput: Option[String] = Option(readLine())
    val searchTerms: Option[String] = searchTermsInput.filterNot(_.isEmpty)

    println(s"Searching ${documentType.getOrElse("all entitie").toString.toLowerCase}s for ${fieldName} with value of '${searchTerms.getOrElse("")}'")

    val results: Seq[Entity] = Searcher.search(documentType, fieldName, searchTerms)

    println(s"Found ${results.size} results")
    results.foreach(println)
  } while ( {
    print("Do you want to continue searching (y/n) ? : ")
    readBoolean()
  })
}
