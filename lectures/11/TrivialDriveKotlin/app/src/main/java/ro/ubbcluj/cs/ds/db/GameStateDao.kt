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

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameStateDao {
    @Query("SELECT `value` FROM GameState WHERE `key` = :key LIMIT 1")
    operator fun get(key: String): Flow<Int>

    @Query("REPLACE INTO GameState VALUES(:key,:value)")
    fun put(key: String, value: Int)

    @Query("UPDATE GameState SET `value`=`value`-1 WHERE `key`=:key AND `value` > :minValue")
    fun decrement(key: String, minValue: Int): Int

    @Query("UPDATE GameState SET `value`=`value`+1 WHERE `key`=:key AND `value` < :maxValue")
    fun increment(key: String, maxValue: Int): Int

    @Query("INSERT OR IGNORE INTO GameState VALUES(:key, 0)")
    fun initEntry(key: String)

    @Query("UPDATE GameState SET value = value + :amount WHERE key = :key")
    fun incrementBy(key: String, amount: Int)
}
