package ro.cojocar.dan.preferences.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ro.cojocar.dan.preferences.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val navController = rememberNavController()
    var currentTitle by remember { mutableStateOf(R.string.title) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = currentTitle)) },
                navigationIcon = {
                    if (navController.previousBackStackEntry != null) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "root",
            modifier = Modifier.padding(padding)
        ) {
            composable("root") {
                currentTitle = R.string.title
                Column {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.basic_preferences)) },
                        supportingContent = { Text("Sample preferences using basic attributes") },
                        modifier = Modifier.clickable { navController.navigate("basic") }
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.widgets)) },
                        supportingContent = { Text("Sample preferences with different widgets") },
                        modifier = Modifier.clickable { navController.navigate("widgets") }
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.dialogs)) },
                        supportingContent = { Text("Sample preferences that launch dialogs") },
                        modifier = Modifier.clickable { navController.navigate("dialogs") }
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.advanced_attributes)) },
                        supportingContent = { Text("Sample preferences with advanced attributes") },
                        modifier = Modifier.clickable { navController.navigate("advanced") }
                    )
                }
            }
            composable("basic") {
                currentTitle = R.string.basic_preferences
                BasicPreferencesScreen()
            }
            composable("widgets") {
                currentTitle = R.string.widgets
                WidgetsPreferencesScreen()
            }
            composable("dialogs") {
                currentTitle = R.string.dialogs
                DialogPreferencesScreen()
            }
            composable("advanced") {
                currentTitle = R.string.advanced_attributes
                AdvancedPreferencesScreen()
            }
        }
    }
}
