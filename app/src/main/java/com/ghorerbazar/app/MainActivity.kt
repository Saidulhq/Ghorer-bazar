package com.ghorerbazar.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BazarItem(
    val name: String,
    val quantity: Double,
    val unit: String,
    val price: Double,
    val category: String
) {
    val total: Double get() = quantity * price
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GhorerBazarApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GhorerBazarApp() {
    var isDarkMode by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val items = remember {
        mutableStateListOf(
            BazarItem("নাজিরশাইল চাল", 5.0, "কেজি", 75.0, "চাল/ডাল"),
            BazarItem("দেশি আলু", 2.0, "কেজি", 45.0, "সবজি"),
            BazarItem("রুই মাছ", 1.5, "কেজি", 380.0, "মাছ/মাংস")
        )
    }

    MaterialTheme(
        colorScheme = if (isDarkMode) darkColorScheme() else lightColorScheme(primary = Color(0xFF2E7D32))
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Ghorer Bazar", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = { isDarkMode = !isDarkMode }) {
                            Icon(if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode, null)
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = { Icon(Icons.Default.ReceiptLong, null) }, label = { Text("হিসাব") })
                    NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = { Icon(Icons.Default.ShoppingCart, null) }, label = { Text("বাজার") })
                    NavigationBarItem(selected = selectedTab == 2, onClick = { selectedTab = 2 }, icon = { Icon(Icons.Default.List, null) }, label = { Text("ফর্দ") })
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (selectedTab) {
                    0 -> DashboardScreen(items)
                    1 -> BazarScreen(items)
                    2 -> ShoppingListScreen()
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(items: List<BazarItem>) {
    val total = items.sumOf { it.total }
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("আজকের মোট খরচ: ৳%.2f".format(total), fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("সাম্প্রতিক বাজার", fontWeight = FontWeight.Bold)
                    items.forEach { Text("${it.name} - ৳%.2f".format(it.total)) }
                }
            }
        }
    }
}

@Composable
fun BazarScreen(items: MutableList<BazarItem>) {
    var showDialog by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = { showDialog = true }) { Text("নতুন পণ্য যোগ করুন") }
        LazyColumn(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
            itemsIndexed(items) { index, item ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(item.name, fontWeight = FontWeight.Bold)
                            Text("${item.quantity} ${item.unit} × ৳${item.price}")
                        }
                        IconButton(onClick = { items.removeAt(index) }) {
                            Icon(Icons.Default.Delete, null, tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
    if (showDialog) {
        AddItemDialog(onDismiss = { showDialog = false }, onAdd = { items.add(it); showDialog = false })
    }
}

@Composable
fun AddItemDialog(onDismiss: () -> Unit, onAdd: (BazarItem) -> Unit) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("নতুন বাজার") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("নাম") })
                OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("পরিমাণ") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("দাম") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            Button(onClick = {
                val q = quantity.toDoubleOrNull() ?: 0.0
                val p = price.toDoubleOrNull() ?: 0.0
                if (name.isNotBlank()) onAdd(BazarItem(name, q, "কেজি", p, "সাধারণ"))
            }) { Text("সেভ") }
        }
    )
}

@Composable
fun ShoppingListScreen() {
    val list = remember { mutableStateListOf("দুধ", "ডিম", "পেঁয়াজ") }
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        itemsIndexed(list) { index, item ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(item)
                    IconButton(onClick = { list.removeAt(index) }) { Icon(Icons.Default.Delete, null) }
                }
            }
        }
    }
}
