package ro.cojocar.dan.portfolio.domain

data class Symbol(
  val id: Long,
  val name: String,
  val acquisitionDate: Long,
  val quantity: Long,
  val acquisitionPrice: Double
)