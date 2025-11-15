package com.vamsi.worldcountriesinformation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.vamsi.snapnotify.SnapNotifyProvider
import com.vamsi.worldcountriesinformation.core.designsystem.WorldCountriesTheme
import com.vamsi.worldcountriesinformation.ui.compose.navigation.WorldCountriesNavigation
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main entry point for the World Countries Information application.
 * Sets up Compose UI, navigation, and theme with Hilt dependency injection.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WorldCountriesTheme {
                SnapNotifyProvider {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        WorldCountriesNavigation(navController = navController)
                    }
                }
            }
        }
    }
}
