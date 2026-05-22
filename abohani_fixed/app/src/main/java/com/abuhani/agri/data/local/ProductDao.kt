package com.abuhani.agri.data.local

import androidx.room.*
import com.abuhani.agri.data.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE categoryId = :categoryId ORDER BY createdAt DESC")
    fun getByCategory(categoryId: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Product?

    @Upsert
    suspend fun upsertAll(products: List<Product>)

    @Upsert
    suspend fun upsert(product: Product)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM products")
    suspend fun deleteAll()
}
