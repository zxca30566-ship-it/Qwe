package com.abuhani.agri.ui.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.abuhani.agri.AboHaniApp
import com.abuhani.agri.ui.admin.AdminScreen
import com.abuhani.agri.ui.admin.AdminViewModelFactory
import com.abuhani.agri.ui.cart.CartScreen
import com.abuhani.agri.ui.cart.CartViewModel
import com.abuhani.agri.ui.cart.CartViewModelFactory
import com.abuhani.agri.ui.category.CategoryScreen
import com.abuhani.agri.ui.home.HomeScreen
import com.abuhani.agri.ui.home.HomeViewModel
import com.abuhani.agri.ui.home.HomeViewModelFactory
import com.abuhani.agri.ui.product.ProductDetailScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Category : Screen("category/{categoryId}") {
        fun createRoute(id: String) = "category/$id"
    }
    object Product : Screen("product/{productId}") {
        fun createRoute(id: String) = "product/$id"
    }
    object Cart : Screen("cart")
    object Admin : Screen("admin")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as AboHaniApp

    val homeVm: HomeViewModel = viewModel(factory = HomeViewModelFactory(app.productRepository))
    val cartVm: CartViewModel = viewModel(factory = CartViewModelFactory(app.cartRepository))

    val cartItems by cartVm.cartItems.collectAsState()
    val cartCount = cartItems.sumOf { it.quantity }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(Screen.Home.route, Screen.Cart.route)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "الرئيسية") },
                        label = { Text("الرئيسية") },
                        selected = currentRoute == Screen.Home.route,
                        onClick = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (cartCount > 0) Badge { Text("$cartCount") }
                                }
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "السلة")
                            }
                        },
                        label = { Text("السلة") },
                        selected = currentRoute == Screen.Cart.route,
                        onClick = {
                            navController.navigate(Screen.Cart.route) {
                                popUpTo(Screen.Home.route)
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn() },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut() }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    homeViewModel = homeVm,
                    cartViewModel = cartVm,
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = onToggleDarkMode,
                    onCategoryClick = { categoryId ->
                        navController.navigate(Screen.Category.createRoute(categoryId))
                    },
                    onProductClick = { productId ->
                        navController.navigate(Screen.Product.createRoute(productId))
                    },
                    onAdminLongPress = {
                        navController.navigate(Screen.Admin.route)
                    }
                )
            }
            composable(
                Screen.Category.route,
                arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
            ) { backStack ->
                val categoryId = backStack.arguments?.getString("categoryId") ?: ""
                CategoryScreen(
                    categoryId = categoryId,
                    homeViewModel = homeVm,
                    cartViewModel = cartVm,
                    onProductClick = { productId ->
                        navController.navigate(Screen.Product.createRoute(productId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                Screen.Product.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStack ->
                val productId = backStack.arguments?.getString("productId") ?: ""
                ProductDetailScreen(
                    productId = productId,
                    productRepository = app.productRepository,
                    cartViewModel = cartVm,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Cart.route) {
                CartScreen(
                    cartViewModel = cartVm,
                    onProductClick = { productId ->
                        navController.navigate(Screen.Product.createRoute(productId))
                    }
                )
            }
            composable(Screen.Admin.route) {
                AdminScreen(
                    viewModel = viewModel(
                        factory = AdminViewModelFactory(
                            app.productRepository
                        )
                    ),
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
