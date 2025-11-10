package ro.cojocar.dan.portfolio.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ro.cojocar.dan.portfolio.domain.Portfolio
import ro.cojocar.dan.portfolio.models.MainModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioListScreen(
    viewModel: MainModel,
    onPortfolioClick: (Portfolio) -> Unit
) {
    val portfolios by viewModel.portfolios.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val message by viewModel.message.collectAsState()
    val scope = rememberCoroutineScope()
    var isAuthenticated by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Show message as snackbar
    LaunchedEffect(message) {
        message?.let {
            if (it.isNotBlank()) {
                snackbarHostState.showSnackbar(
                    message = it,
                    duration = if (it.contains("not available") || it.contains("failed") || it.contains("Error")) {
                        SnackbarDuration.Long
                    } else {
                        SnackbarDuration.Short
                    }
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Portfolio") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        if (viewModel.auth()) {
                            isAuthenticated = true
                            viewModel.fetchData()
                        }
                    }
                },
                containerColor = if (isAuthenticated) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.secondary
            ) {
                Icon(
                    imageVector = if (isAuthenticated) Icons.Default.Refresh else Icons.Default.Security,
                    contentDescription = if (isAuthenticated) "Refresh" else "Authenticate"
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (loading) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading portfolios...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (portfolios.isEmpty()) {
                // Show empty state
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No portfolios available",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap the floating action button to authenticate and load data from the server",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Note: Ensure the server is running at http://10.0.2.2:8080",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(portfolios) { portfolio ->
                        PortfolioListItem(
                            portfolio = portfolio,
                            onClick = { onPortfolioClick(portfolio) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PortfolioListItem(
    portfolio: Portfolio,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = portfolio.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Symbols: ${portfolio.symbols.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
