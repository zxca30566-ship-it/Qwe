package com.abuhani.agri.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val icon: String = "🌿",
    val createdAt: Long = System.currentTimeMillis()
)
