package com.example.projectmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.projectmanager.navigation.AppNavigator
import com.example.projectmanager.navigation.AppNavigatorImpl
import com.example.projectmanager.navigation.MainNavigation
import com.example.projectmanager.ui.theme.ProjectmanagerTheme

import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appNavigator: AppNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjectmanagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    (appNavigator as? AppNavigatorImpl)?.setNavController(navController)

                    MainNavigation(
                        navController = navController,
                        startDestination = if (appNavigator.isUserSignedIn()) {
                            "home"
                        } else {
                            "auth"
                        },
                        appNavigator = appNavigator
                    )
                }
            }
        }
    }
} 