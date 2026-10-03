package com.myretailprice.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

private const val API = "https://my-retail-price-api.dksingh2012.workers.dev"

private val CyberBg = Color(0xFF060A12)
private val CyberPanel = Color(0xFF0C1220)
private val CyberPanel2 = Color(0xFF111A2B)
private val NeonCyan = Color(0xFF00E5FF)
private val NeonPurple = Color(0xFF9B5CFF)
private val NeonGreen = Color(0xFF39FF88)
private val CyberText = Color(0xFFE8F7FF)
private val CyberMuted = Color(0xFF8EA3B8)
private val Gold = Color(0xFFFFC857)

@Serializable
data class Offer(
    val id: JsonElement? = null,
    val name: String? = null,
    val pack: String? = null,
    val brand: String? = null,
    val retailer: String? = null,
    val price: Double? = null,
    val pincode: String? = null
)

data class ProductCategory(
    val icon: String,
    val name: String,
    val subtitle: String,
    val color: Color,
    val sample: String
)

private val categories = listOf(
    ProductCategory("♨", "GEYSER", "Capacity & rating", Color(0xFFFFB84D), "Orient Aquator Neo 15L"),
    ProductCategory("▣", "MOBILE", "Phones & variants", NeonCyan, "Samsung Galaxy S25 Ultra"),
    ProductCategory("▤", "TV", "Size & panel", NeonPurple, "Sony Bravia 55 inch 4K"),
    ProductCategory("◫", "AC", "Ton & star rating", NeonGreen, "LG 1.5 Ton 5 Star AC"),
    ProductCategory("▥", "FRIDGE", "Capacity & type", Color(0xFFFFB84D), "LG 655 L Frost Free Refrigerator")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
fun App() {
    val scheme = darkColorScheme(
        primary = NeonCyan,
        onPrimary = Color.Black,
        secondary = NeonPurple,
        background = CyberBg,
        surface = CyberPanel,
        onSurface = CyberText
    )
    MaterialTheme(colorScheme = scheme) {
        Surface(Modifier.fillMaxSize(), color = CyberBg) { MainHome() }
    }
}

suspend fun fetchOffers(client: HttpClient, query: String): List<Offer> {
    val response: HttpResponse = client.get(API + "/api/compare") {
        parameter("q", query.trim())
    }
    if (response.status.value !in 200..299) return emptyList()
    val json = Json { ignoreUnknownKeys = true }
    val root = json.decodeFromString<Map<String, JsonElement>>(response.bodyAsText())
    val arr = root["offers"] ?: return emptyList()
    return json.decodeFromJsonElement(ListSerializer(Offer.serializer()), arr)
}

@Composable
fun CyberHeader() {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(Color(0xFF07121F), Color(0xFF0B1020), Color(0xFF07131D))))
            .padding(horizontal = 18.dp, vertical = 18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("MY RETAIL PRICE", color = CyberText, fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp)
                Text("COMPARE & SHOP", color = NeonCyan, fontSize = 10.sp,
                    letterSpacing = 2.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Surface(shape = RoundedCornerShape(14.dp), color = NeonCyan.copy(alpha = .10f),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = .45f))) {
                Text("GURUGRAM 122001", Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Compare prices. Buy smarter.", color = CyberText, fontSize = 18.sp,
            lineHeight = 23.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(5.dp))
        Text("Find the exact model and compare verified retailer prices.",
            color = CyberMuted, fontSize = 12.sp)
    }
}

