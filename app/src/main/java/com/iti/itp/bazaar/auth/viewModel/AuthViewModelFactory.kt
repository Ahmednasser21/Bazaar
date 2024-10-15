package com.iti.itp.bazaar.auth.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.iti.itp.bazaar.auth.firebase.FirebaseReposatory
import com.iti.itp.bazaar.auth.firebase.IFirebaseReposatory

class AuthViewModelFactory (private val repo : IFirebaseReposatory) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return return if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            AuthViewModel(repo) as T
        } else {
            throw IllegalArgumentException(" class AuthViewModel is not found!")
        }
    }



}