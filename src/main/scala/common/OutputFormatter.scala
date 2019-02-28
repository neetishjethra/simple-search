package common

trait OutputFormatter {
  def prettyOutputLines(x: Map[String, Any], header: Option[String] = None): Seq[String] = {
    header.map(headerLines).toList.flatten ++
      x.toList.sortBy(_._1).map({ case (key, value) =>
        val valueStr = value match {
          case seq: Seq[_] => seq.mkString(", ")
          case _ => value.toString
        }
        s"${key} : ${valueStr}"
      })
  }

  val separator = "********************************"
  val newLine = "\n"

  def headerLines(s: String) = Seq(separator, s, separator)
}
