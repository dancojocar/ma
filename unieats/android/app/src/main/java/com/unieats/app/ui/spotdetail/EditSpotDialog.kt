package com.unieats.app.ui.spotdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.unieats.app.R
import com.unieats.app.data.model.Spot

@Composable
fun EditSpotDialog(
    spot: Spot,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String, openNow: Boolean) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(spot.name) }
    var description by rememberSaveable { mutableStateOf(spot.description) }
    var openNow by rememberSaveable { mutableStateOf(spot.openNow) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.detail_edit_spot)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.edit_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.edit_description)) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(value = openNow, role = Role.Switch, onValueChange = { openNow = it })
                ) {
                    Text(stringResource(R.string.status_open_now), modifier = Modifier.weight(1f))
                    Switch(checked = openNow, onCheckedChange = null)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, description, openNow) }, enabled = name.isNotBlank()) {
                Text(stringResource(R.string.edit_save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}
