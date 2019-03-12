package common

object DocumentType extends Enumeration {
  val USER = Value(1)
  val TICKET = Value(2)
  val ORGANIZATION = Value(3)
}


trait Entity extends OutputFormatter {
  def attributes: Map[String, Any]
}

case class Ticket(attributes: Map[String, Any],
                  submitter: Option[Map[String, Any]],
                  assignee: Option[Map[String, Any]],
                  organization: Option[Map[String, Any]]) extends Entity {

  override def toString: String = {
    (
      List(newLine, newLine) ++
        prettyOutputLines(attributes, Some(DocumentType.TICKET.toString)) ++
        submitter.map(prettyOutputLines(_, Some("Submitter (associated to ticket)"))).toList.flatten ++
        assignee.map(prettyOutputLines(_, Some("Assignee (associated to ticket)"))).toList.flatten ++
        organization.map(prettyOutputLines(_, Some("Organization (associated to ticket)"))).toList.flatten
      )
      .mkString(newLine)
  }
}


case class User(attributes: Map[String, Any],
                organization: Option[Map[String, Any]],
                submitted_tickets: Seq[Map[String, Any]],
                assigned_tickets: Seq[Map[String, Any]]) extends Entity {

  override def toString: String = {
    (
      List(newLine, newLine) ++
        prettyOutputLines(attributes, Some(DocumentType.USER.toString)) ++
        organization.map(prettyOutputLines(_, Some("Organization (associated to User)"))).toList.flatten ++
        submitted_tickets.map(prettyOutputLines(_, Some("Ticket(s) submitted by user"))).toList.flatten ++
        assigned_tickets.map(prettyOutputLines(_, Some("Ticket(s) assigned to user"))).toList.flatten
      )
      .mkString(newLine)
  }

}

case class Organization(attributes: Map[String, Any],
                        users: Seq[Map[String, Any]],
                        tickets: Seq[Map[String, Any]]) extends Entity {

  override def toString: String = {
    (
      List(newLine, newLine) ++
        prettyOutputLines(attributes, Some(DocumentType.ORGANIZATION.toString)) ++
        users.map(prettyOutputLines(_, Some("Users (associated to Organization)"))).toList.flatten ++
        tickets.map(prettyOutputLines(_, Some("Tickets (associated to Organization)"))).toList.flatten
      )
      .mkString(newLine)
  }

}