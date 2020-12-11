/**
 * Copyright (C) 2018 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.ubbcluj.cs.ds

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import ro.ubbcluj.cs.ds.billingrepo.localdb.AugmentedSkuDetailsDao
import ro.ubbcluj.cs.ds.billingrepo.localdb.EntitlementsDao
import ro.ubbcluj.cs.ds.billingrepo.localdb.GasTank
import ro.ubbcluj.cs.ds.billingrepo.localdb.GoldStatus
import ro.ubbcluj.cs.ds.billingrepo.localdb.LocalBillingDb
import ro.ubbcluj.cs.ds.billingrepo.localdb.PremiumCar
import ro.ubbcluj.cs.ds.billingrepo.localdb.PurchaseDao
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class BillingLocalDBTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: LocalBillingDb
    private lateinit var purchaseDao: PurchaseDao
    private lateinit var entitlementsDao: EntitlementsDao
    private lateinit var skuDetailsDao: AugmentedSkuDetailsDao

    @Before
    @Throws(Exception::class)
    fun initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        database = Room.inMemoryDatabaseBuilder(
                InstrumentationRegistry.getContext(),
                LocalBillingDb::class.java
        ).allowMainThreadQueries().build() // allowing main thread queries, just for testing

        purchaseDao = database.purchaseDao()
        entitlementsDao = database.entitlementsDao()
        skuDetailsDao = database.skuDetailsDao()
    }

    @After
    @Throws(Exception::class)
    fun closeDb() {
        database.close()
    }

    @Test
    @Throws(InterruptedException::class)
    fun getGasTankNull() {
        val gasTank = entitlementsDao.getGasTank().getItem()
        assertNull(gasTank)
    }

    @Test
    @Throws(InterruptedException::class)
    fun insertGasTank(){
        val expectedLevel = 4
        entitlementsDao.insert(GasTank(expectedLevel))
        val gasTank=entitlementsDao.getGasTank().getItem()
        assertNotNull(gasTank)
        assertEquals(expectedLevel,gasTank.getLevel())
    }

    @Test
    @Throws(InterruptedException::class)
    fun insertGasTankOnConflictReplace(){
        val expectedLevel=2
        entitlementsDao.insert(GasTank(expectedLevel+expectedLevel))
        entitlementsDao.insert(GasTank(expectedLevel))
        val gasTank=entitlementsDao.getGasTank().getItem()
        assertNotNull(gasTank)
        assertEquals(expectedLevel,gasTank.getLevel())
    }

    @Test
    @Throws(InterruptedException::class)
    fun getGoldStatusNull() {
        val goldStatus = entitlementsDao.getGoldStatus().getItem()
        assertNull(goldStatus)
    }

    @Test
    @Throws(InterruptedException::class)
    fun insertGoldStatus(){
        entitlementsDao.insert(GoldStatus(true))
        val goldStatus = entitlementsDao.getGoldStatus().getItem()
        assertNotNull(goldStatus)
        assertTrue(goldStatus.entitled)
    }

    @Test
    @Throws(InterruptedException::class)
    fun getPremiumCarNull(){
        val premiumCar = entitlementsDao.getPremiumCar().getItem()
        assertNull(premiumCar)
    }

    @Test
    @Throws(InterruptedException::class)
    fun insertPremiumCar(){
        entitlementsDao.insert(PremiumCar(true))
        val premiumCar = entitlementsDao.getPremiumCar().getItem()
        assertNotNull(premiumCar)
        assertTrue(premiumCar.entitled)
    }

    /**
     * This method receives a [LiveData] as parameter, observes it, and return its content.
     * This method is "necessary" because the -Dao methods return LiveData objects.
     *
     * A [CountDownLatch] is used to force this thread to wait for the LiveData to emit. So as not
     * to wait too long, a max wait time of 2 seconds is used.
     */
    @Throws(InterruptedException::class)
    fun <T> LiveData<T>.getItem(): T {
        val data = arrayOfNulls<Any>(1)
        val latch = CountDownLatch(1)
        val observer = object : Observer<T> {
            override fun onChanged(t: T) {
                data[0] = t
                latch.countDown()
                removeObserver(this)
            }
        }
        observeForever(observer)
        latch.await(2, TimeUnit.SECONDS)
        return data[0] as T
    }
}