package ro.cojocar.dan.preferences.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import ro.cojocar.dan.preferences.R

@Composable
fun DialogPreferencesScreen(viewModel: SettingsViewModel = viewModel()) {
    Column {
        PreferenceGroup(title = stringResource(id = R.string.dialogs)) {
            EditTextPreference(
                viewModel = viewModel,
                title = stringResource(id = R.string.title_edittext_preference),
                dialogTitle = stringResource(id = R.string.dialog_title_edittext_preference),
                key = "edittext_preference"
            )
        }
    }
}

@Composable
fun EditTextPreference(
    viewModel: SettingsViewModel,
    title: String,
    dialogTitle: String,
    key: String
) {
    var showDialog by remember { mutableStateOf(false) }
    val value by viewModel.getString(key, "").collectAsState()

    ListItem(
        headlineContent = { Text(text = title) },
        supportingContent = { Text(text = value) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
    )

    if (showDialog) {
        var text by remember { mutableStateOf(value) }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = dialogTitle) },
            text = {
                TextField(value = text, onValueChange = { text = it })
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.setString(key, text)
                    showDialog = false
                }) {
                    Text("OK")
                }
            }
        )
    }
}
