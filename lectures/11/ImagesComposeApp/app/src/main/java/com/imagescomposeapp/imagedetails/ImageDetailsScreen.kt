package com.imagescomposeapp.imagedetails

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.google.accompanist.flowlayout.SizeMode

@Composable
fun ImageDetailsScreen(
    imageId: Int?,
    viewModel: ImageDetailsViewModel
) {
    print(imageId)
    LaunchedEffect(key1 = imageId) {
        viewModel.getImage(imageId)
    }

    val scrollState = rememberScrollState()

    when (val uiState = viewModel.uiState.collectAsState().value) {
        ImageDetailsUiState.Loading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        }
        is ImageDetailsUiState.Success -> {
            Column(Modifier.verticalScroll(scrollState)) {
                Image(
                    painter = rememberImagePainter(uiState.imageData.url),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Crop
                )
                Row(Modifier.padding(16.dp)) {
                    Icon(
                        Icons.Filled.LocationOn,
                        "Location"
                    )
                    uiState.imageData.location?.let {
                        Text(text = it)
                    }
                }
                Box(modifier = Modifier.padding(8.dp)) {
                    FlowRow(
                        mainAxisAlignment = MainAxisAlignment.Center,
                        mainAxisSize = SizeMode.Expand,
                        crossAxisSpacing = 12.dp,
                        mainAxisSpacing = 8.dp
                    ) {
                        uiState.imageData.tags.forEach { hashTag ->
                            Text(
                                text = hashTag,
                                modifier = Modifier.drawBehind {
                                    RoundedCornerShape(4.dp)
                                }.padding(8.dp),
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
        is ImageDetailsUiState.Error -> {

        }
    }
}
