package ro.cojocar.objectbox.ui.users

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
fun UserListScreen(
  viewModel: UserViewModel,
  goToAddUser: () -> Unit,
  goToEditUser: (Long) -> Unit
) {
  val users: List<User> by viewModel.users.observeAsState(emptyList())

  Scaffold(
    floatingActionButton = {
      FloatingActionButton(onClick = { goToAddUser() }) {
        Icon(Icons.Default.Add, contentDescription = "Add User")
      }
    }
  ) { paddingValues ->
    LazyColumn(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .padding(paddingValues)
    ) {
      items(users) { user ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { goToEditUser(user.id) }
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Name: ${user.name}, Email: ${user.email}",
            fontSize = 20.sp,
            textAlign = TextAlign.Center
          )
          IconButton(onClick = { viewModel.removeUser(user) }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete User")
          }
        }
      }
    }
  }
}
