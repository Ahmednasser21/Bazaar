package com.iti.itp.bazaar.auth.firebase

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class FirebaseRemotDataSource ( private val mAuth : FirebaseAuth) : IFirebaseRemotDataSource {

     override fun signUp(email: String, password: String): Task<AuthResult> {
        return mAuth.createUserWithEmailAndPassword(email, password)

    }

     override fun sendVerificationEmail(user: FirebaseUser? /*mAuth.currentUser*/): Task<Void>? {
        return user?.sendEmailVerification()
    }

    override fun logIn(email: String, password: String): Task<AuthResult> {
        return mAuth.signInWithEmailAndPassword(email, password)
    }


    override fun checkIfEmailVerified(): FirebaseUser? {
        return mAuth.currentUser
    }

}