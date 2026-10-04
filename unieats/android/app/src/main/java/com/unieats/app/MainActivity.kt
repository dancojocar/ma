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
import com.unieats.app.data.repository.SpotRepository
import com.unieats.app.ui.spotdetail.SpotDetailScreen
import com.unieats.app.ui.spotlist.SpotListScreen
import com.unieats.app.ui.theme.UniEatsTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var spotRepository: SpotRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UniEatsTheme {
                var selectedSpotId by rememberSaveable { mutableStateOf<String?>(null) }
                val selected = selectedSpotId?.let(spotRepository::getSpot)
                if (selected == null) {
                    SpotListScreen(onSpotClick = { selectedSpotId = it })
                } else {
                    BackHandler { selectedSpotId = null }
                    SpotDetailScreen(
                        spot = selected,
                        reviews = spotRepository.getReviews(selected.id),
                        onBack = { selectedSpotId = null }
                    )
                }
            }
        }
    }
}
