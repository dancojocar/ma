package ro.cojocar.dan.portfolio.domain

sealed class Action

data class PortfolioAction(val userId: Long) : Action()