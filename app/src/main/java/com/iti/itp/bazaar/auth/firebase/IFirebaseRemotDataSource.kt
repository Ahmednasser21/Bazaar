package com.iti.itp.bazaar.auth.firebase

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser

interface IFirebaseRemotDataSource {
    fun signUp(email: String, password: String): Task<AuthResult>
    fun sendVerificationEmail(user: FirebaseUser? /*mAuth.currentUser*/): Task<Void>?
    fun logIn(email: String, password: String): Task<AuthResult>
    fun checkIfEmailVerified(): FirebaseUser?
}