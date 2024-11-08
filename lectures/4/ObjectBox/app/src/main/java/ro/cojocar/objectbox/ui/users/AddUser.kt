package ro.cojocar.objectbox.ui.users

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ro.cojocar.objectbox.ui.users.viewmodel.UserViewModel


@Composable
fun AddUserScreen(viewModel: UserViewModel, goBack: () -> Unit) {
  var name by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }

  Box(
    modifier = Modifier
      .fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Name") },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(16.dp))
      OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email") },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(16.dp))

      Button(
        onClick = {
          if (name.isNotEmpty() && email.isNotEmpty()) {
            viewModel.addUser(name, email)
            goBack()
          }
        },
        modifier = Modifier.padding(16.dp)
      ) {
        Text("Add User")
      }
    }
  }
}