@Composable
fun CategoryDropdown(
    selected: ProductCategory,
    onSelect: (ProductCategory) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
        Surface(
            Modifier.fillMaxWidth().clickable { expanded = true },
            shape = RoundedCornerShape(15.dp),
            color = CyberPanel,
            border = BorderStroke(1.dp, NeonCyan.copy(alpha = .45f))
        ) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("PRODUCT CATEGORY", color = CyberMuted, fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace)
                    Text(selected.name, color = CyberText, fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
                }
                Text("⌄", color = NeonCyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(CyberPanel2)
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name, color = CyberText, fontSize = 12.sp) },
                    onClick = {
                        onSelect(category)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
fun ProductArt(category: ProductCategory, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxWidth().height(78.dp)) {
        val w=size.width; val h=size.height
        when(category.name) {
            "GEYSER" -> {
                drawRoundRect(Color(0xFFF3F6F8), topLeft=Offset(w*.31f,h*.05f),
                    size=androidx.compose.ui.geometry.Size(w*.38f,h*.72f),
                    cornerRadius=androidx.compose.ui.geometry.CornerRadius(18f,18f))
                drawRoundRect(Color(0xFF17202D), topLeft=Offset(w*.43f,h*.37f),
                    size=androidx.compose.ui.geometry.Size(w*.14f,h*.25f),
                    cornerRadius=androidx.compose.ui.geometry.CornerRadius(8f,8f))
                drawCircle(NeonGreen,4f,Offset(w*.50f,h*.45f))
                drawCircle(Gold,5f,Offset(w*.50f,h*.57f),style=Stroke(2f))
                drawLine(Color.Red,Offset(w*.42f,h*.77f),Offset(w*.42f,h*.91f),5f)
                drawLine(NeonCyan,Offset(w*.58f,h*.77f),Offset(w*.58f,h*.91f),5f)
            }
            "MOBILE" -> {
                drawRoundRect(Color(0xFFBFC7D4),topLeft=Offset(w*.30f,h*.08f),
                    size=androidx.compose.ui.geometry.Size(w*.30f,h*.80f),
                    cornerRadius=androidx.compose.ui.geometry.CornerRadius(16f,16f))
                drawRoundRect(Color(0xFF111827),topLeft=Offset(w*.48f,h*.08f),
                    size=androidx.compose.ui.geometry.Size(w*.30f,h*.80f),
                    cornerRadius=androidx.compose.ui.geometry.CornerRadius(16f,16f))
                drawCircle(Color(0xFF26354D),8f,Offset(w*.35f,h*.21f))
                drawCircle(Color(0xFF26354D),8f,Offset(w*.41f,h*.21f))
                drawCircle(Color(0xFF26354D),8f,Offset(w*.38f,h*.29f))
                drawCircle(NeonPurple,22f,Offset(w*.63f,h*.49f))
            }
            "TV" -> {
                drawRoundRect(Color(0xFF151D2B),topLeft=Offset(w*.08f,h*.15f),
                    size=androidx.compose.ui.geometry.Size(w*.84f,h*.60f),
                    cornerRadius=androidx.compose.ui.geometry.CornerRadius(6f,6f))
                drawRoundRect(Brush.linearGradient(listOf(NeonPurple,NeonCyan,Gold)),
                    topLeft=Offset(w*.12f,h*.19f),size=androidx.compose.ui.geometry.Size(w*.76f,h*.52f),
                    cornerRadius=androidx.compose.ui.geometry.CornerRadius(4f,4f))
                drawLine(Color(0xFF26354D),Offset(w*.42f,h*.82f),Offset(w*.58f,h*.82f),5f)
            }
            "AC" -> {
                drawRoundRect(Color(0xFFF3F7FA),topLeft=Offset(w*.10f,h*.22f),
                    size=androidx.compose.ui.geometry.Size(w*.80f,h*.42f),
                    cornerRadius=androidx.compose.ui.geometry.CornerRadius(15f,15f))
                drawLine(NeonCyan,Offset(w*.20f,h*.57f),Offset(w*.80f,h*.57f),5f)
                drawLine(NeonCyan,Offset(w*.32f,h*.68f),Offset(w*.25f,h*.82f),3f)
                drawLine(NeonCyan,Offset(w*.50f,h*.68f),Offset(w*.50f,h*.84f),3f)
                drawLine(NeonCyan,Offset(w*.68f,h*.68f),Offset(w*.75f,h*.82f),3f)
            }
            else -> {
                drawRoundRect(Color(0xFF394454),topLeft=Offset(w*.27f,h*.08f),
                    size=androidx.compose.ui.geometry.Size(w*.46f,h*.80f),
                    cornerRadius=androidx.compose.ui.geometry.CornerRadius(10f,10f))
                drawLine(Color(0xFF111827),Offset(w*.50f,h*.12f),Offset(w*.50f,h*.84f),4f)
                drawCircle(NeonCyan,4f,Offset(w*.62f,h*.28f))
            }
        }
    }
}

