package com.iti.itp.bazaar.auth.viewModel

import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.iti.itp.bazaar.auth.firebase.FirebaseReposatory
import com.iti.itp.bazaar.auth.firebase.IFirebaseReposatory

class AuthViewModel (private val repo : IFirebaseReposatory) : ViewModel() {

    // Sign Up methods
    fun signUp (email: String, password: String): Task<AuthResult> {
        return  repo.signUp(email,password)
    }
    fun sendVerificationEmail(user: FirebaseUser?): Task<Void>? {
        return  repo.sendVerificationEmail(user)
    }
//////////////////////////////////////////////////////////
    //Log In methods
    fun logIn(email: String, password: String): Task<AuthResult> {
        return repo.logIn(email, password)
    }

    fun checkIfEmailVerified(): FirebaseUser? {
        return repo.checkIfEmailVerified()
    }


}