package com.ghorerbazar.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GhorerBazarTheme {
                MainScreen()
            }
        }
    }
}

val PrimaryGreen = Color(0xFF1B5E20)
val SecondaryGreen = Color(0xFF2E7D32)
val LightBg = Color(0xFFF4F6F8)
val AccentOrange = Color(0xFFE65100)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val dao = db.bazarDao()
    val scope = rememberCoroutineScope()

    val itemList by dao.getAllItems().collectAsState(initial = emptyList())
    val totalExpense by dao.getTotalExpense().collectAsState(initial = 0.0)

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                    label = { Text("হিসাব") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                    label = { Text("বাজার") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.FormatListBulleted, contentDescription = null) },
                    label = { Text("ফর্দ") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryGreen,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "যোগ করুন")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBg)
                .padding(padding)
        ) {
            // Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(Brush.horizontalGradient(listOf(PrimaryGreen, SecondaryGreen)))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("ঘরের বাজার", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("মোট বাজার খরচ", fontSize = 14.sp, color = Color.Gray)
                                Text("৳ ${totalExpense ?: 0.0}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AccentOrange)
                            }
                            Button(
                                onClick = { showAddDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("+ যোগ করুন")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PaddingBox {
                Text("সাম্প্রতিক খরচের তালিকা", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(itemList) { item ->
                    BazarItemCard(item)
                }
            }
        }
    }

    if (showAddDialog) {
        AddItemDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, category, qty, price ->
                scope.launch {
                    val total = qty * price
                    val currentDate = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date())
                    dao.insertItem(
                        BazarItemEntity(
                            name = name,
                            category = category,
                            quantity = qty,
                            unit = "কেজি/পিস",
                            unitPrice = price,
                            totalPrice = total,
                            date = currentDate
                        )
                    )
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
fun AddItemDialog(onDismiss: () -> Unit, onSave: (String, String, Double, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("চাল/ডাল") }
    var qty by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("নতুন বাজার যোগ করুন") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("পণ্যের নাম") })
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("বিভাগ") })
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it },
                    label = { Text("পরিমাণ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("একক দাম (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val q = qty.toDoubleOrNull() ?: 1.0
                    val p = price.toDoubleOrNull() ?: 0.0
                    if (name.isNotEmpty()) {
                        onSave(name, category, q, p)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("সেভ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}

@Composable
fun BazarItemCard(item: BazarItemEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrimaryGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = PrimaryGreen)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(item.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                    Text("${item.category} • ${item.quantity} টি/কেজি", fontSize = 12.sp, color = Color.Gray)
                }
            }
            Text("৳ ${item.totalPrice}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

@Composable
fun PaddingBox(content: @Composable () -> Unit) {
    Box(modifier = Modifier.padding(horizontal = 16.dp)) { content() }
}

@Composable
fun GhorerBazarTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(primary = PrimaryGreen, secondary = SecondaryGreen),
        content = content
    )
}
