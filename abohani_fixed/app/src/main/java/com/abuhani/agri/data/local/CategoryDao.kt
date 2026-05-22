package com.abuhani.agri.data.local

import androidx.room.*
import com.abuhani.agri.data.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY createdAt ASC")
    fun getAll(): Flow<List<Category>>

    @Upsert
    suspend fun upsertAll(categories: List<Category>)

    @Upsert
    suspend fun upsert(category: Category)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}
