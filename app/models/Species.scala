package models

import models.TaxonomicStatus.TaxonomicStatus

class Species(val latinName: String) {
  // read from the system table in the db
  var familia: String = ""
  var ordo: String = ""
  var klasse: String = ""

  // appended from GBIF
  var status: TaxonomicStatus = TaxonomicStatus.UNKNOWN
  var GbifUsageKey: Int = 0
  var GbifResponse: String = ""

  // constructor from the GBIF api
  def this(
    latinName: String,
    _status: TaxonomicStatus,
    _GbifUsageKey: Int,
    _GbifResponse: String
  ) = {
    this(latinName)
    status = _status
    GbifUsageKey = _GbifUsageKey
    GbifResponse = _GbifResponse
  }
}