package com.example.kotlindemos

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val demos = DemoDetailsList.DEMOS

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier.fillMaxSize()
        ) {
            items(demos) { demo ->
                DemoItem(demo) {
                    context.startActivity(Intent(context, demo.activityClass))
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun DemoItem(demo: DemoDetails, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(demo.titleId),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = stringResource(demo.descriptionId),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
