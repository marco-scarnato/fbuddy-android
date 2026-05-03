package com.oxygen.finance_buddy

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.oxygen.finance_buddy.ui.navigation.FinanceBuddyNavGraph
import com.oxygen.finance_buddy.ui.theme.FinclearTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.fragment.app.FragmentActivity

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinclearTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        FinanceBuddyNavGraph()
                    }
                }
            }
        }
    }
}