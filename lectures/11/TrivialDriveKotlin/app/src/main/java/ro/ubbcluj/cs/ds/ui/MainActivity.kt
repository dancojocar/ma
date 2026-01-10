package ro.ubbcluj.cs.ds.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ro.ubbcluj.cs.ds.GameViewModel
import ro.ubbcluj.cs.ds.MainActivityViewModel
import ro.ubbcluj.cs.ds.MakePurchaseViewModel
import ro.ubbcluj.cs.ds.R
import ro.ubbcluj.cs.ds.TrivialDriveApplication
import ro.ubbcluj.cs.ds.TrivialDriveRepository
import ro.ubbcluj.cs.ds.ui.theme.TrivialDriveTheme

import androidx.activity.enableEdgeToEdge
import androidx.core.net.toUri

class MainActivity : ComponentActivity() {

  private lateinit var mainActivityViewModel: MainActivityViewModel
  private lateinit var gameViewModel: GameViewModel
  private lateinit var makePurchaseViewModel: MakePurchaseViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val repository = (application as TrivialDriveApplication).appContainer.trivialDriveRepository

    mainActivityViewModel = ViewModelProvider(
      this,
      MainActivityViewModel.MainActivityViewModelFactory(repository)
    )[MainActivityViewModel::class.java]
    gameViewModel = ViewModelProvider(
      this,
      GameViewModel.GameViewModelFactory(repository)
    )[GameViewModel::class.java]
    makePurchaseViewModel = ViewModelProvider(
      this,
      MakePurchaseViewModel.MakePurchaseViewModelFactory(repository)
    )[MakePurchaseViewModel::class.java]

    mainActivityViewModel.messages.observe(this) { resourceId ->
      Toast.makeText(this, getString(resourceId), Toast.LENGTH_SHORT).show()
    }

    lifecycle.addObserver(mainActivityViewModel.billingLifecycleObserver)

    setContent {
      TrivialDriveTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          TrivialDriveApp(gameViewModel, makePurchaseViewModel)
        }
      }
    }
  }
}

@Composable
fun TrivialDriveApp(
  gameViewModel: GameViewModel,
  makePurchaseViewModel: MakePurchaseViewModel
) {
  val navController = rememberNavController()

  NavHost(navController = navController, startDestination = "game") {
    composable("game") {
      GameScreen(gameViewModel, navController)
    }
    composable("purchase") {
      PurchaseScreen(makePurchaseViewModel, navController)
    }
  }
}

