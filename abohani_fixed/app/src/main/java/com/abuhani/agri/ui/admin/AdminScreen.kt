package com.abuhani.agri.ui.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.abuhani.agri.data.model.Category
import com.abuhani.agri.data.model.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val authError by viewModel.authError.collectAsState()

    if (!isAuthenticated) {
        AdminLoginScreen(
            onLogin = { password -> viewModel.login(password) },
            hasError = authError,
            onErrorDismiss = { viewModel.clearAuthError() },
            onBack = onBack
        )
    } else {
        AdminDashboard(viewModel = viewModel, onBack = onBack)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(
    onLogin: (String) -> Unit,
    hasError: Boolean,
    onErrorDismiss: () -> Unit,
    onBack: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("لوحة التحكم") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null,
                            modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.height(24.dp))
                    Text("لوحة الإدارة", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(24.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; if (hasError) onErrorDismiss() },
                        label = { Text("كلمة المرور") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = hasError,
                        supportingText = if (hasError) {
                            { Text("كلمة المرور غير صحيحة", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null)
                            }
                        },
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { onLogin(password) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("دخول", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(viewModel: AdminViewModel, onBack: () -> Unit) {
    val categories by viewModel.categories.collectAsState()
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val imageUploadState by viewModel.imageUploadState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("لوحة الإدارة")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                    text = { Text("الأقسام") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                    text = { Text("المنتجات") })
            }
            when (selectedTab) {
                0 -> CategoriesTab(
                    categories = categories, isLoading = isLoading,
                    onAdd = { name, icon -> viewModel.addCategory(name, icon) },
                    onUpdate = { id, name, icon -> viewModel.updateCategory(id, name, icon) },
                    onDelete = { id -> viewModel.deleteCategory(id) }
                )
                1 -> ProductsTab(
                    products = products, categories = categories, isLoading = isLoading,
                    imageUploadState = imageUploadState,
                    onUploadImage = { uri -> viewModel.addImageToProduct(uri) },
                    onUploadMultiple = { uris -> viewModel.uploadMultipleImages(uris) },
                    onRemoveImage = { url -> viewModel.removeUploadedImage(url) },
                    onSetExistingImages = { urls -> viewModel.setExistingImages(urls) },
                    onAdd = { product -> viewModel.addProduct(product) },
                    onUpdate = { product -> viewModel.updateProduct(product) },
                    onDelete = { id -> viewModel.deleteProduct(id) },
                    onClearImages = { viewModel.clearImageUrl() }
                )
            }
        }
    }
}

@Composable
fun CategoriesTab(
    categories: List<Category>, isLoading: Boolean,
    onAdd: (String, String) -> Unit, onUpdate: (String, String, String) -> Unit,
    onDelete: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editCategory by remember { mutableStateOf<Category?>(null) }
    var deleteCategory by remember { mutableStateOf<Category?>(null) }

    if (showAddDialog) {
        CategoryDialog(title = "إضافة قسم جديد", onDismiss = { showAddDialog = false },
            onSave = { name, icon -> onAdd(name, icon); showAddDialog = false })
    }
    editCategory?.let { cat ->
        CategoryDialog(title = "تعديل القسم", initialName = cat.name, initialIcon = cat.icon,
            onDismiss = { editCategory = null },
            onSave = { name, icon -> onUpdate(cat.id, name, icon); editCategory = null })
    }
    deleteCategory?.let { cat ->
        AlertDialog(onDismissRequest = { deleteCategory = null },
            title = { Text("تأكيد الحذف") },
            text = { Text("هل تريد حذف قسم \"${cat.name}\"؟") },
            confirmButton = {
                TextButton(onClick = { onDelete(cat.id); deleteCategory = null }) {
                    Text("حذف", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { deleteCategory = null }) { Text("إلغاء") } }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${categories.size} قسم", style = MaterialTheme.typography.bodyMedium)
            Button(onClick = { showAddDialog = true }, shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("إضافة قسم")
            }
        }
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    AdminCategoryItem(category = cat, onEdit = { editCategory = cat },
                        onDelete = { deleteCategory = cat })
                }
            }
        }
    }
}

