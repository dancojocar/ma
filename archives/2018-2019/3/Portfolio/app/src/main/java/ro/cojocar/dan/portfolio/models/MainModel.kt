package ro.cojocar.dan.portfolio.models

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.android.Main
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import ro.cojocar.dan.portfolio.domain.Action
import ro.cojocar.dan.portfolio.domain.Portfolio
import ro.cojocar.dan.portfolio.domain.PortfolioAction
import ro.cojocar.dan.portfolio.getFreshPortfolios
import ro.cojocar.dan.portfolio.repository.NetworkRepository


class MainModel : ViewModel() {
    private val cache = HashMap<Long, List<Portfolio>>(10)

    private val mutablePortfolio = MutableLiveData<Long>().apply { value = 0 }
    private val mutablePortfolios = MutableLiveData<List<Portfolio>>().apply { value = emptyList() }
    private val mutableLoading = MutableLiveData<Boolean>().apply { value = false }
    private val mutableMessage = MutableLiveData<String>()

    val portfolio: LiveData<Long> = mutablePortfolio
    val portfolios: LiveData<List<Portfolio>> = mutablePortfolios
    val loading: LiveData<Boolean> = mutableLoading
    val message: LiveData<String> = mutableMessage


    private val actor = GlobalScope.actor<Action>(Dispatchers.Main, Channel.CONFLATED) {
        for (action in this) when (action) {
            is PortfolioAction -> {
                mutablePortfolio.value = action.userId
                mutableLoading.value = true
                try {
                    mutablePortfolios.value = cache.getFreshPortfolios(action.userId) ?: getNewPortfolios(action.userId)
                } catch (e: Exception) {
                    mutableMessage.value = e.toString()
                }
                mutableLoading.value = false
            }
        }
    }

    init {
        action(PortfolioAction(0))
    }


    fun action(action: Action) = actor.offer(action)

    override fun onCleared() {
        actor.close()
    }

    /**
     * @throws IllegalStateException
     */
    private suspend fun getNewPortfolios(userId: Long) = NetworkRepository.getPortfolios().also { cache[userId] = it }
}