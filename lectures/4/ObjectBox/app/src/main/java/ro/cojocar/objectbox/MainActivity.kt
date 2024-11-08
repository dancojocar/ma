package ro.cojocar.objectbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ro.cojocar.objectbox.store.ObjectBoxStore.store
import ro.cojocar.objectbox.ui.users.AddUserScreen
import ro.cojocar.objectbox.ui.users.UserListScreen
import ro.cojocar.objectbox.ui.users.viewmodel.UserViewModel
import ro.cojocar.objectbox.ui.users.viewmodel.UserViewModelFactory

class MainActivity : ComponentActivity() {
  private val viewModel: UserViewModel by viewModels {
    UserViewModelFactory(store)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MainScreen(viewModel)
    }
  }
}

@Composable
fun MainScreen(viewModel: UserViewModel) {
  val navController = rememberNavController()

  NavHost(
    navController = navController,
    startDestination = "userList"
  ) {
    composable("userList") {
      UserListScreen(viewModel, goToAddUser = {
        navController.navigate("addUser")
      })
    }
    composable("addUser") {
      AddUserScreen(viewModel, goBack = {
        navController.navigate("userList")
      })
    }
  }
}
