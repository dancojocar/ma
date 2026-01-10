/*
 * Copyright (C) 2021 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ro.ubbcluj.cs.ds

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

/*
   This is used for any business logic, as well as to echo LiveData from the BillingRepository.
*/
class GameViewModel(private val tdr: TrivialDriveRepository) : ViewModel() {
  private val _isDriving = androidx.lifecycle.MutableLiveData(false)
  val isDriving: LiveData<Boolean> = _isDriving

  fun startDriving() {
    _isDriving.value = true
  }

  fun finishDriving() {
    viewModelScope.launch {
      tdr.drive()
      _isDriving.value = false
    }
  }

  private val _isManualDriving = androidx.lifecycle.MutableLiveData(false)
  val isManualDriving: LiveData<Boolean> = _isManualDriving

  // Timer state (seconds remaining)
  private val _manualTimeRemaining = androidx.lifecycle.MutableLiveData(0)
  val manualTimeRemaining: LiveData<Int> = _manualTimeRemaining

  private var timerJob: kotlinx.coroutines.Job? = null

  fun startManualDriving() {
    _isManualDriving.value = true
    _manualTimeRemaining.value = 10

    timerJob?.cancel()
    timerJob = viewModelScope.launch {
      for (i in 10 downTo 1) {
        _manualTimeRemaining.value = i
        kotlinx.coroutines.delay(1000)
      }
      finishManualDriving()
    }
  }

  fun finishManualDriving() {
    if (_isManualDriving.value == true) {
      timerJob?.cancel()
      viewModelScope.launch {
        tdr.drive() // Consume gas
        _isManualDriving.value = false
        _manualTimeRemaining.value = 0
      }
    }
  }

  /*
      We can drive if we have at least one unit of gas or if we are premium/infinite.
      However, repo's gasTankLevel returns INFINITE (5) if premium/subscribed.
      So check > 0 is actually sufficient if INFINITE > 0 (5 > 0).
   */
  fun canDrive(): LiveData<Boolean> = gasUnitsRemaining.map { gasUnits: Int -> gasUnits > 0 }

  val isPremium: LiveData<Boolean>
    get() = tdr.isPurchased(TrivialDriveRepository.SKU_PREMIUM).asLiveData()

  val gasUnitsRemaining: LiveData<Int>
    get() = tdr.gasTankLevel().shareIn(viewModelScope, SharingStarted.Lazily).asLiveData()

  val odometer: LiveData<Int>
    get() = tdr.getOdometer().asLiveData()

  companion object {
    val TAG = GameViewModel::class.simpleName
  }

  class GameViewModelFactory(private val trivialDriveRepository: TrivialDriveRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
        return GameViewModel(trivialDriveRepository) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class")
    }
  }
}
