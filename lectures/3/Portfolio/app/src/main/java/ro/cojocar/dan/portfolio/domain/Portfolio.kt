package ro.cojocar.dan.portfolio.domain

data class Portfolio(
    val id: Long,
    val name: String,
    val lastModified: Long,
    val symbols: List<Symbol> = mutableListOf()
)