@Composable
fun CategoryCard(category: ProductCategory, selected: Boolean, onClick: () -> Unit) {
    Surface(Modifier.width(142.dp).clickable { onClick() }, shape = RoundedCornerShape(18.dp),
        color = if (selected) category.color.copy(alpha = .12f) else CyberPanel,
        border = BorderStroke(1.dp, if (selected) category.color.copy(alpha = .85f) else Color(0xFF233149))) {
        Column(Modifier.padding(10.dp)) {
            ProductArt(category)
            Text(category.name, color = CyberText, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace)
            Text(category.subtitle, color = CyberMuted, fontSize = 9.sp)
        }
    }
}

@Composable
fun SpecBox(category: ProductCategory, label: String, initial: String) {
    var value by remember(category.name,label) { mutableStateOf(initial) }
    var expanded by remember(category.name,label) { mutableStateOf(false) }
    val options = when(category.name) {
        "GEYSER" -> when(label) {
            "Brand" -> listOf("Select brand","Orient","Bajaj","Racold","Havells")
            "Type" -> listOf("Storage","Instant")
            "Capacity" -> listOf("6L","10L","15L","25L")
            "Star Rating" -> listOf("3★","4★","5★")
            else -> listOf("1500W","2000W","3000W")
        }
        "MOBILE" -> when(label) {
            "Brand" -> listOf("Select brand","Samsung","Apple","OnePlus","Xiaomi")
            "Model" -> listOf("Select model","Galaxy S25 Ultra","iPhone 17 Pro","OnePlus 13")
            "RAM" -> listOf("4GB","6GB","8GB","12GB")
            "Storage" -> listOf("128GB","256GB","512GB","1TB")
            else -> listOf("Black","Silver","Blue","Green")
        }
        "TV" -> when(label) {
            "Brand" -> listOf("Select brand","Sony","LG","Samsung","TCL")
            "Model" -> listOf("Select model","Bravia 55","OLED C5","QLED 55")
            "Screen Size" -> listOf("43 inch","50 inch","55 inch","65 inch")
            "Resolution" -> listOf("FHD","4K","8K")
            else -> listOf("LED","QLED","OLED")
        }
        "AC" -> when(label) {
            "Brand" -> listOf("Select brand","LG","Daikin","Voltas","Blue Star")
            "Model" -> listOf("Select model","1.5 Ton 5 Star","1.5 Ton 3 Star")
            "Capacity" -> listOf("1 Ton","1.5 Ton","2 Ton")
            "Type" -> listOf("Split","Window")
            else -> listOf("3★","4★","5★")
        }
        else -> when(label) {
            "Brand" -> listOf("Select brand","LG","Samsung","Whirlpool","Haier")
            "Model" -> listOf("Select model","655L","650L","500L")
            "Capacity" -> listOf("250L","350L","500L","650L")
            "Type" -> listOf("Frost Free","Direct Cool")
            else -> listOf("3★","4★","5★")
        }
    }
    Box(Modifier.width(112.dp).height(82.dp)) {
        Surface(Modifier.fillMaxSize().clickable { expanded=true }, shape=RoundedCornerShape(13.dp),
            color=CyberPanel2, border=BorderStroke(1.dp,category.color.copy(alpha=.38f))) {
            Column(Modifier.padding(9.dp)) {
                Text(label.uppercase(),color=category.color,fontSize=7.sp,fontWeight=FontWeight.Bold,fontFamily=FontFamily.Monospace)
                Spacer(Modifier.height(5.dp))
                Text(value,color=CyberText,fontSize=9.sp,maxLines=1)
                Spacer(Modifier.weight(1f))
                Text("SELECT  ⌄",color=category.color,fontSize=7.sp,fontFamily=FontFamily.Monospace)
            }
        }
        DropdownMenu(expanded,{expanded=false},modifier=Modifier.background(CyberPanel2)) {
            options.forEach { option ->
                DropdownMenuItem(text={Text(option,color=CyberText,fontSize=11.sp)},onClick={value=option;expanded=false})
            }
        }
    }
}

