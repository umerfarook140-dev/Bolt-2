package com.example
 
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ads.AdMobManager
import com.example.data.AppDatabase
import com.example.data.GameRepository
import com.example.model.ScreenState
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.GamePlayScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LevelSelectScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.GameViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AdMobManager.initialize(this)

        val database = AppDatabase.getDatabase(this)
        val repository = GameRepository(database.gameDao())
        val factory = GameViewModelFactory(repository)

        setContent {
            MyApplicationTheme {
                val gameViewModel: GameViewModel = viewModel(factory = factory)
                val uiState by gameViewModel.uiState.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GameNavigationHost(
                        uiState = uiState,
                        viewModel = gameViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun GameNavigationHost(
    uiState: com.example.viewmodel.GameUiState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    when (uiState.screenState) {
        ScreenState.HOME -> {
            HomeScreen(
                uiState = uiState,
                onPlayClick = {
                    viewModel.startLevel(uiState.highestUnlockedLevel)
                },
                onLevelsClick = {
                    viewModel.navigateTo(ScreenState.LEVEL_SELECT)
                },
                onSettingsClick = {
                    viewModel.toggleSettings(true)
                },
                modifier = modifier
            )

            if (uiState.isSettingsOpen) {
                SettingsDialog(
                    isSoundEnabled = uiState.isSoundEnabled,
                    isMusicEnabled = uiState.isMusicEnabled,
                    onSoundToggle = { viewModel.setSoundEnabled(it) },
                    onMusicToggle = { viewModel.setMusicEnabled(it) },
                    onClose = { viewModel.toggleSettings(false) }
                )
            }
        }

        ScreenState.LEVEL_SELECT -> {
            LevelSelectScreen(
                highestUnlockedLevel = uiState.highestUnlockedLevel,
                levelRecords = uiState.levelRecords,
                onLevelSelected = { levelNum ->
                    viewModel.startLevel(levelNum)
                },
                onBackClick = {
                    viewModel.navigateTo(ScreenState.HOME)
                },
                modifier = modifier
            )
        }

        ScreenState.PLAYING -> {
            GamePlayScreen(
                uiState = uiState,
                viewModel = viewModel,
                modifier = modifier
            )
        }
    }
}

