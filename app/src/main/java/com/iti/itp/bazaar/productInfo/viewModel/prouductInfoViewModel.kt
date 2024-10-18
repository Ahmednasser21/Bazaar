package com.example.productinfoform_commerce.productInfo.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class prouductInfoViewModel (private val repo: Repository) : ViewModel() {

    private val _productDetailsStateFlow = MutableStateFlow<DataState>(DataState.Loading)
    val productDetailsStateFlow = _productDetailsStateFlow.asStateFlow()


    fun getProductDetails(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getProductDetails (id)
                .catch {e->
                    _productDetailsStateFlow.value = DataState.OnFailed(e)
                    Log.d("TAG", "getProductDetails VIEW MODEL: ${e.printStackTrace()}")
                }
                .collectLatest{
                    _productDetailsStateFlow.value = DataState.OnSuccess(it)
                    Log.d("TAG", "getProductDetails VIEW MODEL: CASE SUCCESS")
                }
        }
    }

}