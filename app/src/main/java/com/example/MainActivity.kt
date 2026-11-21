package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.di.SpendWiseContainer
import com.example.ui.SpendWiseApp

class MainActivity : ComponentActivity() {
    private val container: SpendWiseContainer by lazy {
        SpendWiseContainer(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpendWiseApp(container = container)
        }
    }
}
