package com.senthil.AI

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import com.senthil.AI.ui.SentinelApp

class MainActivity : ComponentActivity() {
    private val destinationState = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        destinationState.value = intent.getStringExtra("destination")
        setContent {
            SentinelApp(initialDestination = destinationState.value)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        destinationState.value = intent?.getStringExtra("destination")
    }
}

