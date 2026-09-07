package com.ghorerbazar.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
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
    val shoppingList by dao.getShoppingList().collectAsState(initial = emptyList())
    val totalExpense by dao.getTotalExpense().collectAsState(initial = 0.0)

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddBazarDialog by remember { mutableStateOf(false) }
    var showAddFordoDialog by remember { mutableStateOf(false) }
    var selectedItemForReceipt by remember { mutableStateOf<BazarItemEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("হোম") }
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
                    label = { Text("তালিকা") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                    label = { Text("রিপোর্ট/PDF") }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(onClick = { showAddBazarDialog = true }, containerColor = PrimaryGreen) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                }
            } else if (selectedTab == 2) {
                FloatingActionButton(onClick = { showAddFordoDialog = true }, containerColor = AccentOrange) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBg)
                .padding(padding)
        ) {
            HeaderBanner(totalExpense = totalExpense ?: 0.0)

            when (selectedTab) {
                0 -> HomeScreen(itemList = itemList, onAddClick = { showAddBazarDialog = true }, onItemClick = { selectedItemForReceipt = it })
                1 -> BazarScreen(itemList = itemList, searchQuery = searchQuery, onSearchChange = { searchQuery = it }, onItemClick = { selectedItemForReceipt = it })
                2 -> FordoScreen(shoppingList = shoppingList, onToggle = { item ->
                    scope.launch { dao.updateShoppingItem(item.copy(isBought = !item.isBought)) }
                })
                3 -> ReportAndPdfScreen(itemList = itemList, totalExpense = totalExpense ?: 0.0, context = context)
            }
        }
    }

    if (showAddBazarDialog) {
        AddBazarDialog(
            onDismiss = { showAddBazarDialog = false },
            onSave = { name, cat, qty, unit, price, shop, payMethod, note ->
                scope.launch {
                    val dateStr = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date())
                    val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                    val item = BazarItemEntity(
                        name = name,
                        category = cat,
                        quantity = qty,
                        unit = unit,
                        unitPrice = price,
                        totalPrice = qty * price,
                        date = dateStr,
                        time = timeStr,
                        shopName = shop,
                        paymentMethod = payMethod,
                        note = note
                    )
                    dao.insertItem(item)
                    dao.insertPriceHistory(PriceHistory(itemName = name, price = price, date = dateStr))
                    showAddBazarDialog = false
                }
            }
        )
    }

    if (showAddFordoDialog) {
        AddFordoDialog(
            onDismiss = { showAddFordoDialog = false },
            onSave = { name, qty ->
                scope.launch {
                    dao.insertShoppingItem(ShoppingListItem(name = name, quantity = qty))
                    showAddFordoDialog = false
                }
            }
        )
    }

    if (selectedItemForReceipt != null) {
        ReceiptDialog(item = selectedItemForReceipt!!, onDismiss = { selectedItemForReceipt = null }, context = context)
    }
}

@Composable
fun HeaderBanner(totalExpense: Double) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(Brush.horizontalGradient(listOf(PrimaryGreen, SecondaryGreen)))
            .padding(20.dp)
    ) {
        Column {
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
                        Text("মোট বাজার খরচ", fontSize = 13.sp, color = Color.Gray)
                        Text("৳ $totalExpense", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AccentOrange)
                    }
                    Text("বাজেট সীমা: ৫০,০০০৳", fontSize = 12.sp, color = PrimaryGreen, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun HomeScreen(itemList: List<BazarItemEntity>, onAddClick: () -> Unit, onItemClick: (BazarItemEntity) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("সাম্প্রতিক বাজার (POS রসিদের জন্য ক্লিক করুন)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = onAddClick) { Text("+ যোগ করুন") }
            }
        }

        items(itemList.take(10)) { item ->
            BazarCard(item, onClick = { onItemClick(item) })
        }
    }
}

@Composable
fun BazarScreen(itemList: List<BazarItemEntity>, searchQuery: String, onSearchChange: (String) -> Unit, onItemClick: (BazarItemEntity) -> Unit) {
    val filteredList = itemList.filter { it.name.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("পণ্য বা বিভাগ খুঁজুন...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filteredList) { item ->
                BazarCard(item, onClick = { onItemClick(item) })
            }
        }
    }
}

