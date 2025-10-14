package ro.cojocar.dan.preferences.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ro.cojocar.dan.preferences.R

@Composable
fun AdvancedPreferencesScreen(viewModel: SettingsViewModel = viewModel()) {
    val context = LocalContext.current
    val parentEnabled by viewModel.getBoolean("parent_preference", false).collectAsState()

    Column {
        PreferenceGroup(title = stringResource(id = R.string.advanced_attributes)) {
            Preference(
                title = stringResource(id = R.string.title_expandable_preference),
                summary = stringResource(id = R.string.summary_expandable_preference),
                onClick = {}
            )
            Preference(
                title = stringResource(id = R.string.title_intent_preference),
                summary = stringResource(id = R.string.summary_intent_preference),
                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://www.android.com"))) }
            )
            SwitchPreference(
                title = stringResource(id = R.string.title_parent_preference),
                summary = stringResource(id = R.string.summary_parent_preference),
                checked = parentEnabled,
                onCheckedChange = { viewModel.setBoolean("parent_preference", it) }
            )
            val childEnabled by viewModel.getBoolean("child_preference", false).collectAsState()
            SwitchPreference(
                title = stringResource(id = R.string.title_child_preference),
                summary = stringResource(id = R.string.summary_child_preference),
                checked = childEnabled,
                onCheckedChange = { viewModel.setBoolean("child_preference", it) },
                enabled = parentEnabled
            )
        }
    }
}

@Composable
fun Preference(title: String, summary: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(text = title) },
        supportingContent = { Text(text = summary) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    )
}

@Composable
fun SwitchPreference(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
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
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}
