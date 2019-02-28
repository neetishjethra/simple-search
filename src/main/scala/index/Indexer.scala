package index

import java.nio.file.Paths

import common.SearchConfig._
import common.{DocumentType, JsonSupport}
import org.apache.lucene.document._
import org.apache.lucene.index.IndexWriterConfig.OpenMode
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.FSDirectory
import org.json4s.jackson.Serialization


trait Indexer extends JsonSupport {

  var writerOption: Option[IndexWriter] = None

  def open = {
    val index = FSDirectory.open(Paths.get(indexPath))
    val config = new IndexWriterConfig(analyzer)
    config.setOpenMode(OpenMode.CREATE)
    writerOption = Some(new IndexWriter(index, config))
  }

  def close() = {
    writerOption.map(_.close())
  }

  def addDoc(docType: DocumentType.Value)(valueMap: Map[String, Any]) = {
    writerOption match {
      case Some(writer) => writer.addDocument(buildDoc(valueMap, docType))
      case None => throw new IllegalStateException("Need to call open method on Indexer before using it")
    }
  }

  def buildDoc(valueMap: Map[String, Any], docType: DocumentType.Value): Document = {
    val doc = new Document()
    valueMap.foreach({ case (key, value) =>
      //index each value in lists separately
      val valueList = value match {
        case iterable: Iterable[_] => iterable
        case _ => List(value)
      }
      valueList.foreach(value => doc.add(new TextField(key, value.toString, Field.Store.NO)))
      doc.add(new StringField(fields.allFieldNames, key, Field.Store.NO))
    })

    doc.add(new StringField(fields.documentType, docType.toString, Field.Store.YES))
    doc.add(new StoredField(fields.payload, Serialization.write(valueMap)))
    doc
  }
}
