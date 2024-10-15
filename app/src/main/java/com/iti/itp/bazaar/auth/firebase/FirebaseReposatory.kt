package com.iti.itp.bazaar.auth.firebase

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser

class FirebaseReposatory (private val firebaseRemotDataSource: IFirebaseRemotDataSource) :
    IFirebaseReposatory {


    companion object {
        private var instance: FirebaseReposatory? = null
        fun getInstance(
            firebaseRemotDataSource: FirebaseRemotDataSource
        ): FirebaseReposatory {
            return instance ?: synchronized(this) {
                val temp = FirebaseReposatory(firebaseRemotDataSource)
                instance = temp
                temp
            }
        }
    }


    override fun signUp (email: String, password: String): Task<AuthResult> {
      return  firebaseRemotDataSource.signUp(email,password)
    }
    override fun sendVerificationEmail(user: FirebaseUser?): Task<Void>? {
      return  firebaseRemotDataSource.sendVerificationEmail(user)
    }

    override fun logIn(email: String, password: String): Task<AuthResult> {
        return firebaseRemotDataSource.logIn(email, password)
    }
    override fun checkIfEmailVerified(): FirebaseUser? {
        return firebaseRemotDataSource.checkIfEmailVerified()
    }

}