@Composable
fun GameScreen(
  gameViewModel: GameViewModel,
  navController: NavController
) {
  val gasUnits by gameViewModel.gasUnitsRemaining.observeAsState(0)
  val isPremium by gameViewModel.isPremium.observeAsState(false)
  val canDrive by gameViewModel.canDrive().observeAsState(false)
  val isDriving by gameViewModel.isDriving.observeAsState(false)
  val odometer by gameViewModel.odometer.observeAsState(0)

  Scaffold { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Header
      Text(
        text = "Trivial Drive",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )

      // Car Display
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        contentAlignment = Alignment.Center
      ) {
        Vehicle(
          gasLevel = gasUnits,
          isPremium = isPremium,
          modifier = Modifier.size(300.dp)
        )
      }

      // Gas Gauge
      GasGauge(
        gasLevel = gasUnits,
        isPremium = isPremium,
        modifier = Modifier
          .size(150.dp)
          .padding(bottom = 16.dp)
      )

      // Odometer
      // Odometer
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = "ODOMETER",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.secondary,
          letterSpacing = 2.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        OdometerDisplay(
          value = odometer.toDouble(),
          modifier = Modifier.padding(bottom = 8.dp)
        )
      }

      // Controls
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
      ) {
        Button(
          onClick = { gameViewModel.startDriving() },
          enabled = canDrive
        ) {
          Text("AutoPilot")
        }
        Button(
          onClick = { gameViewModel.startManualDriving() },
          enabled = canDrive,
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
          Text("Manual")
        }
        Button(
          onClick = { navController.navigate("purchase") }
        ) {
          Text("Store")
        }
      }
    }

    if (isDriving) {
      DrivingOverlay(
        isPremium = isPremium,
        currentOdometer = odometer,
        onAnimationFinished = {
          gameViewModel.finishDriving()
        }
      )
    }

    val isManualDriving by gameViewModel.isManualDriving.observeAsState(false)
    val manualTime by gameViewModel.manualTimeRemaining.observeAsState(0)

    if (isManualDriving) {
      ManualDrivingOverlay(
        isPremium = isPremium,
        currentOdometer = odometer,
        timeRemaining = manualTime,
        onFinish = { gameViewModel.finishManualDriving() }
      )
    }
  }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseScreen(
  viewModel: MakePurchaseViewModel,
  navController: NavController
) {
  val context = androidx.compose.ui.platform.LocalContext.current
  val activity = context as? android.app.Activity

  // Inventory Data
  val gasTitle by viewModel.getSkuDetails(TrivialDriveRepository.SKU_GAS).title.observeAsState("")
  val gasPrice by viewModel.getSkuDetails(TrivialDriveRepository.SKU_GAS).price.observeAsState("")
  val premiumTitle by viewModel.getSkuDetails(TrivialDriveRepository.SKU_PREMIUM).title.observeAsState(
    ""
  )
  val premiumPrice by viewModel.getSkuDetails(TrivialDriveRepository.SKU_PREMIUM).price.observeAsState(
    ""
  )
  val infiniteGasMonthlyTitle by viewModel.getSkuDetails(TrivialDriveRepository.SKU_INFINITE_GAS_MONTHLY).title.observeAsState(
    ""
  )
  val infiniteGasMonthlyPrice by viewModel.getSkuDetails(TrivialDriveRepository.SKU_INFINITE_GAS_MONTHLY).price.observeAsState(
    ""
  )
  val infiniteGasYearlyTitle by viewModel.getSkuDetails(TrivialDriveRepository.SKU_INFINITE_GAS_YEARLY).title.observeAsState(
    ""
  )
  val infiniteGasYearlyPrice by viewModel.getSkuDetails(TrivialDriveRepository.SKU_INFINITE_GAS_YEARLY).price.observeAsState(
    ""
  )

  val isPremiumPurchased by viewModel.isPurchased(TrivialDriveRepository.SKU_PREMIUM)
    .observeAsState(false)
  val isInfiniteGasMonthlyPurchased by viewModel.isPurchased(TrivialDriveRepository.SKU_INFINITE_GAS_MONTHLY)
    .observeAsState(false)
  val isInfiniteGasYearlyPurchased by viewModel.isPurchased(TrivialDriveRepository.SKU_INFINITE_GAS_YEARLY)
    .observeAsState(false)

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = { Text("Store") },
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(
              painterResource(android.R.drawable.ic_menu_close_clear_cancel),
              contentDescription = "Back"
            ) // Placeholder icon
          }
        }
      )
    }
  ) { innerPadding ->
    LazyColumn(
      contentPadding = innerPadding,
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item {
        Text("Fuel Your Ride", style = MaterialTheme.typography.titleMedium)
      }
      item {
        PurchaseItem(
          title = gasTitle,
          price = gasPrice,
          icon = Icons.Default.LocalGasStation,
          onClick = { activity?.let { viewModel.buySku(it, TrivialDriveRepository.SKU_GAS) } }
        )
      }

      item {
        Text("Go Premium", style = MaterialTheme.typography.titleMedium)
      }
      item {
        PurchaseItem(
          title = premiumTitle,
          price = premiumPrice,
          icon = Icons.Default.ShoppingCart,
          isPurchased = isPremiumPurchased,
          onClick = { activity?.let { viewModel.buySku(it, TrivialDriveRepository.SKU_PREMIUM) } },
          onConsume = { viewModel.consumePremium() }
        )
      }

      item {
        Text("Subscribe", style = MaterialTheme.typography.titleMedium)
      }
      item {
        PurchaseItem(
          title = infiniteGasMonthlyTitle,
          price = infiniteGasMonthlyPrice,
          icon = Icons.Default.LocalGasStation,
          isPurchased = isInfiniteGasMonthlyPurchased,
          onClick = {
            activity?.let {
              viewModel.buySku(
                it,
                TrivialDriveRepository.SKU_INFINITE_GAS_MONTHLY
              )
            }
          },
          onManage = {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
              data =
                "https://play.google.com/store/account/subscriptions?sku=${TrivialDriveRepository.SKU_INFINITE_GAS_MONTHLY}&package=${context.packageName}".toUri()
            }
            context.startActivity(intent)
          },
          onCancel = {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
              data =
                "https://play.google.com/store/account/subscriptions?sku=${TrivialDriveRepository.SKU_INFINITE_GAS_MONTHLY}&package=${context.packageName}".toUri()
            }
            context.startActivity(intent)
          }
        )
      }
      item {
        PurchaseItem(
          title = infiniteGasYearlyTitle,
          price = infiniteGasYearlyPrice,
          icon = Icons.Default.LocalGasStation,
          isPurchased = isInfiniteGasYearlyPurchased,
          onClick = {
            activity?.let {
              viewModel.buySku(
                it,
                TrivialDriveRepository.SKU_INFINITE_GAS_YEARLY
              )
            }
          },
          onManage = {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
              data =
                "https://play.google.com/store/account/subscriptions?sku=${TrivialDriveRepository.SKU_INFINITE_GAS_YEARLY}&package=${context.packageName}".toUri()
            }
            context.startActivity(intent)
          },
          onCancel = {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
              data =
                "https://play.google.com/store/account/subscriptions?sku=${TrivialDriveRepository.SKU_INFINITE_GAS_YEARLY}&package=${context.packageName}".toUri()
            }
            context.startActivity(intent)
          }
        )
      }
    }
  }
}

@Composable
fun PurchaseItem(
  title: String,
  price: String,
  icon: ImageVector,
  isPurchased: Boolean = false,
  onClick: () -> Unit,
  onConsume: (() -> Unit)? = null,
  onManage: (() -> Unit)? = null,
  onCancel: (() -> Unit)? = null
) {
  Card(
    onClick = onClick,
    enabled = !isPurchased,
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier
          .size(40.dp)
          .padding(end = 16.dp)
      )
      Column(
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyLarge
        )
        if (isPurchased) {
          Text(
            text = "Purchased",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }
      if (!isPurchased) {
        Text(
          text = price,
          style = MaterialTheme.typography.labelLarge,
          modifier = Modifier.padding(start = 16.dp)
        )
      } else {
        if (onConsume != null) {
          Button(
            onClick = onConsume,
            modifier = Modifier.padding(start = 16.dp)
          ) {
            Text("Consume")
          }
        }

        if (onManage != null) {
          Button(
            onClick = onManage,
            modifier = Modifier.padding(start = 8.dp)
          ) {
            Text("Manage")
          }
        }

        if (onCancel != null) {
          Button(
            onClick = onCancel,
            modifier = Modifier.padding(start = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
          ) {
            Text("Cancel")
          }
        }
      }
    }
  }
}
