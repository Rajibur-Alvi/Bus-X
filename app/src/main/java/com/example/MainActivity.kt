package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.ui.BusinessXRayMainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BusinessXRayViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BusinessXRayViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Main layout screen taking care of insets padding internally or in Scaffold parent
                    BusinessXRayMainScreen(viewModel)
                }
            }
        }
    }
}
