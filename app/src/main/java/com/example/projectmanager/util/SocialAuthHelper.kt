package com.example.projectmanager.util

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import timber.log.Timber

object SocialAuthHelper {
    
    /**
     * Start Google sign-in flow directly with Google Sign-In API
     * This avoids the FirebaseUI dependency and its associated configuration issues
     */
    fun signInWithGoogle(context: Context, onComplete: (Resource<Unit>) -> Unit) {
        try {
            // Build a GoogleSignInClient with the default app client ID
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("434858540199-c50ggcroqib7itn1ba4sacdkoem1maes.apps.googleusercontent.com") // Replace with your actual Web Client ID from Firebase console
                .requestEmail()
                .build()
                
            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            
            // Get current user status
            val user = FirebaseAuth.getInstance().currentUser
            
            if (user != null) {
                // User is already signed in
                onComplete(Resource.success(Unit))
            } else {
                // Start sign-in flow
                val signInIntent = googleSignInClient.signInIntent
                
                // This is a simplified approach for demo purposes
                // In a real app, you should properly handle the activity result
                (context as? android.app.Activity)?.startActivityForResult(signInIntent, 9001)
                
                // Add a listener to catch the result
                FirebaseAuth.getInstance().addAuthStateListener { auth ->
                    if (auth.currentUser != null) {
                        onComplete(Resource.success(Unit))
                    }
                }
                
                // Display a message for demo purposes
                onComplete(Resource.error("Google Authentication is not fully configured. Please add your Web Client ID."))
            }
        } catch (e: Exception) {
            Timber.e(e, "Google Auth Failure")
            onComplete(Resource.error("Google authentication requires configuration: ${e.message}"))
        }
    }
    
    /**
     * Start GitHub sign-in flow using Firebase directly
     */
    fun signInWithGitHub(context: Context, onComplete: (Resource<Unit>) -> Unit) {
        try {
            val provider = OAuthProvider.newBuilder("github.com")
            
            val firebaseAuth = FirebaseAuth.getInstance()
            val pendingAuthTask = firebaseAuth.pendingAuthResult
            
            if (pendingAuthTask != null) {
                // There's already a pending auth task
                pendingAuthTask
                    .addOnSuccessListener { 
                        Timber.d("GitHub Auth Success") 
                        onComplete(Resource.success(Unit))
                    }
                    .addOnFailureListener { exception ->
                        Timber.e(exception, "GitHub Auth Failure")
                        onComplete(Resource.error("GitHub authentication failed: ${exception.message}"))
                    }
            } else {
                try {
                    // Start new GitHub sign-in flow
                    firebaseAuth
                        .startActivityForSignInWithProvider(context as android.app.Activity, provider.build())
                        .addOnSuccessListener {
                            Timber.d("GitHub Auth Success")
                            onComplete(Resource.success(Unit))
                        }
                        .addOnFailureListener { exception ->
                            Timber.e(exception, "GitHub Auth Failure")
                            onComplete(Resource.error("GitHub authentication failed: ${exception.message}"))
                        }
                } catch (e: Exception) {
                    // Handle specific errors like CONFIGURATION_NOT_FOUND
                    if (e.message?.contains("CONFIGURATION_NOT_FOUND") == true) {
                        onComplete(Resource.error("GitHub authentication is not configured in Firebase Console. Please enable GitHub authentication in the Firebase Console."))
                    } else {
                        onComplete(Resource.error("GitHub authentication failed: ${e.message}"))
                    }
                    Timber.e(e, "GitHub Auth Failure")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "GitHub Auth Failure")
            onComplete(Resource.error("GitHub authentication failed: ${e.message}"))
        }
    }

    /**
     * Handle the activity result from Google Sign-In
     */
    fun handleGoogleSignInResult(data: Intent?, onComplete: (Resource<Unit>) -> Unit) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            
            // Authenticate with Firebase using the Google ID token
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnSuccessListener {
                    onComplete(Resource.success(Unit))
                }
                .addOnFailureListener { e ->
                    onComplete(Resource.error("Firebase authentication failed: ${e.message}"))
                }
        } catch (e: Exception) {
            onComplete(Resource.error("Google sign-in failed: ${e.message}"))
        }
    }
} 