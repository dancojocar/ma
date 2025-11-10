package ro.cojocar.dan.portfolio

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ro.cojocar.dan.portfolio.models.MainModel
import ro.cojocar.dan.portfolio.ui.screens.PortfolioDetailScreen
import ro.cojocar.dan.portfolio.ui.screens.PortfolioListScreen
import ro.cojocar.dan.portfolio.ui.theme.PortfolioTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PortfolioTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PortfolioApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun PortfolioApp(viewModel: MainModel) {
    val navController = rememberNavController()
    val portfolios by viewModel.portfolios.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "portfolio_list"
    ) {
        composable("portfolio_list") {
            PortfolioListScreen(
                viewModel = viewModel,
                onPortfolioClick = { portfolio ->
                    navController.navigate("portfolio_detail/${portfolio.id}")
                }
            )
        }

        composable(
            route = "portfolio_detail/{portfolioId}",
            arguments = listOf(navArgument("portfolioId") { type = NavType.LongType })
        ) { backStackEntry ->
            val portfolioId = backStackEntry.arguments?.getLong("portfolioId")
            val portfolio = portfolios.firstOrNull { it.id == portfolioId }
            
            PortfolioDetailScreen(
                portfolio = portfolio,
                onNavigateBack = { navController.popBackStack() },
                onFabClick = {
                    Toast.makeText(
                        navController.context,
                        "FAB clicked, no action yet!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }
}
