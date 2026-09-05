package com.example.grocerydeliveryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.platform.LocalContext

data class Product(
    val name: String,
    val price: Int,
    val emoji: String,
    val description: String
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GroceryApp()
        }
    }
}

@Composable
fun GroceryApp() {

    var screen by remember { mutableStateOf("login") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    when (screen) {

        "login" -> LoginScreen(
            onLogin = { screen = "home" },
            onRegisterClick = { screen = "register" }
        )

        "register" -> RegisterScreen(
            onRegister = { screen = "home" },
            onBackToLogin = { screen = "login" }
        )

        "home" -> HomeScreen(
            onProductClick = {
                selectedProduct = it
                screen = "details"
            },
            onProfileClick = {
                screen = "profile"
            }
        )

        "profile" -> ProfileScreen(
            onBack = {
                screen = "home"
            },
            onLogout = {
                screen = "login"
            },
            onOrderHistory = {
                screen = "orders"
            },
            onAddressClick = {
                screen = "address"
            },
        )
        "orders" -> OrderHistoryScreen()
        "address" -> AddressScreen()

        "details" -> {
            selectedProduct?.let {
                ProductDetailsScreen(
                    product = it,
                    onBack = { screen = "home" },
                    onAddToCart = { screen = "cart" }
                )
            }
        }

        "cart" -> CartScreen(
            onBack = { screen = "home" },
            onCheckout = { screen = "checkout" }
        )

        "checkout" -> CheckoutScreen(
            onOrderPlaced = { screen = "order" }
        )

        "order" -> OrderScreen(
            onHome = { screen = "home" }
        )
    }
}

@Composable
fun LoginScreen(
    onLogin: () -> Unit,
    onRegisterClick: () -> Unit
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text("🛒 Grocery App", fontSize = 30.sp)

        Spacer(modifier = Modifier.height(12.dp))

        Text("Welcome Back!", fontSize = 22.sp)

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onLogin()
                        } else {
                            Toast.makeText(
                                context,
                                task.exception?.message ?: "Login failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("LOGIN")
        }

        Spacer(modifier = Modifier.height(15.dp))

        TextButton(onClick = onRegisterClick) {
            Text("Don't have an account? Register")
        }
    }
}