@Composable
fun AdminCategoryItem(category: Category, onEdit: () -> Unit, onDelete: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(category.icon.ifEmpty { "🌿" }, fontSize = 28.sp)
            Spacer(Modifier.width(12.dp))
            Text(category.name, modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun CategoryDialog(
    title: String, initialName: String = "", initialIcon: String = "🌿",
    onDismiss: () -> Unit, onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var icon by remember { mutableStateOf(initialIcon) }

    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("اسم القسم") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = icon, onValueChange = { icon = it },
                    label = { Text("الأيقونة (إيموجي)") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onSave(name.trim(), icon.trim()) }) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
fun ProductsTab(
    products: List<Product>, categories: List<Category>, isLoading: Boolean,
    imageUploadState: ImageUploadState,
    onUploadImage: (Uri) -> Unit,
    onUploadMultiple: (List<Uri>) -> Unit,
    onRemoveImage: (String) -> Unit,
    onSetExistingImages: (List<String>) -> Unit,
    onAdd: (Product) -> Unit, onUpdate: (Product) -> Unit, onDelete: (String) -> Unit,
    onClearImages: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editProduct by remember { mutableStateOf<Product?>(null) }
    var deleteProduct by remember { mutableStateOf<Product?>(null) }

    if (showAddDialog) {
        ProductDialog(
            title = "إضافة منتج جديد", categories = categories,
            imageUploadState = imageUploadState,
            onUploadImage = onUploadImage, onUploadMultiple = onUploadMultiple,
            onRemoveImage = onRemoveImage,
            onDismiss = { showAddDialog = false; onClearImages() },
            onSave = { product -> onAdd(product); showAddDialog = false; onClearImages() }
        )
    }
    editProduct?.let { prod ->
        val existingImages = try {
            val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
            Gson().fromJson<List<String>>(prod.images, type) ?: emptyList()
        } catch (_: Exception) { if (prod.imageUrl.isNotEmpty()) listOf(prod.imageUrl) else emptyList() }

        LaunchedEffect(prod.id) { onSetExistingImages(existingImages) }

        ProductDialog(
            title = "تعديل المنتج", initialProduct = prod, categories = categories,
            imageUploadState = imageUploadState,
            onUploadImage = onUploadImage, onUploadMultiple = onUploadMultiple,
            onRemoveImage = onRemoveImage,
            onDismiss = { editProduct = null; onClearImages() },
            onSave = { product -> onUpdate(product); editProduct = null; onClearImages() }
        )
    }
    deleteProduct?.let { prod ->
        AlertDialog(onDismissRequest = { deleteProduct = null },
            title = { Text("تأكيد الحذف") },
            text = { Text("هل تريد حذف منتج \"${prod.name}\"؟") },
            confirmButton = {
                TextButton(onClick = { onDelete(prod.id); deleteProduct = null }) {
                    Text("حذف", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { deleteProduct = null }) { Text("إلغاء") } }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${products.size} منتج", style = MaterialTheme.typography.bodyMedium)
            Button(onClick = { onClearImages(); showAddDialog = true }, shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("إضافة منتج")
            }
        }
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(products) { prod ->
                    AdminProductItem(product = prod, onEdit = { editProduct = prod },
                        onDelete = { deleteProduct = prod })
                }
            }
        }
    }
}

@Composable
fun AdminProductItem(product: Product, onEdit: () -> Unit, onDelete: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center) {
                if (product.imageUrl.isNotEmpty()) {
                    AsyncImage(model = product.imageUrl, contentDescription = null,
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else { Text("🌱", fontSize = 24.sp) }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium, maxLines = 1)
                Text("${product.price} ريال / ${product.unit}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                if (product.categoryName.isNotEmpty()) {
                    Text(product.categoryName, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDialog(
    title: String, initialProduct: Product? = null, categories: List<Category>,
    imageUploadState: ImageUploadState,
    onUploadImage: (Uri) -> Unit, onUploadMultiple: (List<Uri>) -> Unit,
    onRemoveImage: (String) -> Unit,
    onDismiss: () -> Unit, onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var description by remember { mutableStateOf(initialProduct?.description ?: "") }
    var price by remember { mutableStateOf(initialProduct?.price?.toString() ?: "") }
    var unit by remember { mutableStateOf(initialProduct?.unit ?: "كيلو") }
    var inStock by remember { mutableStateOf(initialProduct?.inStock ?: true) }
    var selectedCategoryId by remember { mutableStateOf(initialProduct?.categoryId ?: "") }
    var expanded by remember { mutableStateOf(false) }
    val selectedCategory = categories.find { it.id == selectedCategoryId }

    // اختيار صورة واحدة
    val singleImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onUploadImage(it) }
    }
    // اختيار عدة صور
    val multiImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) onUploadMultiple(uris)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // ── قسم الصور ─────────────────────────────────────────
                Text("الصور", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                // مؤشر التحميل
                AnimatedVisibility(visible = imageUploadState.isUploading) {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("جاري رفع الصورة...", style = MaterialTheme.typography.bodySmall)
                            }
                            if (imageUploadState.progress > 0f) {
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { imageUploadState.progress },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // رسالة الخطأ
                imageUploadState.errorMessage?.let { err ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text(err, modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }

                // عرض الصور المرفوعة
                if (imageUploadState.uploadedUrls.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(imageUploadState.uploadedUrls) { url ->
                            Box(modifier = Modifier.size(80.dp)) {
                                AsyncImage(model = url, contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)))
                                // زر حذف الصورة
                                IconButton(
                                    onClick = { onRemoveImage(url) },
                                    modifier = Modifier.size(24.dp).align(Alignment.TopEnd)
                                        .background(MaterialTheme.colorScheme.error, CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "حذف الصورة",
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onError)
                                }
                            }
                        }
                    }
                }

                // أزرار رفع الصور
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { singleImagePicker.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !imageUploadState.isUploading
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("صورة", style = MaterialTheme.typography.labelMedium)
                    }
                    OutlinedButton(
                        onClick = { multiImagePicker.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !imageUploadState.isUploading
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("معرض", style = MaterialTheme.typography.labelMedium)
                    }
                }

                HorizontalDivider()

                // ── حقول المنتج ────────────────────────────────────────
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("اسم المنتج") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = description, onValueChange = { description = it },
                    label = { Text("الوصف") }, modifier = Modifier.fillMaxWidth(),
                    maxLines = 3, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = price, onValueChange = { price = it },
                    label = { Text("السعر") }, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = unit, onValueChange = { unit = it },
                    label = { Text("الوحدة (كيلو، لتر...)") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp))

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = selectedCategory?.let { "${it.icon} ${it.name}" } ?: "اختر القسم",
                        onValueChange = {}, readOnly = true,
                        label = { Text("القسم") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${cat.icon} ${cat.name}") },
                                onClick = { selectedCategoryId = cat.id; expanded = false }
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("متوفر في المخزن", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = inStock, onCheckedChange = { inStock = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val gson = Gson()
                    val imagesJson = gson.toJson(imageUploadState.uploadedUrls)
                    val primaryImage = imageUploadState.uploadedUrls.firstOrNull() ?: ""
                    val p = (initialProduct ?: Product()).copy(
                        name = name.trim(), description = description.trim(),
                        price = price.toDoubleOrNull() ?: 0.0, unit = unit.trim(),
                        imageUrl = primaryImage, images = imagesJson,
                        categoryId = selectedCategoryId,
                        categoryName = selectedCategory?.name ?: "", inStock = inStock
                    )
                    if (p.name.isNotBlank()) onSave(p)
                },
                enabled = !imageUploadState.isUploading
            ) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
