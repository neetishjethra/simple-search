package search

import java.nio.file.Paths

import common.SearchConfig._
import common._
import org.apache.lucene.index.{DirectoryReader, Term}
import org.apache.lucene.queryparser.classic.{QueryParser, QueryParserBase}
import org.apache.lucene.search._
import org.apache.lucene.store.FSDirectory
import org.json4s.jackson.Serialization

trait Searcher extends JsonSupport with Logging {

  lazy val HITS_PER_PAGE = 10

  def search(collectionType: Option[DocumentType.Value] = Some(DocumentType.TICKET), fieldName: String, searchTermOption: Option[String]): Seq[Entity] = {
    val index = FSDirectory.open(Paths.get(indexPath))
    val reader = DirectoryReader.open(index)
    val indexSearcher = new IndexSearcher(reader)

    val indexSearchResults = getIndexSearchResults(collectionType, fieldName, searchTermOption, indexSearcher)
    val results = indexSearchResults.map(result =>
      result.get(fields.documentType).get match {
        case DocumentType.TICKET =>
          val submitter = result.get(fields.submitterId).flatMap(id => getIndexSearchResults(Some(DocumentType.USER), fields.id, Some(id.toString), indexSearcher).headOption)
          val assignee = result.get(fields.assigneeId).flatMap(id => getIndexSearchResults(Some(DocumentType.USER), fields.id, Some(id.toString), indexSearcher).headOption)
          val organization = result.get(fields.organizationId).flatMap(id => getIndexSearchResults(Some(DocumentType.ORGANIZATION), fields.id, Some(id.toString), indexSearcher).headOption)
          Ticket(result, submitter, assignee, organization)
        case DocumentType.USER =>
          val userId = result.get(fields.id).get.toString
          val submittedTickets = getIndexSearchResults(Some(DocumentType.TICKET), fields.submitterId, Some(userId), indexSearcher)
          val assignedTickets = getIndexSearchResults(Some(DocumentType.TICKET), fields.assigneeId, Some(userId), indexSearcher)
          val organization = result.get(fields.organizationId).flatMap(id => getIndexSearchResults(Some(DocumentType.ORGANIZATION), fields.id, Some(id.toString), indexSearcher).headOption)
          User(result, organization, submittedTickets, assignedTickets)
        case DocumentType.ORGANIZATION =>
          val organizationId = result.get(fields.id).get.toString
          val tickets = getIndexSearchResults(Some(DocumentType.TICKET), fields.organizationId, Some(organizationId), indexSearcher)
          val users = getIndexSearchResults(Some(DocumentType.USER), fields.organizationId, Some(organizationId), indexSearcher)
          Organization(result, users, tickets)
      }
    )

    index.close()
    reader.close()

    results
  }

  private def getIndexSearchResults(collectionType: Option[DocumentType.Value], fieldName: String, searchTermOption: Option[String], indexSearcher: IndexSearcher): Seq[Map[String, Any]] = {
    val query: Query = getSearchQuery(collectionType, fieldName, searchTermOption)
    val docs: TopDocs = indexSearcher.search(query, HITS_PER_PAGE)
    val hits: Array[ScoreDoc] = docs.scoreDocs


    val results: Array[Map[String, Any]] = hits
      .map(scoreDoc => indexSearcher.doc(scoreDoc.doc))
      .map(document =>
        Serialization.read[Map[String, Any]](document.getField(fields.payload).stringValue()) +
          (fields.documentType -> DocumentType.withName(document.getField(fields.documentType).stringValue()))
      )
    results
  }

  private def getSearchQuery(documentType: Option[DocumentType.Value], fieldName: String, searchTermOption: Option[String]): Query = {
    val boolQueryBuilder = new BooleanQuery.Builder()

    searchTermOption match {
      case None =>
        boolQueryBuilder.add(new MatchAllDocsQuery(), BooleanClause.Occur.FILTER)
        boolQueryBuilder.add(new TermQuery(new Term(fields.allFieldNames, fieldName)), BooleanClause.Occur.MUST_NOT)
      case Some(searchTerm) =>
        val queryParser = new QueryParser(fieldName, analyzer)
        queryParser.setDefaultOperator(QueryParser.Operator.AND)
        boolQueryBuilder.add(queryParser.parse(QueryParserBase.escape(searchTerm)), BooleanClause.Occur.MUST)
    }

    documentType.foreach(colType => boolQueryBuilder.add(new TermQuery(new Term(fields.documentType, colType.toString)), BooleanClause.Occur.FILTER))

    val query = boolQueryBuilder.build()
    log.debug(query.toString())
    query
  }
}
