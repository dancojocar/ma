package com.imagescomposeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.imagescomposeapp.imagedetails.ImageDetailsScreen
import com.imagescomposeapp.imagedetails.ImageDetailsUiState
import com.imagescomposeapp.imagedetails.ImageDetailsViewModel
import com.imagescomposeapp.imageslist.ImagesListScreen
import com.imagescomposeapp.imageslist.ImagesViewModel
import com.imagescomposeapp.ui.theme.ImagesComposeAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val imagesListViewModel by viewModels<ImagesViewModel>()
    private val imageDetailsViewModel by viewModels<ImageDetailsViewModel>()

    @ExperimentalFoundationApi
    @ExperimentalComposeUiApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImagesComposeAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    val navController = rememberNavController()

                    AppNavigator(
                        imagesListViewModel = imagesListViewModel,
                        imageDetailsViewModel = imageDetailsViewModel,
                        navController
                    )
                }
            }
        }
    }
}

@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@Composable
fun AppNavigator(
    imagesListViewModel: ImagesViewModel,
    imageDetailsViewModel: ImageDetailsViewModel,
    navController: NavHostController
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val isLikeState = remember { imageDetailsViewModel.isLiked }
    val imageDetailsUiState = imageDetailsViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Home")
                },
                actions = {
                    if (
                        navBackStackEntry.value?.destination?.route == AppScreens.IMAGE_DETAILS_PATH &&
                        imageDetailsUiState.value is ImageDetailsUiState.Success
                    ) {
                        val imageData = (imageDetailsUiState.value as ImageDetailsUiState.Success).imageData

                        Icon(
                            if (isLikeState.value.not()) Icons.Default.FavoriteBorder else Icons.Filled.Favorite,
                            contentDescription = null,
                            modifier = Modifier.clickable {
                                imageDetailsViewModel.likeImage(imageData.id)
                            },
                        )
                    }
                }
            )
        },
        content = {
            NavHost(navController, startDestination = AppScreens.IMAGES_LIST) {
                composable(AppScreens.IMAGES_LIST) {
                    ImagesListScreen(imagesListViewModel) {
                        println(it)
                        navController.navigate(
                            "${AppScreens.IMAGE_DETAILS}/${it}",
                        )
                    }
                }
                composable(AppScreens.IMAGE_DETAILS_PATH) { backStackEntry ->
                    ImageDetailsScreen(
                        backStackEntry.arguments?.getString("imageId")?.toInt(),
                        imageDetailsViewModel
                    )
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ImagesComposeAppTheme {
        //HomeView(viewModel)
    }
}