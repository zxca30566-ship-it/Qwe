package com.abuhani.agri.ui.cart

import androidx.lifecycle.*
import com.abuhani.agri.data.model.CartItem
import com.abuhani.agri.data.model.Product
import com.abuhani.agri.data.repository.CartRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CartViewModel(private val repo: CartRepository) : ViewModel() {

    val cartItems: StateFlow<List<CartItem>> = repo.getCartItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalPrice: StateFlow<Double> = cartItems
        .map { items -> items.sumOf { it.price * it.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalCount: StateFlow<Int> = cartItems
        .map { items -> items.sumOf { it.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun addToCart(product: Product, quantity: Int = 1) {
        viewModelScope.launch {
            val existing = cartItems.value.find { it.productId == product.id }
            if (existing != null) {
                repo.updateQuantity(product.id, existing.quantity + quantity)
            } else {
                repo.addToCart(product, quantity)
            }
        }
    }

    fun updateQuantity(productId: String, quantity: Int) {
        viewModelScope.launch { repo.updateQuantity(productId, quantity) }
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch { repo.removeFromCart(productId) }
    }

    fun clearCart() {
        viewModelScope.launch { repo.clearCart() }
    }
}

class CartViewModelFactory(private val repo: CartRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CartViewModel(repo) as T
    }
}
