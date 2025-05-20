package com.example.projectmanager.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.projectmanager.ui.auth.ForgotPasswordScreen
import com.example.projectmanager.ui.auth.SignInScreen
import com.example.projectmanager.ui.auth.SignUpScreen
import com.example.projectmanager.ui.splash.SplashScreen
import com.example.projectmanager.util.Resource
import com.example.projectmanager.util.SocialAuthHelper

const val AUTH_GRAPH_ROUTE = "auth"
const val SPLASH_ROUTE = "splash"
const val SIGN_IN_ROUTE = "sign_in"
const val SIGN_UP_ROUTE = "sign_up"
const val FORGOT_PASSWORD_ROUTE = "forgot_password"

fun NavGraphBuilder.authNavigation(
    navController: NavController,
    onAuthSuccess: () -> Unit
) {
    navigation(
        startDestination = SPLASH_ROUTE,
        route = AUTH_GRAPH_ROUTE
    ) {
        composable(
            route = SPLASH_ROUTE,
            enterTransition = { EnterTransition.None },
            exitTransition = {
                fadeOut(animationSpec = tween(500))
            }
        ) {
            SplashScreen(
                onSplashComplete = onAuthSuccess,
                onNavigateToSignUp = {
                    navController.navigate(SIGN_UP_ROUTE)
                },
                onNavigateToSignIn = {
                    navController.navigate(SIGN_IN_ROUTE)
                },
                onGoogleSignIn = {
                    // Handle Google Sign-In
                    SocialAuthHelper.signInWithGoogle(navController.context) { result ->
                        if (result is Resource.Success) {
                            onAuthSuccess()
                        } else if (result is Resource.Error) {
                            // Show a toast or snackbar with the error message
                            if (navController.context is android.app.Activity) {
                                android.widget.Toast.makeText(
                                    navController.context, 
                                    "To use Google Sign-In: Configure Firebase Authentication in the console and update the Web Client ID in SocialAuthHelper.kt", 
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                },
                onGitHubSignIn = {
                    // Handle GitHub Sign-In
                    SocialAuthHelper.signInWithGitHub(navController.context) { result ->
                        if (result is Resource.Success) {
                            onAuthSuccess()
                        } else if (result is Resource.Error) {
                            // Show a toast or snackbar with the error message
                            if (navController.context is android.app.Activity) {
                                android.widget.Toast.makeText(
                                    navController.context, 
                                    "To use GitHub Sign-In: Enable GitHub authentication in Firebase Console", 
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            )
        }
        
        composable(
            route = SIGN_IN_ROUTE,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            SignInScreen(
                onSignInSuccess = onAuthSuccess,
                onNavigateToSignUp = {
                    navController.navigate(SIGN_UP_ROUTE) {
                        popUpTo(SIGN_IN_ROUTE) { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = {
                    navController.navigate(FORGOT_PASSWORD_ROUTE)
                }
            )
        }
        
        composable(
            route = SIGN_UP_ROUTE,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            SignUpScreen(
                onSignUpSuccess = onAuthSuccess,
                onNavigateToSignIn = {
                    navController.navigate(SIGN_IN_ROUTE) {
                        popUpTo(SIGN_UP_ROUTE) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = FORGOT_PASSWORD_ROUTE,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
    }
} 