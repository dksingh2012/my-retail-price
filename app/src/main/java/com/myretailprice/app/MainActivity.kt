package com.myretailprice.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myretailprice.app.R
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.builtins.ListSerializer

private const val API = "https://my-retail-price-api.dksingh2012.workers.dev"

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

data class BasketLine(
    val query: String,
    val offer: Offer
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
fun App() {
    val colors = lightColorScheme(
        primary = Color(0xFF16A34A),
        onPrimary = Color.White,
        secondary = Color(0xFFFF6A00),
        background = Color(0xFFF7FAF8),
        surface = Color.White,
        onSurface = Color(0xFF132033)
    )
    MaterialTheme(colorScheme = colors) {
        Surface(modifier = Modifier.fillMaxSize()) { GroceryHome() }
    }
}

suspend fun fetchOffers(client: HttpClient, query: String): List<Offer> {
    val response: HttpResponse = client.get(API + "/api/compare") {
        parameter("q", query.trim())
    }
    if (response.status.value !in 200..299) return emptyList()
    val json = Json { ignoreUnknownKeys = true }
    val root = json.decodeFromString<Map<String, kotlinx.serialization.json.JsonElement>>(response.bodyAsText())
    val arr = root["offers"] ?: return emptyList()
    return json.decodeFromJsonElement(ListSerializer(Offer.serializer()), arr)
}

@Composable
fun BrandHeader() {
    Row(Modifier.fillMaxWidth().background(Color(0xFF08743A)).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(74.dp).clip(RoundedCornerShape(18.dp)).background(Color.White).padding(4.dp),
            contentAlignment = Alignment.Center) {
            Image(painterResource(R.drawable.my_retail_price_logo), "MY RETAIL PRICE logo",
                Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("MY RETAIL PRICE", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
            Text("Compare Prices • Save More", color = Color(0xFFDDF7E6), fontSize = 12.sp)
        }
        Text("⚙", color = Color.White, fontSize = 24.sp)
    }
}

@Composable
fun RetailerStrip() {
    val retailers = listOf("B" to "Blinkit", "Z" to "Zepto", "b" to "BigBasket", "J" to "JioMart")
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column {
            Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                retailers.forEachIndexed { index, pair ->
                    val bg = listOf(Color(0xFFFFC400), Color(0xFF5B16A6), Color(0xFFB6D800), Color(0xFF1749C7))[index]
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Box(Modifier.size(46.dp).clip(RoundedCornerShape(13.dp)).background(bg), contentAlignment = Alignment.Center) {
                            Text(pair.first, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(pair.second, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10213B))
                    }
                }
            }
            Row(Modifier.fillMaxWidth().background(Color(0xFFEAF8EF)).padding(vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceEvenly) {
                Text("⚡ Live Prices", color = Color(0xFF08743A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("✓ Real-time Compare", color = Color(0xFF08743A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("🏷 Best Deals", color = Color(0xFF08743A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun BottomNav() {
    Surface(color = Color.White, shadowElevation = 8.dp) {
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            listOf("⌂" to "Home", "▥" to "History", "♡" to "Saved", "⚙" to "Settings").forEachIndexed { index, item ->
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(item.first, fontSize = 21.sp, color = if (index == 0) Color(0xFF16A34A) else Color(0xFF59616B))
                    Text(item.second, fontSize = 10.sp, fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (index == 0) Color(0xFF08743A) else Color(0xFF59616B))
                }
            }
        }
    }
}

@Composable
fun GroceryHome() {
    var query by remember { mutableStateOf("milk") }
    var offers by remember { mutableStateOf<List<Offer>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var live by remember { mutableStateOf(false) }

    var basketInput by remember { mutableStateOf("milk, atta, salt") }
    var basketLoading by remember { mutableStateOf(false) }
    var basketLines by remember { mutableStateOf<List<BasketLine>>(emptyList()) }
    var basketMissing by remember { mutableStateOf<List<String>>(emptyList()) }
    var basketSplitTotal by remember { mutableStateOf<Double?>(null) }
    var basketSingleRetailer by remember { mutableStateOf<String?>(null) }
    var basketSingleTotal by remember { mutableStateOf<Double?>(null) }

    val client = remember { HttpClient(Android) { expectSuccess = false } }
    val scope = rememberCoroutineScope()
    DisposableEffect(Unit) { onDispose { client.close() } }

    fun search() {
        if (query.isBlank()) return
        loading = true
        error = null
        offers = emptyList()
        scope.launch {
            try {
                val result = fetchOffers(client, query)
                offers = result
                live = true
                if (offers.isEmpty()) error = "No matching offers found. Try milk, atta or rice."
            } catch (e: Exception) {
                live = false
                error = e.message ?: "Unable to reach the live price service."
            } finally {
                loading = false
            }
        }
    }

    fun compareBasket() {
        val items = basketInput.split(",").map { it.trim() }.filter { it.isNotBlank() }.distinct().take(10)
        if (items.isEmpty()) return
        basketLoading = true
        basketLines = emptyList()
        basketMissing = emptyList()
        basketSplitTotal = null
        basketSingleRetailer = null
        basketSingleTotal = null
        scope.launch {
            try {
                val results = items.map { item -> item to fetchOffers(client, item) }
                val missing = results.filter { it.second.isEmpty() }.map { it.first }
                val matched = results.filter { it.second.isNotEmpty() }
                val lines = matched.map { (item, itemOffers) ->
                    BasketLine(item, itemOffers.minByOrNull { it.price ?: Double.MAX_VALUE }!!)
                }
                basketLines = lines
                basketMissing = missing
                basketSplitTotal = lines.sumOf { it.offer.price ?: 0.0 }

                val retailers = lines.mapNotNull { it.offer.retailer }.distinct()
                var bestRetailer: String? = null
                var bestTotal = Double.MAX_VALUE
                for (retailer in retailers) {
                    var complete = true
                    var total = 0.0
                    for ((_, itemOffers) in matched) {
                        val offer = itemOffers.firstOrNull { it.retailer == retailer }
                        if (offer == null) { complete = false; break }
                        total += offer.price ?: 0.0
                    }
                    if (complete && total < bestTotal) {
                        bestTotal = total
                        bestRetailer = retailer
                    }
                }
                basketSingleRetailer = bestRetailer
                basketSingleTotal = if (bestRetailer != null) bestTotal else null
            } catch (e: Exception) {
                error = e.message ?: "Basket comparison failed."
            } finally {
                basketLoading = false
            }
        }
    }

    val cheapest = offers.minByOrNull { it.price ?: Double.MAX_VALUE }

    Column(Modifier.fillMaxSize().background(Color(0xFFF5F9F6)).verticalScroll(rememberScrollState())) {
        BrandHeader()
        Column(Modifier.padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(12.dp))
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("📍", fontSize = 27.sp)
                    Spacer(Modifier.width(9.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Gurugram", color = Color(0xFF10213B), fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                        Text("PIN 122001", color = Color(0xFF657180), fontSize = 12.sp)
                    }
                    Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFEAF8EF)) {
                        Text("✓ Service area", Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            color = Color(0xFF08743A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            RetailerStrip()
            Spacer(Modifier.height(14.dp))

            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⌕", color = Color(0xFF10213B), fontSize = 34.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Compare one item", color = Color(0xFF10213B), fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
                            Text("Search and compare prices across retailers.", color = Color(0xFF586575), fontSize = 12.sp)
                        }
                        if (live) {
                            Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFFEAF8EF)) {
                                Text("● LIVE", Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                                    color = Color(0xFF08743A), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(),
                        singleLine = true, leadingIcon = { Text("⌕", fontSize = 24.sp) },
                        placeholder = { Text("Milk, atta, rice, oil...") },
                        label = { Text("What do you want to compare?") },
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { search() }, enabled = query.isNotBlank() && !loading,
                        modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(if (loading) "CHECKING PRICES..." else "⌕  COMPARE PRICES",
                            fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    }

                    Spacer(Modifier.height(11.dp))
                    Text("Popular searches", color = Color(0xFF586575), fontSize = 12.sp)
                    Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("milk", "atta", "rice", "oil").forEach { item ->
                            Surface(Modifier.weight(1f).clickable { query = item; search() },
                                shape = RoundedCornerShape(20.dp), color = Color(0xFFEAF8EF)) {
                                Text(item, Modifier.padding(vertical = 9.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    color = Color(0xFF08743A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (error != null) {
                        Spacer(Modifier.height(12.dp))
                        Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFFFECEB)) {
                            Column(Modifier.padding(12.dp)) {
                                Text("Live price connection needs attention", color = Color(0xFF9C241D),
                                    fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(error!!, color = Color(0xFF9C241D), fontSize = 11.sp)
                            }
                        }
                    }

                    if (cheapest != null) {
                        Spacer(Modifier.height(12.dp))
                        Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFEAF8EF)) {
                            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("LOWEST PRICE FOUND", color = Color(0xFF08743A), fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
                                    Text(cheapest.retailer ?: "Retailer", color = Color(0xFF10213B), fontWeight = FontWeight.Bold)
                                }
                                Text("₹${"%.0f".format(cheapest.price ?: 0.0)}", color = Color(0xFF10213B), fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }

                    if (offers.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Text("Retailer offers", color = Color(0xFF10213B), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        offers.take(8).forEach { offer ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(offer.retailer ?: "Retailer", color = Color(0xFF10213B), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${offer.name ?: query} ${offer.pack ?: ""}".trim(), color = Color(0xFF657180), fontSize = 10.sp)
                                }
                                Text("₹${"%.0f".format(offer.price ?: 0.0)}", color = Color(0xFF10213B), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            }
                            HorizontalDivider(color = Color(0xFFE9EEF0))
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF5))) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🛒", fontSize = 28.sp)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Smart Basket", color = Color(0xFF10213B), fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
                            Text("Compare one store vs a split basket.", color = Color(0xFF586575), fontSize = 12.sp)
                        }
                        Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFFEAF8EF)) {
                            Text("Save More", Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                                color = Color(0xFF08743A), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(11.dp))
                    OutlinedTextField(value = basketInput, onValueChange = { basketInput = it },
                        modifier = Modifier.fillMaxWidth(), minLines = 2, label = { Text("Basket items") },
                        placeholder = { Text("e.g. milk, atta, salt") }, shape = RoundedCornerShape(16.dp))
                    Spacer(Modifier.height(9.dp))
                    Button(onClick = { compareBasket() }, enabled = !basketLoading && basketInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6A00))) {
                        Text(if (basketLoading) "CALCULATING..." else "🛒  COMPARE MY BASKET",
                            fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }

                    if (basketSplitTotal != null) {
                        Spacer(Modifier.height(11.dp))
                        Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFEAF8EF)) {
                            Column(Modifier.fillMaxWidth().padding(13.dp)) {
                                Text("CHEAPEST SPLIT BASKET", color = Color(0xFF08743A), fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
                                Text("₹${"%.0f".format(basketSplitTotal)}", color = Color(0xFF10213B), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        Spacer(Modifier.height(7.dp))
                        Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                            Column(Modifier.fillMaxWidth().padding(13.dp)) {
                                Text("CHEAPEST SINGLE STORE", color = Color(0xFF10213B), fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
                                if (basketSingleRetailer != null && basketSingleTotal != null) {
                                    Text(basketSingleRetailer!!, color = Color(0xFF10213B), fontWeight = FontWeight.Bold)
                                    Text("₹${"%.0f".format(basketSingleTotal)}", color = Color(0xFF10213B), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                                } else {
                                    Text("No single retailer has every matched item.", color = Color(0xFF657180), fontSize = 11.sp)
                                }
                            }
                        }
                        basketLines.forEach { line ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(line.query, color = Color(0xFF10213B), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(line.offer.retailer ?: "Retailer", color = Color(0xFF657180), fontSize = 10.sp)
                                }
                                Text("₹${"%.0f".format(line.offer.price ?: 0.0)}", color = Color(0xFF10213B), fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        if (basketMissing.isNotEmpty()) {
                            Text("Not found: ${basketMissing.joinToString(", ")}", color = Color(0xFF9C241D), fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFEAF4FF)) {
                Text("Live price comparison is connected for Gurugram (122001). Results depend on the connected price service and retailer availability.",
                    Modifier.fillMaxWidth().padding(11.dp), color = Color(0xFF135EA8), fontSize = 10.sp)
            }
            Spacer(Modifier.height(12.dp))
        }
        BottomNav()
    }
}
