package com.example.sheshield

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.sheshield.app.SheShieldApp  // Correct import
import com.example.sheshield.ui.theme.SheShieldTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SheShieldTheme {
                SheShieldApp()
            }
        }
    }
}