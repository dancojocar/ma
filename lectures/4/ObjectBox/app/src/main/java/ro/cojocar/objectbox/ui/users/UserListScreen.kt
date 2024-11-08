package ro.cojocar.objectbox.ui.users

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ro.cojocar.objectbox.domain.User
import ro.cojocar.objectbox.ui.users.viewmodel.UserViewModel

@Composable
fun UserListScreen(viewModel: UserViewModel, goToAddUser: () -> Unit) {
  val users: List<User> by viewModel.users.observeAsState(emptyList())

  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxWidth()
    ) {
      // Button to navigate to AddUserScreen
      Button(
        onClick = { goToAddUser() },
        modifier = Modifier.padding(16.dp)
      ) {
        Text("Add User")
      }

      LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
      ) {
        items(users) { user ->
          Text(
            text = "Name: ${user.name}, Email: ${user.email}",
            fontSize = 24.sp,
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Center
          )
        }
      }
    }
  }
}
