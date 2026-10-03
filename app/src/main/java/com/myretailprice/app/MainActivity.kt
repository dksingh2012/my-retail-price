package com.myretailprice.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
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
        Text("Compare prices. Buy smarter.", color = CyberText, fontSize = 25.sp,
            lineHeight = 30.sp, fontWeight = FontWeight.ExtraBold)
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
fun CategoryCard(category: ProductCategory, selected: Boolean, onClick: () -> Unit) {
    Surface(Modifier.width(142.dp).clickable { onClick() }, shape = RoundedCornerShape(18.dp),
        color = if (selected) category.color.copy(alpha = .12f) else CyberPanel,
        border = BorderStroke(1.dp, if (selected) category.color.copy(alpha = .85f) else Color(0xFF233149))) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(category.icon, color = category.color, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                if (selected) Text("●", color = category.color, fontSize = 9.sp)
            }
            Spacer(Modifier.height(10.dp))
            Text(category.name, color = CyberText, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace)
            Text(category.subtitle, color = CyberMuted, fontSize = 9.sp)
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
fun HighValueHome() {
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

    Column(Modifier.fillMaxSize().background(CyberBg)) {
        Box(Modifier.weight(1f)) {
            if (selectedTab == 0) {
                HighValueHome()
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
        FixedFooter(selectedTab) { selectedTab = it }
    }
}
