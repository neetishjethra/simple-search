package common

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.custom.CustomAnalyzer
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper
import org.apache.lucene.analysis.standard.StandardAnalyzer

import scala.collection.JavaConverters._

object SearchConfig {
  private val configTypeSafe: Config = ConfigFactory.load().resolve()
  val fullTextFields: Set[String] = configTypeSafe.getStringList("fullTextFields").asScala.toSet
  val fullTextAnalyzer = new StandardAnalyzer()
  val customKeywordAnalyzer: Analyzer = CustomAnalyzer.builder.withTokenizer("keyword").addTokenFilter("lowercase").build

  val analyzersMap = fullTextFields.map((_, fullTextAnalyzer)).toMap[String, Analyzer].asJava
  val indexPath = configTypeSafe.getString("index.dir")

  object fields {
    val allFieldNames = "all_field_names"
    val documentType = "document_type"
    val payload = "payload"
    val submitterId = "submitter_id"
    val assigneeId = "assignee_id"
    val organizationId = "organization_id"
    val id = "_id"
  }

  val analyzer: Analyzer = new PerFieldAnalyzerWrapper(customKeywordAnalyzer, analyzersMap);
}
