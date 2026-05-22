package com.abuhani.agri

import android.app.Application
import com.abuhani.agri.data.local.AppDatabase
import com.abuhani.agri.data.repository.CartRepository
import com.abuhani.agri.data.repository.ProductRepository

class AboHaniApp : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val productRepository by lazy {
        ProductRepository(
            database.categoryDao(),
            database.productDao(),
            applicationContext
        )
    }
    val cartRepository by lazy { CartRepository(database.cartDao()) }
}
