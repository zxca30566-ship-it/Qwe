package com.abuhani.agri.ui.admin

import android.net.Uri
import androidx.lifecycle.*
import com.abuhani.agri.data.model.Category
import com.abuhani.agri.data.model.Product
import com.abuhani.agri.data.repository.ProductRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private const val ADMIN_PASSWORD = "ZIMAHOTS@772487373.781268327"

data class ImageUploadState(
    val isUploading: Boolean = false,
    val uploadedUrls: List<String> = emptyList(),
    val errorMessage: String? = null,
    val progress: Float = 0f
)

class AdminViewModel(private val repo: ProductRepository) : ViewModel() {

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _authError = MutableStateFlow(false)
    val authError: StateFlow<Boolean> = _authError.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _imageUploadState = MutableStateFlow(ImageUploadState())
    val imageUploadState: StateFlow<ImageUploadState> = _imageUploadState.asStateFlow()

    // للتوافق مع الكود الحالي
    val uploadedImageUrl: StateFlow<String> = _imageUploadState
        .map { it.uploadedUrls.firstOrNull() ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val categories: StateFlow<List<Category>> = repo.getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<Product>> = repo.getProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun login(password: String) {
        if (password == ADMIN_PASSWORD) {
            _isAuthenticated.value = true
            _authError.value = false
        } else {
            _authError.value = true
        }
    }

    /**
     * رفع صورة واحدة مع الضغط التلقائي
     */
    fun uploadImage(uri: Uri) {
        viewModelScope.launch {
            _imageUploadState.value = ImageUploadState(isUploading = true)
            try {
                val url = repo.uploadImage(uri)
                _imageUploadState.value = ImageUploadState(
                    isUploading = false,
                    uploadedUrls = listOf(url)
                )
            } catch (e: Exception) {
                _imageUploadState.value = ImageUploadState(
                    isUploading = false,
                    errorMessage = "فشل رفع الصورة: ${e.localizedMessage}"
                )
            }
        }
    }

    /**
     * رفع عدة صور دفعة واحدة
     */
    fun uploadMultipleImages(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _imageUploadState.value = ImageUploadState(isUploading = true, progress = 0f)
            try {
                val uploaded = mutableListOf<String>()
                uris.forEachIndexed { index, uri ->
                    val url = repo.uploadImage(uri)
                    uploaded.add(url)
                    _imageUploadState.value = ImageUploadState(
                        isUploading = true,
                        uploadedUrls = uploaded.toList(),
                        progress = (index + 1f) / uris.size
                    )
                }
                _imageUploadState.value = ImageUploadState(
                    isUploading = false,
                    uploadedUrls = uploaded,
                    progress = 1f
                )
            } catch (e: Exception) {
                _imageUploadState.value = ImageUploadState(
                    isUploading = false,
                    errorMessage = "فشل رفع الصور: ${e.localizedMessage}"
                )
            }
        }
    }

    /**
     * إضافة صورة من URI إلى القائمة الحالية
     */
    fun addImageToProduct(uri: Uri) {
        viewModelScope.launch {
            val current = _imageUploadState.value.uploadedUrls.toMutableList()
            _imageUploadState.value = _imageUploadState.value.copy(isUploading = true)
            try {
                val url = repo.uploadImage(uri)
                current.add(url)
                _imageUploadState.value = ImageUploadState(
                    isUploading = false,
                    uploadedUrls = current
                )
            } catch (e: Exception) {
                _imageUploadState.value = _imageUploadState.value.copy(
                    isUploading = false,
                    errorMessage = "فشل رفع الصورة"
                )
            }
        }
    }

    /**
     * حذف صورة من القائمة
     */
    fun removeUploadedImage(url: String) {
        val current = _imageUploadState.value.uploadedUrls.toMutableList()
        current.remove(url)
        _imageUploadState.value = _imageUploadState.value.copy(uploadedUrls = current)
    }

    /**
     * تعيين صور مسبقة (عند التعديل)
     */
    fun setExistingImages(urls: List<String>) {
        _imageUploadState.value = ImageUploadState(uploadedUrls = urls)
    }

    fun clearImageUrl() {
        _imageUploadState.value = ImageUploadState()
    }

    fun clearAuthError() { _authError.value = false }

    fun addCategory(name: String, icon: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try { repo.addCategory(name, icon) } catch (_: Exception) {}
            _isLoading.value = false
        }
    }

    fun updateCategory(id: String, name: String, icon: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try { repo.updateCategory(id, name, icon) } catch (_: Exception) {}
            _isLoading.value = false
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            try { repo.deleteCategory(id) } catch (_: Exception) {}
        }
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            _isLoading.value = true
            try { repo.addProduct(product) } catch (_: Exception) {}
            _isLoading.value = false
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            _isLoading.value = true
            try { repo.updateProduct(product) } catch (_: Exception) {}
            _isLoading.value = false
        }
    }

    fun deleteProduct(id: String) {
        viewModelScope.launch {
            try { repo.deleteProduct(id) } catch (_: Exception) {}
        }
    }
}

class AdminViewModelFactory(private val repo: ProductRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AdminViewModel(repo) as T
    }
}
