package ro.cojocar.dan.preferences.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ro.cojocar.dan.preferences.R

@Composable
fun BasicPreferencesScreen() {
    Column {
        PreferenceGroup(title = stringResource(id = R.string.basic_preferences)) {
            Preference(
                title = stringResource(id = R.string.title_basic_preference),
                summary = stringResource(id = R.string.summary_basic_preference)
            )
            Preference(
                title = stringResource(id = R.string.title_stylish_preference),
                summary = stringResource(id = R.string.summary_stylish_preference)
            )
            Preference(
                title = stringResource(id = R.string.title_icon_preference),
                summary = stringResource(id = R.string.summary_icon_preference),
                icon = R.drawable.ic_info_black_24dp
            )
            Preference(
                title = stringResource(id = R.string.title_single_line_title_preference),
                summary = stringResource(id = R.string.summary_single_line_title_preference)
            )
        }
    }
}

@Composable
fun PreferenceGroup(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            modifier = Modifier.padding(16.dp)
        )
        content()
    }
}

@Composable
fun Preference(title: String, summary: String, icon: Int? = null) {
    ListItem(
        headlineContent = { Text(text = title) },
        supportingContent = { Text(text = summary) },
        leadingContent = {
            if (icon != null) {
                // TODO: Add icon
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}
