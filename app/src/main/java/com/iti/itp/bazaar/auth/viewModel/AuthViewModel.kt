package com.iti.itp.bazaar.auth.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.iti.itp.bazaar.auth.firebase.FirebaseReposatory
import com.iti.itp.bazaar.auth.firebase.IFirebaseReposatory
import com.iti.itp.bazaar.dto.CustomerRequest
import com.iti.itp.bazaar.dto.UpdateCustomerRequest
import com.iti.itp.bazaar.dto.cutomerResponce.CustomerResponse
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AuthViewModel (private val repo : IFirebaseReposatory , private val repository: Repository) : ViewModel() {

    private val _customerStateFlow = MutableStateFlow<DataState>(DataState.Loading)
    val customerStateFlow = _customerStateFlow.asStateFlow()

    private val _customerByEmailStateFlow = MutableStateFlow<DataState>(DataState.Loading)
    val customerByEmailStateFlow = _customerByEmailStateFlow.asStateFlow()

    private val _updateCustomerByIdStateFlow = MutableStateFlow<DataState>(DataState.Loading)
    val updateCustomerByIdStateFlow = _updateCustomerByIdStateFlow.asStateFlow()


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
    // methode psot id method

    fun postCustomer (customer : CustomerRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.postCustomer (customer)
                .catch {e->
                    _customerStateFlow.value = DataState.OnFailed(e)
                }
                .collectLatest{
                    _customerStateFlow.value = DataState.OnSuccess(it)
                }
        }
    }

    fun getCustomerByEmail (email:String ){

        viewModelScope.launch(Dispatchers.IO) {
            repository.getCustomerByEmail(email).catch {

                _customerByEmailStateFlow.value = DataState.OnFailed(it)
            }
                .collect {
                    _customerByEmailStateFlow.value = DataState.OnSuccess(it)
                }
        }
    }

    fun updateCustomerById( customerId : Long , updateCustomerRequest: UpdateCustomerRequest  ){

        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCustomerById(customerId,updateCustomerRequest).catch {
                _updateCustomerByIdStateFlow.value =DataState.OnFailed(it)

            }.collect {
                _updateCustomerByIdStateFlow.value =DataState.OnSuccess (it)
            }
        }
    }



}