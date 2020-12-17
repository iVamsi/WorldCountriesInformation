package com.vamsi.worldcountriesinformation.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.vamsi.worldcountriesinformation.R
import com.vamsi.worldcountriesinformation.core.extensions.inTransaction
import com.vamsi.worldcountriesinformation.ui.countries.CountriesFragment
import dagger.hilt.android.AndroidEntryPoint

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