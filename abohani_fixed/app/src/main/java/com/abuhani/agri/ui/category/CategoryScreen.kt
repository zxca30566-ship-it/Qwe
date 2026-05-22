package com.abuhani.agri.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abuhani.agri.ui.cart.CartViewModel
import com.abuhani.agri.ui.home.EmptyState
import com.abuhani.agri.ui.home.HomeViewModel
import com.abuhani.agri.ui.home.ProductCard
import com.abuhani.agri.ui.home.ProductShimmerCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    categoryId: String,
    homeViewModel: HomeViewModel,
    cartViewModel: CartViewModel,
    onProductClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val categories by homeViewModel.categories.collectAsState()
    val allProducts by homeViewModel.products.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val category = categories.find { it.id == categoryId }
    val products = allProducts.filter { it.categoryId == categoryId }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(category?.icon ?: "🌿", fontSize = 22.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            category?.name ?: "القسم",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (isLoading && products.isEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(400.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    userScrollEnabled = false
                ) {
                    items(4) { ProductShimmerCard() }
                }
            } else if (products.isEmpty()) {
                Spacer(Modifier.height(40.dp))
                EmptyState(message = "لا توجد منتجات في هذا القسم حالياً", emoji = "🍃")
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height((((products.size + 1) / 2) * 260 + 20).dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    userScrollEnabled = false
                ) {
                    items(products) { product ->
                        ProductCard(
                            product = product,
                            onClick = { onProductClick(product.id) },
                            onAddToCart = {
                                cartViewModel.addToCart(product, 1)
                                scope.launch {
                                    snackbarHostState.showSnackbar("تمت الإضافة للسلة: ${product.name}")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