@Composable
fun FordoScreen(shoppingList: List<ShoppingListItem>, onToggle: (ShoppingListItem) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(shoppingList) { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = item.isBought, onCheckedChange = { onToggle(item) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                item.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (item.isBought) Color.Gray else Color.Black
                            )
                            Text("পরিমাণ: ${item.quantity}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    Text(
                        if (item.isBought) "কেনা হয়েছে" else "বাকি আছে",
                        fontSize = 12.sp,
                        color = if (item.isBought) PrimaryGreen else AccentOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ReportAndPdfScreen(itemList: List<BazarItemEntity>, totalExpense: Double, context: Context) {
    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("মাসিক হিসাব ও রিপোর্ট", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("মোট বাজার খরচ: ৳ $totalExpense", fontSize = 14.sp)
                Text("মোট কেনাকাটার সংখ্যা: ${itemList.size} টি", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { generatePdfReport(context, itemList, totalExpense) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PDF রিপোর্ট ডাউনলোড করুন")
                }
            }
        }
    }
}

@Composable
fun BazarCard(item: BazarItemEntity, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = PrimaryGreen)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(item.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text("${item.category} • ${item.quantity} ${item.unit}", fontSize = 12.sp, color = Color.Gray)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("৳ ${item.totalPrice}", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("মেমো/প্রিন্ট", fontSize = 10.sp, color = PrimaryGreen, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ReceiptDialog(item: BazarItemEntity, onDismiss: () -> Unit, context: Context) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("POS ক্যাশ মেমো", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(item.shopName.ifEmpty { "ঘরের বাজার" }, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("তারিখ: ${item.date} | সময়: ${item.time}", fontSize = 11.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("পণ্যের নাম:")
                    Text(item.name, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("পরিমাণ:")
                    Text("${item.quantity} ${item.unit}")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("একক দর:")
                    Text("৳ ${item.unitPrice}")
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("মোট মূল্য:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("৳ ${item.totalPrice}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AccentOrange)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("পেমেন্ট পদ্ধতি:", fontSize = 12.sp)
                    Text(item.paymentMethod, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    Toast.makeText(context, "ব্লুটুথ থার্মাল প্রিন্টারে পাঠানো হচ্ছে...", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) { Text("প্রিন্ট (POS)") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বন্ধ করুন") } }
    )
}

@Composable
fun AddBazarDialog(onDismiss: () -> Unit, onSave: (String, String, Double, String, Double, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("চাল/ডাল") }
    var qty by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("কেজি") }
    var price by remember { mutableStateOf("") }
    var shop by remember { mutableStateOf("") }
    var payMethod by remember { mutableStateOf("নগদ (Cash)") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("দৈনিক খরচের তথ্য") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("পণ্যের নাম/খরচ") })
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("বিভাগ") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = qty,
                        onValueChange = { qty = it },
                        label = { Text("পরিমাণ") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("একক (কেজি/পিস)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("একক দাম (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(value = shop, onValueChange = { shop = it }, label = { Text("দোকান/বাজারের নাম") })
                OutlinedTextField(value = payMethod, onValueChange = { payMethod = it }, label = { Text("পেমেন্ট মেথড") })
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("অতিরিক্ত মন্তব্য") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val q = qty.toDoubleOrNull() ?: 1.0
                    val p = price.toDoubleOrNull() ?: 0.0
                    if (name.isNotEmpty()) onSave(name, category, q, unit, p, shop, payMethod, note)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) { Text("সেভ করুন") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}

@Composable
fun AddFordoDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ফর্দে নতুন পণ্য যোগ") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("পণ্যের নাম") })
                OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("পরিমাণ (যেমন: ২ কেজি)") })
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotEmpty()) onSave(name, qty) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
            ) { Text("যোগ করুন") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}

fun generatePdfReport(context: Context, itemList: List<BazarItemEntity>, totalExpense: Double) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas: Canvas = page.canvas
    val paint = Paint()

    paint.textSize = 18f
    paint.isFakeBoldText = true
    canvas.drawText("Ghorer Bazar - Expense Report", 40f, 50f, paint)

    paint.textSize = 12f
    paint.isFakeBoldText = false
    canvas.drawText("Total Expense: BDT $totalExpense", 40f, 80f, paint)
    canvas.drawText("Total Items: ${itemList.size}", 40f, 100f, paint)

    var y = 140f
    paint.isFakeBoldText = true
    canvas.drawText("Item Name | Category | Total Price", 40f, y, paint)
    paint.isFakeBoldText = false

    for (item in itemList.take(25)) {
        y += 25f
        canvas.drawText("${item.name} | ${item.category} | BDT ${item.totalPrice}", 40f, y, paint)
    }

    pdfDocument.finishPage(page)

    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Ghorer_Bazar_Report.pdf")
    try {
        pdfDocument.writeTo(FileOutputStream(file))
        Toast.makeText(context, "PDF সংরক্ষিত হয়েছে: ${file.name}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "PDF তৈরিতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show()
    }
    pdfDocument.close()
}

@Composable
fun GhorerBazarTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(primary = PrimaryGreen, secondary = SecondaryGreen),
        content = content
    )
}
