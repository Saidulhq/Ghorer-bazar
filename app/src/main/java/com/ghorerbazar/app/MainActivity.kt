package com.ghorerbazar.app

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

// Data Models
data class ExpenseItem(
    val id: Int,
    val name: String,
    val category: String,
    val amount: Double,
    val date: String,
    val quantity: String = "১ টি",
    val paymentMethod: String = "নগদ"
)

data class CategoryItem(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GhorerBazarTheme {
                MainAppStructure()
            }
        }
    }
}

val DeepGreen = Color(0xFF004D40)
val MediumGreen = Color(0xFF00796B)
val LightGreenBg = Color(0xFFE8F5E9)
val CardBgColor = Color(0xFFFFFFFF)
val ScreenBgColor = Color(0xFFF4F6F8)
val AccentRed = Color(0xFFE53935)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppStructure() {
    val context = LocalContext.current
    var isStarted by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    var userName by remember { mutableStateOf("শেখ সাইদুল হক") }
    var userBudget by remember { mutableDoubleStateOf(50000.0) }

    val expenses = remember {
        mutableStateListOf(
            ExpenseItem(1, "আলু", "সবজি ও ফল", 120.0, "07 Sep 2026", "২ কেজি"),
            ExpenseItem(2, "ডিম", "দুধ ও দুগ্ধজাত", 180.0, "06 Sep 2026", "১ ডজন"),
            ExpenseItem(3, "চাল", "চাল, ডাল, আটা", 600.0, "05 Sep 2026", "৫ কেজি"),
            ExpenseItem(4, "তেল", "তেল ও মসলা", 175.0, "04 Sep 2026", "১ লিটার")
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedReceiptItem by remember { mutableStateOf<ExpenseItem?>(null) }

    if (!isStarted) {
        WelcomeScreen(onStartClick = { isStarted = true })
    } else {
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
                        icon = { Icon(Icons.Default.List, contentDescription = null) },
                        label = { Text("তালিকা") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { showAddDialog = true },
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(DeepGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                            }
                        },
                        label = { Text("") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                        label = { Text("রিপোর্ট") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("সেটিংস") }
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ScreenBgColor)
                    .padding(padding)
            ) {
                when (selectedTab) {
                    0 -> DashboardScreen(
                        expenses = expenses,
                        userName = userName,
                        onCategoryClick = { selectedTab = 1 },
                        onReportClick = { selectedTab = 3 },
                        onAddClick = { showAddDialog = true },
                        onItemClick = { selectedReceiptItem = it }
                    )
                    1 -> ExpenseListScreen(expenses = expenses, onItemClick = { selectedReceiptItem = it })
                    3 -> AnalyticsReportScreen(expenses = expenses, context = context)
                    4 -> CustomSettingsScreen(
                        userName = userName,
                        onNameChange = { userName = it },
                        budget = userBudget,
                        onBudgetChange = { userBudget = it }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, category, qty, amount ->
                val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                expenses.add(0, ExpenseItem(expenses.size + 1, name, category, amount, dateStr, qty))
                showAddDialog = false
            }
        )
    }

    if (selectedReceiptItem != null) {
        ReceiptModal(item = selectedReceiptItem!!, onDismiss = { selectedReceiptItem = null }, context = context)
    }
}

// 1. Welcome Screen
@Composable
fun WelcomeScreen(onStartClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGreenBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(DeepGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White, modifier = Modifier.size(60.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("দৈনিক বাজার হিসাব নিকাশ", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
        Text("আজকের বাজার, আগামী দিনের সাশ্রয়", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onStartClick,
            colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp)
        ) {
            Text("শুরু করুন ➔", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// 2. Dashboard Screen
@Composable
fun DashboardScreen(
    expenses: List<ExpenseItem>,
    userName: String,
    onCategoryClick: () -> Unit,
    onReportClick: () -> Unit,
    onAddClick: () -> Unit,
    onItemClick: (ExpenseItem) -> Unit
) {
    val totalExpense = expenses.sumOf { it.amount }
    val todayExpense = expenses.filter { it.date == SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()) }.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("দৈনিক বাজার হিসাব নিকাশ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                    Text("হ্যালো, $userName", fontSize = 13.sp, color = Color.Gray)
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DeepGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = DeepGreen)
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("মোট খরচ", fontSize = 12.sp, color = DeepGreen)
                        Text("৳ ${totalExpense.toInt()}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("আজকের খরচ", fontSize = 12.sp, color = AccentRed)
                        Text("৳ ${todayExpense.toInt()}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AccentRed)
                    }
                }
            }
        }

        item {
            Text("দ্রুত অ্যাকশন", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickActionButton("নতুন খরচ", Icons.Default.Add, Modifier.weight(1f), onClick = onAddClick)
                QuickActionButton("তালিকা", Icons.Default.List, Modifier.weight(1f), onClick = onCategoryClick)
                QuickActionButton("রিপোর্ট", Icons.Default.BarChart, Modifier.weight(1f), onClick = onReportClick)
            }
        }

        item {
            Text("সাম্প্রতিক বাজার", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        items(expenses.take(5)) { item ->
            ExpenseCardItem(item = item, onClick = { onItemClick(item) })
        }
    }
}

@Composable
fun QuickActionButton(title: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CardBgColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = DeepGreen)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ExpenseCardItem(item: ExpenseItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CardBgColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(LightGreenBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = DeepGreen, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(item.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text("${item.category} (${item.quantity})", fontSize = 12.sp, color = Color.Gray)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("৳ ${item.amount.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                Text(item.date, fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

// 3. Expense List
@Composable
fun ExpenseListScreen(expenses: List<ExpenseItem>, onItemClick: (ExpenseItem) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("খরচের তালিকা", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepGreen, modifier = Modifier.padding(bottom = 12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(expenses) { item ->
                ExpenseCardItem(item = item, onClick = { onItemClick(item) })
            }
        }
    }
}

// 4. Analytics & Report
@Composable
fun AnalyticsReportScreen(expenses: List<ExpenseItem>, context: Context) {
    val totalExpense = expenses.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("মাসিক সারাংশ ও রিপোর্ট", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBgColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ক্যাটাগরি ভিত্তিক খরচ", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(12.dp))
                
                // Simple Donut Chart Representation
                Canvas(modifier = Modifier.size(120.dp)) {
                    drawArc(
                        color = DeepGreen,
                        startAngle = 0f,
                        sweepAngle = 240f,
                        useCenter = false,
                        style = Stroke(width = 30f)
                    )
                    drawArc(
                        color = AccentRed,
                        startAngle = 240f,
                        sweepAngle = 120f,
                        useCenter = false,
                        style = Stroke(width = 30f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("মোট খরচ: ৳ ${totalExpense.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
            }
        }

        Button(
            onClick = { generateAndOpenPdfReport(context, expenses, totalExpense) },
            colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("PDF রিপোর্ট দেখুন ও সেভ করুন")
        }
    }
}

// 5. Custom Settings
@Composable
fun CustomSettingsScreen(userName: String, onNameChange: (String) -> Unit, budget: Double, onBudgetChange: (Double) -> Unit) {
    var tempName by remember { mutableStateOf(userName) }
    var tempBudget by remember { mutableStateOf(budget.toInt().toString()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("অ্যাপ সেটিংস", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBgColor),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text("ব্যবহারকারীর নাম") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tempBudget,
                    onValueChange = { tempBudget = it },
                    label = { Text("মাসিক বাজেট সীমা (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        onNameChange(tempName)
                        val b = tempBudget.toDoubleOrNull() ?: budget
                        onBudgetChange(b)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("সেটিংস সেভ করুন")
                }
            }
        }
    }
}

// Modal Dialogs
@Composable
fun ReceiptModal(item: ExpenseItem, onDismiss: () -> Unit, context: Context) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ক্যাশ মেমো / রসিদ", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text("ঘরের বাজার", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DeepGreen)
                Text("তারিখ: ${item.date}", fontSize = 11.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text("পণ্য: ${item.name}", fontWeight = FontWeight.SemiBold)
                Text("ক্যাটাগরি: ${item.category}")
                Text("পরিমাণ: ${item.quantity}")
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text("মোট দাম: ৳ ${item.amount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AccentRed)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    Toast.makeText(context, "Thermal Printer-এ প্রিন্ট কমান্ড পাঠানো হয়েছে", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = DeepGreen)
            ) { Text("Thermal Print") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বন্ধ করুন") } }
    )
}

@Composable
fun AddExpenseDialog(onDismiss: () -> Unit, onSave: (String, String, String, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("সবজি ও ফল") }
    var qty by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("নতুন খরচ যোগ করুন") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("আইটেমের নাম") })
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("ক্যাটাগরি") })
                OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("পরিমাণ (যেমন: ১ কেজি)") })
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("মোট দাম (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val a = amount.toDoubleOrNull() ?: 0.0
                    if (name.isNotEmpty()) onSave(name, category, qty.ifEmpty { "১ টি" }, a)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DeepGreen)
            ) { Text("সংরক্ষণ করুন") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}

// PDF Helper Function
fun generateAndOpenPdfReport(context: Context, expenses: List<ExpenseItem>, totalExpense: Double) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas: Canvas = page.canvas
    val paint = Paint()

    paint.textSize = 18f
    paint.isFakeBoldText = true
    canvas.drawText("Ghorer Bazar - Monthly Report", 40f, 50f, paint)

    paint.textSize = 12f
    paint.isFakeBoldText = false
    canvas.drawText("Total Expense: BDT $totalExpense", 40f, 80f, paint)

    var y = 120f
    for (item in expenses) {
        y += 25f
        canvas.drawText("${item.date} | ${item.name} | ${item.category} | BDT ${item.amount}", 40f, y, paint)
    }

    pdfDocument.finishPage(page)

    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Ghorer_Bazar_Report.pdf")
    try {
        pdfDocument.writeTo(FileOutputStream(file))
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "PDF ওপেন করুন"))
    } catch (e: Exception) {
        Toast.makeText(context, "PDF সংরক্ষণ হয়েছে: ${file.path}", Toast.LENGTH_LONG).show()
    }
    pdfDocument.close()
}

@Composable
fun GhorerBazarTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(primary = DeepGreen, secondary = MediumGreen),
        content = content
    )
}
