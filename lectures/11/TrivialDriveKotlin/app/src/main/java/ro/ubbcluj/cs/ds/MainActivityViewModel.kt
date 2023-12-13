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

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData

/*
   This is used for any business logic, as well as to echo LiveData from the BillingRepository.
*/
class MainActivityViewModel(private val tdr: TrivialDriveRepository) : ViewModel() {

    val messages: LiveData<Int>
        get() = tdr.messages.asLiveData()

    fun debugConsumePremium() {
        tdr.debugConsumePremium()
    }

    val billingLifecycleObserver: LifecycleObserver
        get() = tdr.billingLifecycleObserver

    companion object {
        val TAG = "TrivialDrive:" + GameViewModel::class.simpleName
    }

    class MainActivityViewModelFactory(private val trivialDriveRepository: TrivialDriveRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
                return MainActivityViewModel(trivialDriveRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
