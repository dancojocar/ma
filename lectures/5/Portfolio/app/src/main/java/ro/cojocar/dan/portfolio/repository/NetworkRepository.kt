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

  suspend fun auth(credentials: LoginCredentials): String? {
    val tokenHolder = PortfolioApi.service.authenticate(credentials)
    return tokenHolder.token
  }

  private suspend fun getPortfoliosFromNetwork(): List<Portfolio> =
    PortfolioApi.service.getPortfolios()

  fun setToken(authToken: String?) {
    PortfolioApi.tokenInterceptor.token = authToken
  }
}