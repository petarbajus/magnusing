package com.example.magnusing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.magnusing.ui.navigation.AppNav
import com.example.magnusing.ui.theme.MagnusingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MagnusingTheme {
                AppNav()
            }
        }
    }
}