@Composable
fun SpecBox(category: ProductCategory, label: String, initial: String) {
    var value by remember(category.name,label) { mutableStateOf(initial) }
    var expanded by remember(category.name,label) { mutableStateOf(false) }
    val options = when(category.name) {
        "GEYSER" -> when(label) {
            "Brand" -> listOf("Select brand","Orient","Bajaj","Racold","Havells")
            "Type" -> listOf("Storage","Instant")
            "Capacity" -> listOf("6L","10L","15L","25L")
            "Star Rating" -> listOf("3★","4★","5★")
            else -> listOf("1500W","2000W","3000W")
        }
        "MOBILE" -> when(label) {
            "Brand" -> listOf("Select brand","Samsung","Apple","OnePlus","Xiaomi")
            "Model" -> listOf("Select model","Galaxy S25 Ultra","iPhone 17 Pro","OnePlus 13")
            "RAM" -> listOf("4GB","6GB","8GB","12GB")
            "Storage" -> listOf("128GB","256GB","512GB","1TB")
            else -> listOf("Black","Silver","Blue","Green")
        }
        "TV" -> when(label) {
            "Brand" -> listOf("Select brand","Sony","LG","Samsung","TCL")
            "Model" -> listOf("Select model","Bravia 55","OLED C5","QLED 55")
            "Screen Size" -> listOf("43 inch","50 inch","55 inch","65 inch")
            "Resolution" -> listOf("FHD","4K","8K")
            else -> listOf("LED","QLED","OLED")
        }
        "AC" -> when(label) {
            "Brand" -> listOf("Select brand","LG","Daikin","Voltas","Blue Star")
            "Model" -> listOf("Select model","1.5 Ton 5 Star","1.5 Ton 3 Star")
            "Capacity" -> listOf("1 Ton","1.5 Ton","2 Ton")
            "Type" -> listOf("Split","Window")
            else -> listOf("3★","4★","5★")
        }
        else -> when(label) {
            "Brand" -> listOf("Select brand","LG","Samsung","Whirlpool","Haier")
            "Model" -> listOf("Select model","655L","650L","500L")
            "Capacity" -> listOf("250L","350L","500L","650L")
            "Type" -> listOf("Frost Free","Direct Cool")
            else -> listOf("3★","4★","5★")
        }
    }
    Box(Modifier.width(112.dp).height(82.dp)) {
        Surface(Modifier.fillMaxSize().clickable { expanded=true }, shape=RoundedCornerShape(13.dp),
            color=CyberPanel2, border=BorderStroke(1.dp,category.color.copy(alpha=.38f))) {
            Column(Modifier.padding(9.dp)) {
                Text(label.uppercase(),color=category.color,fontSize=7.sp,fontWeight=FontWeight.Bold,fontFamily=FontFamily.Monospace)
                Spacer(Modifier.height(5.dp))
                Text(value,color=CyberText,fontSize=9.sp,maxLines=1)
                Spacer(Modifier.weight(1f))
                Text("SELECT  ⌄",color=category.color,fontSize=7.sp,fontFamily=FontFamily.Monospace)
            }
        }
        DropdownMenu(expanded,{expanded=false},modifier=Modifier.background(CyberPanel2)) {
            options.forEach { option ->
                DropdownMenuItem(text={Text(option,color=CyberText,fontSize=11.sp)},onClick={value=option;expanded=false})
            }
        }
    }
}

