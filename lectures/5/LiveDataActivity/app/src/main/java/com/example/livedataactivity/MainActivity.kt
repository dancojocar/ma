package com.example.livedataactivity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import android.app.Activity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.example.livedataactivity.core.Person
import com.example.livedataactivity.image.NetworkImageComponentPicasso
import com.example.livedataactivity.state.SuperheroesViewModel
import com.example.livedataactivity.ui.theme.LiveDataActivityTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val viewModel = ViewModelProvider(this)[SuperheroesViewModel::class.java]

    setContent {
      val view = LocalView.current
      if (!view.isInEditMode) {
        SideEffect {
          val window = (view.context as Activity).window
          WindowCompat.setDecorFitsSystemWindows(window, false)
        }
      }
      EdgeToEdgeContent(viewModel)
    }
  }
}

@Composable
fun EdgeToEdgeContent(viewModel: SuperheroesViewModel) {
  LiveDataActivityTheme {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .systemBarsPadding(),
    ) {
      LiveDataComponent(viewModel.superheroes)
    }
  }
}

@Composable
fun LiveDataComponent(personListLiveData: LiveData<List<Person>>) {
  val personList by personListLiveData.observeAsState(initial = emptyList())
  if (personList.isEmpty()) {
    LiveDataLoadingComponent()
  } else {
    LiveDataComponentList(personList)
  }
}

@Composable
fun LiveDataComponentList(personList: List<Person>) {
  LazyColumn {
    items(
      items = personList
    ) { person ->
      Card(
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier
          .fillParentMaxWidth()
          .padding(8.dp)
      ) {
        ListItem(headlineContent = {
          Text(
            text = person.name,
            style = MaterialTheme.typography.headlineLarge
          )
        }, supportingContent = {
          Text(
            text = "Age: ${person.age}",
            style = MaterialTheme.typography.bodyLarge
          )
        }, leadingContent = {
          person.profilePictureUrl?.let { imageUrl ->
            NetworkImageComponentPicasso(
              url = imageUrl,
              modifier = Modifier
                .width(60.dp)
                .height(60.dp)
            )
          }
        })
      }
    }
  }
}

@Composable
fun LiveDataLoadingComponent() {
  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    CircularProgressIndicator(modifier = Modifier.wrapContentSize(Alignment.Center))
  }
}
