package com.abuhani.agri.ui.home

import androidx.lifecycle.*
import com.abuhani.agri.data.model.Category
import com.abuhani.agri.data.model.Product
import com.abuhani.agri.data.repository.ProductRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(private val repo: ProductRepository) : ViewModel() {

    val categories: StateFlow<List<Category>> = repo.getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<Product>> = repo.getProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        sync()
    }

    fun sync() {
        viewModelScope.launch {
            _isLoading.value = true
            repo.syncCategories()
            repo.syncProducts()
            _isLoading.value = false
        }
    }
}

class HomeViewModelFactory(private val repo: ProductRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HomeViewModel(repo) as T
    }
}
