@file:Suppress("DEPRECATION")

package com.example.iptfinal.services

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class AuthServices(private val context: Context) {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val oneTapClient: SignInClient = Identity.getSignInClient(context)

    private val signInRequest: BeginSignInRequest = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId("1048026606726-f1aamm7prssgq41oroqoa5u029e88l2j.apps.googleusercontent.com")
                .setFilterByAuthorizedAccounts(false)
                .build()
        )
        .setAutoSelectEnabled(false)
        .build()


    fun signIn(activity: Activity, launcher: ActivityResultLauncher<IntentSenderRequest>) {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                val intentSender = result.pendingIntent.intentSender
                launcher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }




    fun handleSignInResult(
        data: android.content.Intent?,
        callback: (account: com.google.android.gms.auth.api.identity.SignInCredential?, error: String?) -> Unit
    ) {
        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(data)
            val idToken = credential.googleIdToken
            if (idToken != null) {
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                firebaseAuth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            callback(credential, null)
                        } else {
                            callback(null, task.exception?.message)
                        }
                    }
            } else {
                callback(null, "No token found.")
            }
        } catch (e: Exception) {
            callback(null, e.message)
        }
    }



    fun signUpWithEmail(
        email: String,
        password: String,
        callback: (user: FirebaseUser?, error: String?) -> Unit
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(firebaseAuth.currentUser, null)
                } else {
                    callback(null, task.exception?.message)
                }
            }
    }


    fun signInWithEmail(
        email: String,
        password: String,
        callback: (user: FirebaseUser?, error: String?) -> Unit
    ) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(firebaseAuth.currentUser, null)
                } else {
                    callback(null, task.exception?.message)
                }
            }
    }




    fun signOut() {
        firebaseAuth.signOut()
        oneTapClient.signOut()
    }
}
