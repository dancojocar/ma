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

package ro.ubbcluj.cs.ds.db

import android.app.Application
import androidx.room.Room
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.withContext

class GameStateModel(application: Application) {
    private val db: GameStateDatabase
    private val gameStateDao: GameStateDao
    private val gasTankLevel: Flow<Int>
    private val odometer: Flow<Int>
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO

    suspend fun decrementGas(minLevel: Int): Int {
        return withContext(defaultDispatcher) {
            gameStateDao.decrement(GAS_LEVEL, minLevel)
        }
    }

    suspend fun incrementGas(maxLevel: Int): Int {
        return withContext(defaultDispatcher) {
            gameStateDao.increment(GAS_LEVEL, maxLevel)
        }
    }

    fun gasTankLevel(): Flow<Int> {
        return gasTankLevel
    }

    fun getOdometer(): Flow<Int> {
        return odometer
    }

    suspend fun incrementOdometer(amount: Int) {
        withContext(defaultDispatcher) {
            gameStateDao.initEntry(ODOMETER_KEY)
            gameStateDao.incrementBy(ODOMETER_KEY, amount)
        }
    }

    companion object {
        private const val GAS_LEVEL = "gas"
        private const val ODOMETER_KEY = "odometer"
    }

    init {
        // This creates our DB and populates our game state database with the initial state of
        // a full tank
        db = Room.databaseBuilder(
            application,
            GameStateDatabase::class.java, "GameState.db"
        )
            .createFromAsset("database/initialgamestate.db")
            .build()
        gameStateDao = db.gameStateDao()
        // this causes the gasTankLevel from our Room database to behave more like LiveData
        gasTankLevel = gameStateDao[GAS_LEVEL].distinctUntilChanged().shareIn(CoroutineScope(Dispatchers.Main), SharingStarted.Lazily, 1)
        odometer = gameStateDao[ODOMETER_KEY].distinctUntilChanged().shareIn(CoroutineScope(Dispatchers.Main), SharingStarted.Lazily, 1)
    }
}
