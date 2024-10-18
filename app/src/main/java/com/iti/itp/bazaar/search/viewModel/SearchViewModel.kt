package com.iti.itp.bazaar.search.viewModel

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

class SearchViewModel (private val repo: Repository) : ViewModel() {

    private val _searchStateFlow = MutableStateFlow<DataState>(DataState.Loading)
    val searchStateFlow = _searchStateFlow.asStateFlow()

    fun getAllProducts() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getAllProuducts ()
                .catch {e->
                    _searchStateFlow.value = DataState.OnFailed(e)
                    Log.d("TAG", "getAllProuudcts VIEW MODEL: ${e.printStackTrace()}")
                }
                .collectLatest{
                    _searchStateFlow.value = DataState.OnSuccess(it)
                    Log.d("TAG", "getAllProuudcts VIEW MODEL: CASE SUCCESS")
                }
        }
    }
}