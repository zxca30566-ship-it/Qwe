package com.abuhani.agri.data.repository

import com.abuhani.agri.data.local.CartDao
import com.abuhani.agri.data.model.CartItem
import com.abuhani.agri.data.model.Product
import kotlinx.coroutines.flow.Flow

class CartRepository(private val cartDao: CartDao) {

    fun getCartItems(): Flow<List<CartItem>> = cartDao.getAll()

    suspend fun addToCart(product: Product, quantity: Int) {
        cartDao.upsert(
            CartItem(
                productId = product.id,
                productName = product.name,
                price = product.price,
                unit = product.unit,
                imageUrl = product.imageUrl,
                quantity = quantity
            )
        )
    }

    suspend fun updateQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            cartDao.delete(productId)
        } else {
            cartDao.updateQuantity(productId, quantity)
        }
    }

    suspend fun removeFromCart(productId: String) {
        cartDao.delete(productId)
    }

    suspend fun clearCart() {
        cartDao.deleteAll()
    }
}
