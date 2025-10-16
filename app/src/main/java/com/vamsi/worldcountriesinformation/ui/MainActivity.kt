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
import com.vamsi.worldcountriesinformation.ui.compose.navigation.WorldCountriesNavigation
import com.vamsi.worldcountriesinformation.ui.theme.WorldCountriesTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WorldCountriesTheme {
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

/* Legacy Fragment-based implementation - keeping for reference during migration
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // hide default action bar
        supportActionBar?.hide()

        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.inTransaction {
                add(R.id.fragment_container, CountriesFragment.newInstance())
            }
        }
    }
}
*/