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


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import ro.ubbcluj.cs.ds.billingrepo.localdb.GasTank
import ro.ubbcluj.cs.ds.viewmodels.BillingViewModel
import kotlinx.android.synthetic.main.fragment_game.btn_drive
import kotlinx.android.synthetic.main.fragment_game.btn_purchase
import kotlinx.android.synthetic.main.fragment_game.free_or_premium_car
import kotlinx.android.synthetic.main.fragment_game.gas_gauge
import kotlinx.android.synthetic.main.fragment_game.gold_status

/**
 * This Fragment represents the game world. Hence it contains logic to display the items the user
 * owns -- gas, premium car, and gold status--,and logic for what it means to drive
 * the car; this is a driving game after all. When the user wants to buy, the app navigates to a
 * different Fragment.
 *
 * As you can see there is really nothing about Billing here. That's on purpose, all the billing
 * code reside in the [billingrepo][BillingRepository] layer and below.
 */
class GameFragment : androidx.fragment.app.Fragment() {
    private val LOG_TAG = "GameFragment"

    private var gasLevel: GasTank? = null
    private lateinit var billingViewModel: BillingViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            containter: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game, containter, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_drive.setOnClickListener { onDrive() }
        btn_purchase.setOnClickListener { onPurchase(it) }

        billingViewModel = ViewModelProvider(this).get(BillingViewModel::class.java)
        billingViewModel.gasTankLiveData.observe(viewLifecycleOwner, {
            gasLevel = it
            Log.d(LOG_TAG,"showGasLevel called from billingViewModel with level ${it?.getLevel()}")
            showGasLevel()
        })
        billingViewModel.premiumCarLiveData.observe(viewLifecycleOwner, {
            it?.apply { showPremiumCar(entitled) }
        })
        billingViewModel.goldStatusLiveData.observe(viewLifecycleOwner, {
            it?.apply { showGoldStatus(entitled) }
        })
    }

    private fun onDrive() {
        gasLevel?.apply {
            if (!needGas()) {
                billingViewModel.decrementAndSaveGas()
                showGasLevel()
                Toast.makeText(activity, getString(R.string.alert_drove), Toast.LENGTH_LONG).show()
            }
        }
        if (gasLevel?.needGas() != false) {
            Toast.makeText(activity, getString(R.string.alert_no_gas), Toast.LENGTH_LONG).show()
        }
    }

    private fun onPurchase(view: View) {
        view.findNavController().navigate(R.id.action_makePurchase)
    }

    private fun showGasLevel() {
        gasLevel?.apply {
            Log.d(LOG_TAG,"showGasLevel called with level ${getLevel()} ")
            val drawableName = "gas_level_${getLevel()}"
            val drawableId = resources.getIdentifier(
                    drawableName,
                    "drawable",
                    requireActivity().packageName
            )
            gas_gauge.setImageResource(drawableId)
        }
        if (gasLevel == null) {
            gas_gauge.setImageResource(R.drawable.gas_level_0)
        }
    }

    private fun showPremiumCar(entitled: Boolean) {
        if (entitled) {
            free_or_premium_car.setImageResource(R.drawable.premium_car)
        } else {
            free_or_premium_car.setImageResource(R.drawable.free_car)
        }
    }

    private fun showGoldStatus(entitled: Boolean) {
        if (entitled) {
            gold_status.setBackgroundResource(R.drawable.gold_status)
        } else {
            gold_status.setBackgroundResource(0)
        }
    }

}