@Composable
fun ProductSpecPanel(category: ProductCategory) {
    val specs = when(category.name) {
        "GEYSER" -> listOf("Brand" to "Select brand","Type" to "Storage","Capacity" to "15L","Star Rating" to "5★","Power" to "2000W")
        "MOBILE" -> listOf("Brand" to "Select brand","Model" to "Select model","RAM" to "8GB","Storage" to "256GB","Variant" to "Black")
        "TV" -> listOf("Brand" to "Select brand","Model" to "Select model","Screen Size" to "55 inch","Resolution" to "4K","Panel" to "OLED")
        "AC" -> listOf("Brand" to "Select brand","Model" to "Select model","Capacity" to "1.5 Ton","Type" to "Split","Rating" to "5★")
        else -> listOf("Brand" to "Select brand","Model" to "Select model","Capacity" to "500L","Type" to "Frost Free","Rating" to "5★")
    }
    Column {
        Row(verticalAlignment=Alignment.CenterVertically) {
            Text("PRODUCT SPECIFICATIONS",color=CyberMuted,fontSize=8.sp,fontWeight=FontWeight.Bold,letterSpacing=1.3.sp,fontFamily=FontFamily.Monospace)
            Spacer(Modifier.weight(1f))
            Text("TAP TO SELECT",color=category.color,fontSize=7.sp,fontFamily=FontFamily.Monospace)
        }
        Spacer(Modifier.height(7.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(7.dp)) {
            specs.forEach { (label,value) -> SpecBox(category,label,value) }
        }
    }
}

@Composable
fun ExactMatchPanel(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedCategory: ProductCategory,
    loading: Boolean,
    live: Boolean,
    offers: List<Offer>,
    error: String?,
    onSearch: () -> Unit
) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = CyberPanel,
        border = BorderStroke(1.dp, Color(0xFF26364F))) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("EXACT PRODUCT MATCH", color = NeonCyan, fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold, letterSpacing = 1.4.sp, fontFamily = FontFamily.Monospace)
                    Text("Brand + model + variant", color = CyberText, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                }
                Surface(shape = RoundedCornerShape(12.dp), color = NeonGreen.copy(alpha = .10f),
                    border = BorderStroke(1.dp, NeonGreen.copy(alpha = .4f))) {
                    Text(if (live) "LIVE" else "READY", Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        color = if (live) NeonGreen else CyberMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query, onValueChange = onQueryChange, modifier = Modifier.fillMaxWidth(), singleLine = true,
                label = { Text("Search ${selectedCategory.name.lowercase()} model") },
                placeholder = { Text(selectedCategory.sample, color = CyberMuted) },
                shape = RoundedCornerShape(15.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan, unfocusedBorderColor = Color(0xFF33445E),
                    focusedLabelColor = NeonCyan, unfocusedLabelColor = CyberMuted,
                    focusedTextColor = CyberText, unfocusedTextColor = CyberText, cursorColor = NeonCyan
                )
            )
            Spacer(Modifier.height(10.dp))
            Button(onClick = onSearch, enabled = query.isNotBlank() && !loading,
                modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black)) {
                Text(if (loading) "SCANNING..." else "⌕  COMPARE & SHOP",
                    fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("EXACT MATCH", "REAL PRICE", "PRICE HISTORY").forEach {
                    Surface(shape = RoundedCornerShape(9.dp), color = Color(0xFF111C2D),
                        border = BorderStroke(1.dp, Color(0xFF2C405D))) {
                        Text(it, Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            color = CyberMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
            if (error != null) {
                Spacer(Modifier.height(12.dp))
                Surface(shape = RoundedCornerShape(13.dp), color = Color(0xFF25151B),
                    border = BorderStroke(1.dp, Color(0xFF6E2C3B))) {
                    Text(error, Modifier.fillMaxWidth().padding(11.dp), color = Color(0xFFFF9BAE), fontSize = 10.sp)
                }
            }
            if (offers.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                Text("RETAILER SCAN", color = NeonPurple, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp, fontFamily = FontFamily.Monospace)
                offers.take(8).forEachIndexed { index, offer ->
                    Surface(Modifier.fillMaxWidth().padding(top = 7.dp), shape = RoundedCornerShape(14.dp),
                        color = if (index == 0) NeonGreen.copy(alpha = .08f) else CyberPanel2,
                        border = BorderStroke(1.dp, if (index == 0) NeonGreen.copy(alpha = .45f) else Color(0xFF26364F))) {
                        Row(Modifier.padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(offer.retailer ?: "Retailer",
                                    color = if (index == 0) NeonGreen else CyberText, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                Text("${offer.name ?: query} ${offer.pack ?: ""}".trim(), color = CyberMuted, fontSize = 9.sp, maxLines = 2)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                if (index == 0) Text("LOWEST", color = NeonGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                Text("₹${"%.0f".format(offer.price ?: 0.0)}", color = CyberText, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            } else if (!loading) {
                Spacer(Modifier.height(14.dp))
                Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFF101A2A),
                    border = BorderStroke(1.dp, Color(0xFF26364F))) {
                    Column(Modifier.padding(12.dp)) {
                        Text("MATCH ENGINE READY", color = NeonCyan, fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
                        Text("Retailer feeds for ${selectedCategory.name.lowercase()} will plug into this exact-match screen. No price is shown as live until a verified feed is connected.",
                            color = CyberMuted, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun GeyserCard(title: String, accent: Color) {
    Surface(Modifier.width(230.dp),shape=RoundedCornerShape(20.dp),color=CyberPanel,border=BorderStroke(1.dp,accent.copy(alpha=.6f))) {
        Column(Modifier.padding(12.dp)) {
            ProductArt(categories.first(),Modifier.height(105.dp))
            Text(title,color=CyberText,fontSize=13.sp,fontWeight=FontWeight.ExtraBold)
            Text("Storage geyser",color=CyberMuted,fontSize=9.sp)
            Spacer(Modifier.height(7.dp))
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                listOf("15L","5★","2000W").forEach {
                    Surface(shape=RoundedCornerShape(8.dp),color=accent.copy(alpha=.08f),border=BorderStroke(1.dp,accent.copy(alpha=.25f))) {
                        Text(it,Modifier.padding(horizontal=7.dp,vertical=5.dp),color=accent,fontSize=8.sp,fontFamily=FontFamily.Monospace)
                    }
                }
            }
            Spacer(Modifier.height(9.dp))
            Text("PRICE CHECK",color=NeonGreen,fontSize=9.sp,fontWeight=FontWeight.Bold,fontFamily=FontFamily.Monospace)
            Text("Verified retailer prices will appear here",color=CyberMuted,fontSize=9.sp)
        }
    }
}

@Composable
fun GeyserScreen(onBack: () -> Unit) {
    var brand by remember { mutableStateOf("Select Brand") }
    var type by remember { mutableStateOf("Storage") }
    var capacity by remember { mutableStateOf("15L") }
    var rating by remember { mutableStateOf("5★") }
    var power by remember { mutableStateOf("2000W") }
    var searched by remember { mutableStateOf(false) }
    val boxes=listOf(
        Triple("Brand",brand,listOf("Select Brand","Orient","Bajaj","Racold","Havells")),
        Triple("Type",type,listOf("Storage","Instant")),
        Triple("Capacity",capacity,listOf("6L","10L","15L","25L")),
        Triple("Star Rating",rating,listOf("3★","4★","5★")),
        Triple("Power",power,listOf("1500W","2000W","3000W"))
    )
    Column(Modifier.fillMaxSize().background(CyberBg).verticalScroll(rememberScrollState())) {
        Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically) {
            Text("‹",Modifier.clickable{onBack()},color=NeonCyan,fontSize=36.sp)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text("GEYSER",color=CyberText,fontSize=27.sp,fontWeight=FontWeight.Black,fontFamily=FontFamily.Monospace)
                Text("COMPARE & SHOP",color=NeonCyan,fontSize=10.sp,letterSpacing=2.5.sp,fontFamily=FontFamily.Monospace)
            }
            Text("122001",color=CyberMuted,fontSize=9.sp,fontFamily=FontFamily.Monospace)
        }
        Surface(Modifier.padding(horizontal=14.dp).fillMaxWidth(),shape=RoundedCornerShape(22.dp),color=Color(0xFF0B1622),border=BorderStroke(1.dp,NeonCyan.copy(alpha=.45f))) {
            Box(Modifier.height(170.dp)) {
                ProductArt(categories.first(),Modifier.fillMaxWidth().padding(top=18.dp).height(135.dp))
                Column(Modifier.padding(16.dp)) {
                    Text("FIND YOUR GEYSER",color=CyberText,fontSize=24.sp,fontWeight=FontWeight.Black)
                    Text("Select specifications to find the exact model.",color=CyberMuted,fontSize=10.sp)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("PRODUCT SPECIFICATIONS",Modifier.padding(horizontal=16.dp),color=CyberMuted,fontSize=9.sp,fontWeight=FontWeight.Bold,letterSpacing=1.4.sp,fontFamily=FontFamily.Monospace)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal=14.dp),horizontalArrangement=Arrangement.spacedBy(7.dp)) {
            boxes.forEach { (label,value,opts) ->
                var expanded by remember(label){mutableStateOf(false)}
                Box(Modifier.width(118.dp).height(82.dp)) {
                    Surface(Modifier.fillMaxSize().clickable{expanded=true},shape=RoundedCornerShape(13.dp),color=CyberPanel2,border=BorderStroke(1.dp,Gold.copy(alpha=.5f))) {
                        Column(Modifier.padding(9.dp)) {
                            Text(label.uppercase(),color=Gold,fontSize=7.sp,fontWeight=FontWeight.Bold,fontFamily=FontFamily.Monospace)
                            Spacer(Modifier.height(6.dp)); Text(value,color=CyberText,fontSize=10.sp,maxLines=1)
                            Spacer(Modifier.weight(1f)); Text("SELECT  ⌄",color=NeonCyan,fontSize=7.sp,fontFamily=FontFamily.Monospace)
                        }
                    }
                    DropdownMenu(expanded,{expanded=false},modifier=Modifier.background(CyberPanel2)) {
                        opts.forEach { o ->
                            DropdownMenuItem(text={Text(o,color=CyberText,fontSize=11.sp)},onClick={
                                when(label){"Brand"->brand=o;"Type"->type=o;"Capacity"->capacity=o;"Star Rating"->rating=o;"Power"->power=o}
                                expanded=false
                            })
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick={searched=true},Modifier.padding(horizontal=14.dp).fillMaxWidth().height(52.dp),shape=RoundedCornerShape(15.dp),colors=ButtonDefaults.buttonColors(containerColor=NeonCyan,contentColor=Color.Black)) {
            Text("⌕  FIND EXACT GEYSER",fontSize=13.sp,fontWeight=FontWeight.Black,fontFamily=FontFamily.Monospace)
        }
        Spacer(Modifier.height(16.dp))
        Text(if(searched)"MATCHES FOR "+capacity+" • "+rating+" • "+power else "POPULAR GEYSERS",Modifier.padding(horizontal=16.dp),color=NeonCyan,fontSize=12.sp,fontWeight=FontWeight.ExtraBold,fontFamily=FontFamily.Monospace)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal=14.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            GeyserCard("Orient Aquator Neo",Gold); GeyserCard("Bajaj Shield Series",NeonCyan); GeyserCard("Havells Adonia",NeonPurple)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun HighValueHome(onGeyser: () -> Unit) {
    var selected by remember { mutableStateOf(categories.first()) }
    var query by remember { mutableStateOf(categories.first().sample) }
    var offers by remember { mutableStateOf<List<Offer>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var live by remember { mutableStateOf(false) }

    val client = remember { HttpClient(Android) { expectSuccess = false } }
    val scope = rememberCoroutineScope()
    DisposableEffect(Unit) { onDispose { client.close() } }

    fun select(category: ProductCategory) {
        if (category.name == "GEYSER") { onGeyser(); return }
        selected = category
        query = category.sample
        offers = emptyList()
        error = null
        live = false
    }

    fun search() {
        if (query.isBlank()) return
        loading = true
        error = null
        offers = emptyList()
        scope.launch {
            try {
                val result = fetchOffers(client, query)
                val tokens = query.lowercase().split(Regex("[^a-z0-9]+")).filter { it.length >= 3 }.distinct()
                val exact = result.filter { offer ->
                    val haystack = ((offer.brand ?: "") + " " + (offer.name ?: "") + " " + (offer.pack ?: "")).lowercase()
                    tokens.count { haystack.contains(it) } >= maxOf(1, (tokens.size * 0.7f).toInt())
                }
                offers = exact
                live = false
                if (exact.isEmpty()) error = "No verified exact match found. Unrelated retailer results are hidden."
            } catch (e: Exception) {
                live = false
                error = "Live comparison is not available for this category yet."
            } finally {
                loading = false
            }
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(CyberBg)) {
        CyberHeader()
        Column(Modifier.padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(12.dp))
            Text("CHOOSE CATEGORY", color = CyberMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp, fontFamily = FontFamily.Monospace)
            Spacer(Modifier.height(7.dp))
            CategoryDropdown(selected) { select(it) }
            Spacer(Modifier.height(10.dp))
            Text("QUICK CATEGORY", color = CyberMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp, fontFamily = FontFamily.Monospace)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                categories.forEach { category -> CategoryCard(category, selected == category) { select(category) } }
            }
            Spacer(Modifier.height(15.dp))
            ProductSpecPanel(selected)
            Spacer(Modifier.height(12.dp))
            ExactMatchPanel(query, { query = it }, selected, loading, live, offers, error) { search() }
            Spacer(Modifier.height(15.dp))
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = Color(0xFF0A111D),
                border = BorderStroke(1.dp, NeonPurple.copy(alpha = .35f))) {
                Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("◆", color = NeonPurple, fontSize = 18.sp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("WHY MY RETAIL PRICE?", color = CyberText, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace)
                        Text("Same model first — then price. Similar products are kept separate.", color = CyberMuted, fontSize = 9.sp)
                    }
                }
            }
            Spacer(Modifier.height(15.dp))
            Text("COMING INTO THE ENGINE", color = CyberMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp, fontFamily = FontFamily.Monospace)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("EXACT MATCH" to NeonCyan, "EFFECTIVE PRICE" to NeonGreen, "PRICE HISTORY" to NeonPurple).forEach {
                    Surface(Modifier.weight(1f), shape = RoundedCornerShape(13.dp), color = it.second.copy(alpha = .08f),
                        border = BorderStroke(1.dp, it.second.copy(alpha = .35f))) {
                        Text(it.first, Modifier.padding(vertical = 11.dp), textAlign = TextAlign.Center,
                            color = it.second, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
fun FixedFooter(selected: Int, onSelect: (Int) -> Unit) {
    Surface(
        color = Color(0xFF080E18),
        shadowElevation = 18.dp,
        tonalElevation = 4.dp,
        border = BorderStroke(1.dp, Color(0xFF26364F))
    ) {
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("⌂" to "Home", "◷" to "History", "♡" to "Saved", "⚙" to "Settings")
                .forEachIndexed { index, item ->
                    Surface(
                        Modifier.weight(1f).clickable { onSelect(index) },
                        shape = RoundedCornerShape(13.dp),
                        color = if (selected == index) NeonCyan.copy(alpha = .10f) else Color.Transparent
                    ) {
                        Column(Modifier.padding(vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(item.first, fontSize = 19.sp,
                                color = if (selected == index) NeonCyan else CyberMuted)
                            Text(item.second, fontSize = 8.sp,
                                fontWeight = if (selected == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected == index) CyberText else CyberMuted,
                                fontFamily = FontFamily.Monospace)
                        }
                    }
                }
        }
    }
}

@Composable
fun MainHome() {
    var selectedTab by remember { mutableStateOf(0) }
    var showGeyser by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(CyberBg)) {
        Box(Modifier.weight(1f)) {
            if (showGeyser) {
                GeyserScreen { showGeyser = false }
            } else if (selectedTab == 0) {
                HighValueHome { showGeyser = true }
            } else {
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        when (selectedTab) {
                            1 -> "PRICE HISTORY"
                            2 -> "SAVED PRODUCTS"
                            else -> "SETTINGS"
                        },
                        color = NeonCyan, fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("This section is ready for the next build.",
                        color = CyberMuted, fontSize = 11.sp, textAlign = TextAlign.Center)
                }
            }
        }
        if (!showGeyser) FixedFooter(selectedTab) { selectedTab = it }
    }
}
