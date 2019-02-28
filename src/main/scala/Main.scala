import java.nio.file.{Files, Path, Paths}

import com.concurrentthought.cla._
import common.{DocumentType, Entity, Logging}
import index.{Indexer, Parser}
import search.Searcher

object Main extends App with Logging {

  val initialArgs: Args =
    """
      |sbt "run [options] [options]"
      |
      |e.g. sbt "run --search-terms=114  --field-name=_id --doc-type=ORGANIZATION"
      |
      |OR
      |
      |java -jar simple-search-fat.jar --search-terms=114  --field-name=_id --doc-type=ORGANIZATION
      |
      |The application searches the dataset for Tickets, Organizations and Users.
      |
      |Most of the search testing is centered around single term exact match, but multiple terms search will do
      |something reasonable in most cases. They just need to be passed in pipe (|) delimited to get around
      |command line restrictions. Multi term will try to match all terms and won't care about order of terms.
      |
      |  [--doc-type        string]                     Document type to search - TICKET, ORGANIZATION or USER
      |  --field-name       string                      Field to search for, e.g. subject, description, _id
      |  [--search-terms    seq([|])]                   Leave blank to check for field being empty. To pass multiple values, use |
      |  --json-path        string=src/main/resources   Path to json files if it needs to be overridden
      |""".stripMargin.toArgs

  val finalArgs: Args = initialArgs.process(args)

  log.debug(finalArgs.allValues.toString)

  object Parser extends Parser

  object Searcher extends Searcher

  object Indexer extends Indexer

  log.debug("started indexing")

  Indexer.open

  val jsonPath: Path = Paths.get(finalArgs.get[String]("json-path").getOrElse("src/main/resources"))


  Parser.parseStreamAndProcess(Files.newInputStream(jsonPath.resolve("tickets.json")), Indexer.addDoc(DocumentType.TICKET))
  log.debug("finished indexing tickets")
  Parser.parseStreamAndProcess(Files.newInputStream(jsonPath.resolve("users.json")), Indexer.addDoc(DocumentType.USER))
  log.debug("finished indexing users")
  Parser.parseStreamAndProcess(Files.newInputStream(jsonPath.resolve("organizations.json")), Indexer.addDoc(DocumentType.ORGANIZATION))
  log.debug("finished indexing organizations")

  Indexer.close

  log.debug("finished indexing")

  val results: Seq[Entity] = Searcher.search(
    finalArgs.get[String]("doc-type").map(docTypeStr => DocumentType.withName(docTypeStr.toUpperCase)),
    finalArgs.get[String]("field-name").get,
    finalArgs.get[Seq[String]]("search-terms").map(_.mkString(" "))
  )

  println(s"Showing ${results.size} results")
  results.foreach(println)
}
