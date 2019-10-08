package ro.cojocar.dan.portfolio.repository

import ro.cojocar.dan.portfolio.await
import ro.cojocar.dan.portfolio.domain.Portfolio
import ro.cojocar.dan.portfolio.service.PortfolioApi

object NetworkRepository {
    suspend fun getPortfolios(): List<Portfolio> {
        val portfolios = getPortfoliosFromNetwork()
        portfolios.size > 1 || return emptyList()
        return portfolios
    }

    private suspend fun getPortfoliosFromNetwork(): List<Portfolio> = PortfolioApi.service.getPortfolios().await()
}