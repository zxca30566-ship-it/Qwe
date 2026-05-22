package com.abuhani.agri.data.repository

import android.content.Context
import android.net.Uri
import com.abuhani.agri.data.local.CategoryDao
import com.abuhani.agri.data.local.ProductDao
import com.abuhani.agri.data.model.Category
import com.abuhani.agri.data.model.Product
import com.abuhani.agri.utils.ImageUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ProductRepository(
    private val categoryDao: CategoryDao,
    private val productDao: ProductDao,
    private val context: Context
) {
    private val db: FirebaseFirestore = Firebase.firestore
    private val storage: FirebaseStorage = Firebase.storage

    init {
        db.firestoreSettings = com.google.firebase.firestore.firestoreSettings {
            isPersistenceEnabled = true
        }
    }

    // ─── Categories ───────────────────────────────────────────────────────────

    fun getCategories(): Flow<List<Category>> = categoryDao.getAll()

    suspend fun syncCategories() {
        try {
            val snapshot = db.collection("categories").get().await()
            val categories = snapshot.documents.mapNotNull { doc ->
                Category(
                    id = doc.id,
                    name = doc.getString("name") ?: return@mapNotNull null,
                    icon = doc.getString("icon") ?: "🌿",
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            }
            categoryDao.upsertAll(categories)
        } catch (_: Exception) {}
    }

    suspend fun addCategory(name: String, icon: String): String {
        val id = UUID.randomUUID().toString()
        val data = hashMapOf("name" to name, "icon" to icon, "createdAt" to System.currentTimeMillis())
        db.collection("categories").document(id).set(data).await()
        categoryDao.upsert(Category(id, name, icon))
        return id
    }

    suspend fun updateCategory(id: String, name: String, icon: String) {
        val data = mapOf("name" to name, "icon" to icon)
        db.collection("categories").document(id).update(data).await()
        categoryDao.upsert(Category(id, name, icon))
    }

    suspend fun deleteCategory(id: String) {
        db.collection("categories").document(id).delete().await()
        categoryDao.delete(id)
    }

    // ─── Products ─────────────────────────────────────────────────────────────

    fun getProducts(): Flow<List<Product>> = productDao.getAll()

    fun getProductsByCategory(categoryId: String): Flow<List<Product>> =
        productDao.getByCategory(categoryId)

    suspend fun getProduct(id: String): Product? = productDao.getById(id)

    suspend fun syncProducts() {
        try {
            val snapshot = db.collection("products").get().await()
            val products = snapshot.documents.mapNotNull { doc ->
                Product(
                    id = doc.id,
                    name = doc.getString("name") ?: return@mapNotNull null,
                    description = doc.getString("description") ?: "",
                    price = doc.getDouble("price") ?: 0.0,
                    unit = doc.getString("unit") ?: "كيلو",
                    imageUrl = doc.getString("imageUrl") ?: "",
                    images = doc.getString("images") ?: "[]",
                    categoryId = doc.getString("categoryId") ?: "",
                    categoryName = doc.getString("categoryName") ?: "",
                    inStock = doc.getBoolean("inStock") ?: true,
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            }
            productDao.upsertAll(products)
        } catch (_: Exception) {}
    }

    /**
     * رفع صورة واحدة مع ضغطها أولاً
     */
    suspend fun uploadImage(uri: Uri): String {
        val compressed = ImageUtils.compressImage(context, uri, maxSizeKb = 600)
        val ref = storage.reference.child("products/${UUID.randomUUID()}.jpg")
        ref.putBytes(compressed).await()
        return ref.downloadUrl.await().toString()
    }

    /**
     * رفع عدة صور معاً مع ضغطها أولاً
     */
    suspend fun uploadImages(uris: List<Uri>): List<String> {
        return uris.map { uri ->
            val compressed = ImageUtils.compressImage(context, uri, maxSizeKb = 600)
            val ref = storage.reference.child("products/${UUID.randomUUID()}.jpg")
            ref.putBytes(compressed).await()
            ref.downloadUrl.await().toString()
        }
    }

    suspend fun addProduct(product: Product): String {
        val id = UUID.randomUUID().toString()
        val p = product.copy(id = id, createdAt = System.currentTimeMillis())
        db.collection("products").document(id).set(productToMap(p)).await()
        productDao.upsert(p)
        return id
    }

    suspend fun updateProduct(product: Product) {
        db.collection("products").document(product.id).set(productToMap(product)).await()
        productDao.upsert(product)
    }

    suspend fun deleteProduct(id: String) {
        db.collection("products").document(id).delete().await()
        productDao.delete(id)
    }

    private fun productToMap(p: Product): Map<String, Any> = mapOf(
        "name" to p.name,
        "description" to p.description,
        "price" to p.price,
        "unit" to p.unit,
        "imageUrl" to p.imageUrl,
        "images" to p.images,
        "categoryId" to p.categoryId,
        "categoryName" to p.categoryName,
        "inStock" to p.inStock,
        "createdAt" to p.createdAt
    )
}
