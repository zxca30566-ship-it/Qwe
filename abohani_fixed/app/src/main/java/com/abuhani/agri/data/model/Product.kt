package com.abuhani.agri.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val unit: String = "كيلو",
    val imageUrl: String = "",
    val images: String = "[]",
    val categoryId: String = "",
    val categoryName: String = "",
    val inStock: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
