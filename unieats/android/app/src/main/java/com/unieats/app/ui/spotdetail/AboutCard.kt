package com.unieats.app.ui.spotdetail

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.unieats.app.data.model.Spot
import com.unieats.app.ui.components.priceText
import java.text.DateFormat
import java.util.Date
import java.util.Locale

/** Collapsed: one line of description. Expanded: the full text plus details; the size change animates. */
@Composable
fun AboutCard(spot: Spot, modifier: Modifier = Modifier) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Card(
        onClick = { expanded = !expanded },
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(spring(dampingRatio = Spring.DampingRatioLowBouncy))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("About", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Show less" else "Show more"
                )
            }
            Text(
                spot.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (expanded) Int.MAX_VALUE else 1,
                overflow = TextOverflow.Ellipsis
            )
            if (expanded) {
                Detail("Price level", spot.priceText())
                Detail("Location", String.format(Locale.US, "%.4f, %.4f", spot.lat, spot.lng))
                Detail(
                    "Last updated",
                    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(spot.updatedAt))
                )
            }
        }
    }
}

@Composable
private fun Detail(label: String, value: String) {
    Row {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}
