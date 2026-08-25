package com.caiji.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.caiji.app.ui.CaiJiApp
import com.caiji.app.ui.theme.CaiJiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CaiJiTheme {
                CaiJiApp()
            }
        }
    }
}
