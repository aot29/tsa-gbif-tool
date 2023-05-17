package models

import play.api.libs.json.{Reads, Writes}

object TaxonomicStatus extends Enumeration {
  type TaxonomicStatus = Value
  val ACCEPTED, SYNONYM, CONFLICTING, UNKNOWN = Value
  implicit val TaxonomicStatusReads = Reads.enumNameReads(TaxonomicStatus)
  implicit val TaxonomicStatusWrites = Writes.enumNameWrites
}