package com.unieats.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unieats.app.R
import com.unieats.app.data.model.Spot

/** The rating as shown in the list; [newDesign] is the remote-config flag show_new_rating_ui. */
@Composable
fun RatingBadge(spot: Spot, newDesign: Boolean, modifier: Modifier = Modifier) {
    val rated = stringResource(R.string.rated, spot.ratingText())
    val semanticsModifier = modifier.clearAndSetSemantics { contentDescription = rated }
    if (newDesign) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(50),
            modifier = semanticsModifier
        ) {
            Text(
                stringResource(R.string.rating_badge_pill, spot.ratingText()),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    } else {
        Row(
            modifier = semanticsModifier,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painterResource(R.drawable.ic_star),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.tertiary
            )
            Text(spot.ratingText(), style = MaterialTheme.typography.bodySmall)
        }
    }
}
