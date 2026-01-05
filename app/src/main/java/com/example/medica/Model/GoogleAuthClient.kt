package com.example.medica.Model

import android.content.Context
import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException

class GoogleAuthClient(private val context: Context) {

    private val auth = Firebase.auth
    private val credentialManager = CredentialManager.Companion.create(context)

    private val WEB_CLIENT_ID = "291840618036-0nsgpsitmujbpn2v1o4u3ots84ejn144.apps.googleusercontent.com"


    suspend fun signIn(): Boolean {
        return try {

            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(WEB_CLIENT_ID)
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )
            handleSignIn(result.credential)

        } catch (e: GetCredentialException) {
            Log.e("GoogleAuth", "Credential error: ${e.message}")
            false
        } catch (e: CancellationException) {
            Log.d("GoogleAuth", "User membatalkan login")
            false
        } catch (e: Exception) {
            Log.e("GoogleAuth", "Login error: ${e.message}")
            false
        }
    }

    private suspend fun handleSignIn(credential: Credential): Boolean {
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {

                val googleIdTokenCredential = GoogleIdTokenCredential.Companion.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

                auth.signInWithCredential(firebaseCredential).await()

                Log.d("GoogleAuth", "Login Firebase Sukses!")
                return true

            } catch (e: Exception) {
                Log.e("GoogleAuth", "Firebase Auth Gagal", e)
                return false
            }
        }
        return false
    }
}