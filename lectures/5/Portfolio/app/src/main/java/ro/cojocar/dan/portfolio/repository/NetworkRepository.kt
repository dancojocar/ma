package ro.cojocar.dan.portfolio.repository

import ro.cojocar.dan.portfolio.domain.Portfolio
import ro.cojocar.dan.portfolio.service.LoginCredentials
import ro.cojocar.dan.portfolio.service.PortfolioApi

object NetworkRepository {
  suspend fun getPortfolios(): List<Portfolio> {
    val portfolios = getPortfoliosFromNetwork()
    portfolios.size > 1 || return emptyList()
    return portfolios
  }

  suspend fun auth(credentials: LoginCredentials): Boolean {
    if (PortfolioApi.tokenInterceptor.token == null) {
      val tokenHolder = PortfolioApi.service.authenticate(credentials)
      val token = tokenHolder.token
      PortfolioApi.tokenInterceptor.token = token
      return token != ""
    }
    return true
  }


  private suspend fun getPortfoliosFromNetwork(): List<Portfolio> =
    PortfolioApi.service.getPortfolios()
}