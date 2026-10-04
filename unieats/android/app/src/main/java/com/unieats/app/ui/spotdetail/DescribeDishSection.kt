package com.unieats.app.ui.spotdetail

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.unieats.app.ui.TestTags

/** "Describe this dish": the AI text grows as the server streams it. */
@Composable
fun DescribeDishSection(
    text: String,
    isStreaming: Boolean,
    error: String?,
    onDescribe: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onDescribe,
                enabled = !isStreaming,
                modifier = Modifier.testTag(TestTags.DESCRIBE_DISH_BUTTON)
            ) { Text("Describe this dish") }
            if (isStreaming) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        }
        if (text.isNotEmpty()) {
            Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.testTag(TestTags.DISH_DESCRIPTION))
        }
        if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
    }
}
