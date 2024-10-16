package com.iti.itp.bazaar.mainActivity.ui.categories

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

class CategoriesViewModel(private val repo: Repository) : ViewModel() {

    private val _categoryProductStateFlow = MutableStateFlow<DataState>(DataState.Loading)
    val categoryProductStateFlow = _categoryProductStateFlow.asStateFlow()

    fun getCategoryProducts(categoryID: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getCollectionProducts(categoryID)
                .catch {e->
                    _categoryProductStateFlow.value = DataState.OnFailed(e)
                }
                .collectLatest{
                    _categoryProductStateFlow.value = DataState.OnSuccess(it)
                }
        }
    }
}