package com.unieats.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.unieats.app.data.model.seedReviews
import com.unieats.app.data.model.seedSpots
import com.unieats.app.ui.spotdetail.SpotDetailScreen
import com.unieats.app.ui.spotlist.SpotListScreen
import com.unieats.app.ui.theme.UniEatsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UniEatsTheme {
                var selectedSpotId by rememberSaveable { mutableStateOf<String?>(null) }
                val selected = seedSpots.firstOrNull { it.id == selectedSpotId }
                if (selected == null) {
                    SpotListScreen(spots = seedSpots, onSpotClick = { selectedSpotId = it })
                } else {
                    BackHandler { selectedSpotId = null }
                    SpotDetailScreen(
                        spot = selected,
                        reviews = seedReviews.filter { it.spotId == selected.id },
                        onBack = { selectedSpotId = null }
                    )
                }
            }
        }
    }
}
