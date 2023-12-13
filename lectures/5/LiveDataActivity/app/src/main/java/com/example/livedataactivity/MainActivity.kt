package com.example.livedataactivity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.example.livedataactivity.core.Person
import com.example.livedataactivity.core.getSuperheroList
import com.example.livedataactivity.image.NetworkImageComponentPicasso
import com.example.livedataactivity.state.SuperheroesViewModel

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val viewModel = ViewModelProvider(this)[SuperheroesViewModel::class.java]

    setContent {
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LiveDataComponentList(personList: List<Person>) {
  LazyColumn {
    items(
      items = personList
    ) { person ->
      Card(
        shape = RoundedCornerShape(4.dp),
        backgroundColor = Color.White,
        modifier = Modifier.fillParentMaxWidth().padding(8.dp)
      ) {
        ListItem(text = {
          Text(
            text = person.name,
            style = TextStyle(
              fontFamily = FontFamily.Serif, fontSize = 25.sp,
              fontWeight = FontWeight.Bold
            )
          )
        }, secondaryText = {
          Text(
            text = "Age: ${person.age}",
            style = TextStyle(
              fontFamily = FontFamily.Serif, fontSize = 15.sp,
              fontWeight = FontWeight.Light, color = Color.DarkGray
            )
          )
        }, icon = {
          person.profilePictureUrl?.let { imageUrl ->
            NetworkImageComponentPicasso(
              url = imageUrl,
              modifier = Modifier.width(60.dp).height(60.dp)
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
    CircularProgressIndicator(modifier = Modifier.wrapContentWidth(CenterHorizontally))
  }
}
