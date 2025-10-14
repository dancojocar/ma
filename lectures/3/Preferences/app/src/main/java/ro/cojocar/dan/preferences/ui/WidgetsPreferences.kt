package ro.cojocar.dan.preferences.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ro.cojocar.dan.preferences.R

@Composable
fun WidgetsPreferencesScreen(viewModel: SettingsViewModel = viewModel()) {
    Column {
        PreferenceGroup(title = stringResource(id = R.string.widgets)) {
            CheckBoxPreference(
                viewModel = viewModel,
                title = stringResource(id = R.string.title_checkbox_preference),
                summary = stringResource(id = R.string.summary_checkbox_preference),
                key = "checkbox_preference_1"
            )
            CheckBoxPreference(
                viewModel = viewModel,
                title = stringResource(id = R.string.title_checkbox_preference),
                summary = stringResource(id = R.string.summary_checkbox_preference),
                key = "checkbox_preference_2"
            )
            SwitchPreference(
                viewModel = viewModel,
                title = stringResource(id = R.string.title_switch_preference),
                summary = stringResource(id = R.string.summary_switch_preference),
                key = "switch_preference"
            )
        }
    }
}

@Composable
fun CheckBoxPreference(
    viewModel: SettingsViewModel,
    title: String,
    summary: String,
    key: String
) {
    val checked by viewModel.getBoolean(key, false).collectAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title)
            Text(text = summary)
        }
        Checkbox(checked = checked, onCheckedChange = { viewModel.setBoolean(key, it) })
    }
}

@Composable
fun SwitchPreference(
    viewModel: SettingsViewModel,
    title: String,
    summary: String,
    key: String
) {
    val checked by viewModel.getBoolean(key, false).collectAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title)
            Text(text = summary)
        }
        Switch(checked = checked, onCheckedChange = { viewModel.setBoolean(key, it) })
    }
}