@Composable
fun RegisterScreen(
    onRegister: () -> Unit,
    onBackToLogin: () -> Unit
) {

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text("🛒 Create Account", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (password != confirmPassword) {
                    Toast.makeText(
                        context,
                        "Passwords do not match",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = FirebaseAuth.getInstance().currentUser?.uid

                                if (userId != null) {
                                    val userData = hashMapOf(
                                        "name" to name,
                                        "email" to email
                                    )

                                    FirebaseFirestore.getInstance()
                                        .collection("users")
                                        .document(userId)
                                        .set(userData)
                                        .addOnSuccessListener {
                                            onRegister()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                context,
                                                "Failed to save user data",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    task.exception?.message ?: "Registration failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("REGISTER")
        }

        TextButton(onClick = onBackToLogin) {
            Text("Already have an account? Login")
        }
    }
}

@Composable
fun HomeScreen(
    onProductClick: (Product) -> Unit,
    onProfileClick: () -> Unit
){

    var searchText by remember { mutableStateOf("") }

    val products = listOf(
        Product("Apple", 120, "🍎", "Fresh and healthy apples."),
        Product("Milk", 60, "🥛", "Fresh dairy milk."),
        Product("Bread", 45, "🍞", "Soft and fresh bakery bread."),
        Product("Banana", 50, "🍌", "Fresh yellow bananas."),
        Product("Biscuits", 30, "🍪", "Crunchy tasty biscuits."),
        Product("Juice", 80, "🧃", "Refreshing fruit juice.")
    )

    val filteredProducts = products.filter {
        it.name.contains(searchText, ignoreCase = true)
    }

    Scaffold(
topBar = {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = "🛒 Grocery App",
            fontSize = 24.sp
        )

        TextButton(
            onClick = onProfileClick
        ) {
            Text("👤 Profile")
        }
    }
}
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {

            item {

                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("🔍 Search groceries") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text("Categories", fontSize = 22.sp)

                Spacer(modifier = Modifier.height(10.dp))

                Text("🍎 Fruits & Vegetables")
                Text("🥛 Dairy Products")
                Text("🍪 Snacks")
                Text("🥤 Beverages")
                Text("🍞 Bakery")
                Text("🏠 Household Items")

                Spacer(modifier = Modifier.height(25.dp))

                Text("Featured Products", fontSize = 22.sp)

                Spacer(modifier = Modifier.height(10.dp))
            }

            items(filteredProducts) { product ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable {
                            onProductClick(product)
                        }
                ) {

                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = product.emoji,
                            fontSize = 40.sp
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(product.name, fontSize = 19.sp)
                            Text("₹${product.price}", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductDetailsScreen(
    product: Product,
    onBack: () -> Unit,
    onAddToCart: () -> Unit
) {

    var quantity by remember { mutableStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        TextButton(onClick = onBack) {
            Text("← Back")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = product.emoji,
            fontSize = 100.sp
        )

        Text(
            text = product.name,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "₹${product.price}",
            fontSize = 22.sp
        )

        Text(
            text = "10% Discount",
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = product.description,
            fontSize = 17.sp
        )

        Spacer(modifier = Modifier.height(25.dp))

        Text("Quantity", fontSize = 18.sp)

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Button(
                onClick = {
                    if (quantity > 1) quantity--
                }
            ) {
                Text("-")
            }

            Text(
                text = "  $quantity  ",
                fontSize = 20.sp
            )

            Button(
                onClick = {
                    quantity++
                }
            ) {
                Text("+")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        TextButton(onClick = {}) {
            Text("❤️ Add to Wishlist")
        }

        Button(
            onClick = onAddToCart,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ADD TO CART")
        }
    }
}
@Composable
fun CartScreen(
    onBack: () -> Unit,
    onCheckout: () -> Unit
) {

    var quantity by remember { mutableStateOf(1) }

    val price = 120
    val total = price * quantity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        TextButton(onClick = onBack) {
            Text("← Continue Shopping")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "🛒 My Cart",
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "🍎 Apple",
                    fontSize = 22.sp
                )

                Text(
                    text = "₹120 per item",
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(15.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Button(
                        onClick = {
                            if (quantity > 1) {
                                quantity--
                            }
                        }
                    ) {
                        Text("-")
                    }

                    Text(
                        text = "  $quantity  ",
                        fontSize = 20.sp
                    )

                    Button(
                        onClick = {
                            quantity++
                        }
                    ) {
                        Text("+")
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))

                Text(
                    text = "Item Total: ₹$total",
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = {
                        quantity = 0
                    }
                ) {
                    Text("Remove Product")
                }
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        Text(
            text = "Total Price: ₹$total",
            fontSize = 23.sp
        )

        Spacer(modifier = Modifier.height(25.dp))

        Button(
            onClick = onCheckout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("CHECKOUT")
        }
    }
}
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onOrderHistory: () -> Unit,
    onAddressClick: () -> Unit
) {

    var name by remember { mutableStateOf("Vyshnavi") }
    var email by remember { mutableStateOf("vyshnavi@gmail.com") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        TextButton(onClick = onBack) {
            Text("← Back to Home")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "👤 My Profile",
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(25.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("UPDATE PROFILE")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onAddressClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📍 Delivery Addresses")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onOrderHistory,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📦 Order History")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("❤️ My Wishlist")
        }

        Spacer(modifier = Modifier.height(25.dp))

        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("LOGOUT")
        }
    }
}
@Composable
fun CheckoutScreen(
    onOrderPlaced: () -> Unit
) {

    var address by remember {
        mutableStateOf("Khammam, Telangana")
    }

    var paymentMethod by remember {
        mutableStateOf("Cash on Delivery")
    }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            text = "Checkout",
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(25.dp))

        Text(
            text = "Delivery Address",
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = address,
            onValueChange = {
                address = it
            },
            label = {
                Text("Address")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(25.dp))

        Text(
            text = "Payment Method",
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = paymentMethod == "Cash on Delivery",
                onClick = {
                    paymentMethod = "Cash on Delivery"
                }
            )

            Text("Cash on Delivery")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = paymentMethod == "UPI",
                onClick = {
                    paymentMethod = "UPI"
                }
            )

            Text("UPI")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = paymentMethod == "Credit/Debit Card",
                onClick = {
                    paymentMethod = "Credit/Debit Card"
                }
            )

            Text("Credit/Debit Card")
        }

        Spacer(modifier = Modifier.height(25.dp))

        Text(
            text = "Order Summary",
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text("🍎 Apple       ₹120")
        Text("Quantity       1")

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Total: ₹120",
            fontSize = 22.sp
        )

        Spacer(modifier = Modifier.height(25.dp))

        Button(
            onClick = {
                val userId = FirebaseAuth.getInstance().currentUser?.uid

                if (userId != null) {
                    val orderData = hashMapOf(
                        "address" to address,
                        "paymentMethod" to paymentMethod,
                        "total" to 120,
                        "status" to "Order Confirmed"
                    )

                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .collection("orders")
                        .add(orderData)
                        .addOnSuccessListener {
                            onOrderPlaced()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                context,
                                "Order save failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ){
            Text("CONFIRM ORDER")
        }
    }
}
@Composable
fun OrderScreen(
    onHome: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text("✅ Order Confirmed!", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(25.dp))

        Text("Order #1001", fontSize = 20.sp)

        Spacer(modifier = Modifier.height(20.dp))

        Text("✓ Order Confirmed")
        Text("↓")
        Text("✓ Preparing")
        Text("↓")
        Text("● Out for Delivery")
        Text("↓")
        Text("○ Delivered")

        Spacer(modifier = Modifier.height(30.dp))

        Button(onClick = onHome) {
            Text("BACK TO HOME")
        }
    }
}
@Composable
fun OrderHistoryScreen() {
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "📦 Order History",
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(25.dp))

        Text(
            text = "Your orders are saved in Firebase Firestore.",
            fontSize = 17.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Order #1001",
            fontSize = 20.sp
        )

        Text(
            text = "Status: Order Confirmed",
            fontSize = 17.sp
        )

        Text(
            text = "Total: ₹120",
            fontSize = 17.sp
        )
    }
}
@Composable
fun AddressScreen() {
    val context = LocalContext.current
    var newAddress by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "📍 Delivery Address",
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(25.dp))

        Text(
            text = "Current Address",
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Khammam, Telangana",
            fontSize = 18.sp
        )
        OutlinedTextField(
            value = newAddress,
            onValueChange = { newAddress = it },
            label = { Text("Enter New Address") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Spacer(modifier = Modifier.height(25.dp))

        Button(
            onClick = {
                val userId = FirebaseAuth.getInstance().currentUser?.uid

                if (userId != null && newAddress.isNotBlank()) {

                    val addressData = hashMapOf(
                        "address" to newAddress
                    )

                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .collection("addresses")
                        .add(addressData)
                        .addOnSuccessListener {
                            Toast.makeText(
                                context,
                                "Address saved successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                context,
                                "Address save failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ADD NEW ADDRESS")
        }
    }
}