package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.data.database.AppDatabase
import com.example.data.repository.HistoryRepository
import com.example.ui.screens.MainCalculatriceApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Instantiate local Room database and construct repository layers offline
    val database = AppDatabase.getDatabase(this)
    val repository = HistoryRepository(database.historyDao())

    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          MainCalculatriceApp(
            repository = repository,
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }
}

