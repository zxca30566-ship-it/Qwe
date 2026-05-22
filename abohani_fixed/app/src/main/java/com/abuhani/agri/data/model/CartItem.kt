package com.abuhani.agri.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey val productId: String,
    val productName: String,
    val price: Double,
    val unit: String,
    val imageUrl: String,
    val quantity: Int
)
