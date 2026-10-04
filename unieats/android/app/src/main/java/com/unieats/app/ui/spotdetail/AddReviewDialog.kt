package com.unieats.app.ui.spotdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.unieats.app.R

@Composable
fun AddReviewDialog(
    isSubmitting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (stars: Int, text: String) -> Unit
) {
    var stars by rememberSaveable { mutableIntStateOf(5) }
    var text by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add review") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StarPicker(stars = stars, onSelect = { stars = it })
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Your review") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMessage != null) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(stars, text) }, enabled = text.isNotBlank() && !isSubmitting) {
                Text(if (isSubmitting) "Posting…" else "Post")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/** Five radio buttons to TalkBack ("3 stars, selected, 3 of 5"), a row of stars on screen. */
@Composable
private fun StarPicker(stars: Int, onSelect: (Int) -> Unit) {
    Row(Modifier.selectableGroup()) {
        (1..5).forEach { value ->
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .selectable(selected = value == stars, onClick = { onSelect(value) }, role = Role.RadioButton),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(if (value <= stars) R.drawable.ic_star else R.drawable.ic_star_border),
                    contentDescription = if (value == 1) "1 star" else "$value stars",
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}
