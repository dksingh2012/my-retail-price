package com.myretailprice.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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

private const val API = "https://my-retail-price-api.dksingh2012.workers.dev"

@Serializable
data class Offer(
    val id: Int? = null,
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
    return json.decodeFromJsonElement<List<Offer>>(arr)
}

@Composable
fun GroceryHome() {
    var query by remember { mutableStateOf("") }
    var offers by remember { mutableStateOf<List<Offer>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var searched by remember { mutableStateOf(false) }

    var basketInput by remember { mutableStateOf("milk, atta, salt") }
    var basketLoading by remember { mutableStateOf(false) }
    var basketLines by remember { mutableStateOf<List<BasketLine>>(emptyList()) }
    var basketMissing by remember { mutableStateOf<List<String>>(emptyList()) }
    var basketSplitTotal by remember { mutableStateOf<Double?>(null) }
    var basketSingleRetailer by remember { mutableStateOf<String?>(null) }
    var basketSingleTotal by remember { mutableStateOf<Double?>(null) }

    val client = remember { HttpClient(Android) { expectSuccess = false } }
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        onDispose { client.close() }
    }

    fun search() {
        if (query.isBlank()) return
        loading = true
        searched = true
        error = null
        scope.launch {
            try {
                offers = fetchOffers(client, query)
                if (offers.isEmpty()) error = "No matching offers found yet. Try milk or atta."
            } catch (e: Exception) {
                error = "Connection problem. Please check your internet connection and try again."
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

                val cheapestLines = matched.map { (item, itemOffers) ->
                    BasketLine(item, itemOffers.minByOrNull { it.price ?: Double.MAX_VALUE }!!)
                }

                basketLines = cheapestLines
                basketMissing = missing
                basketSplitTotal = cheapestLines.sumOf { it.offer.price ?: 0.0 }

                val retailers = cheapestLines.flatMap { line ->
                    line.offer.retailer?.let { listOf(it) } ?: emptyList()
                }.distinct()

                var bestRetailer: String? = null
                var bestTotal = Double.MAX_VALUE

                for (retailer in retailers) {
                    var complete = true
                    var total = 0.0

                    for ((item, itemOffers) in matched) {
                        val offer = itemOffers.firstOrNull { it.retailer == retailer }
                        if (offer == null) {
                            complete = false
                            break
                        }
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
                error = "Basket comparison failed. Please try again."
            } finally {
                basketLoading = false
            }
        }
    }

    val cheapest = offers.minByOrNull { it.price ?: Double.MAX_VALUE }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
    ) {
        Spacer(Modifier.height(14.dp))

        Image(
            painter = painterResource(R.drawable.my_retail_price_logo),
            contentDescription = "MY RETAIL PRICE logo",
            modifier = Modifier.fillMaxWidth().height(180.dp),
            contentScale = ContentScale.Fit
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📍", fontSize = 18.sp)
            Spacer(Modifier.width(8.dp))
            Column {
                Text("Gurugram", fontWeight = FontWeight.Bold)
                Text("PIN 122001", style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.weight(1f))
            Text("MVP", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))

        Text("Compare one item", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("What do you want to compare?") },
            placeholder = { Text("Milk, atta, rice, oil...") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = { search() },
            enabled = query.isNotBlank() && !loading,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(if (loading) "COMPARING..." else "COMPARE PRICES", fontWeight = FontWeight.ExtraBold)
        }

        if (error != null) {
            Spacer(Modifier.height(10.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        if (cheapest != null) {
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE9F8EE)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("CHEAPEST FOUND", fontWeight = FontWeight.ExtraBold, color = Color(0xFF138A3D))
                    Text("₹" + "%.0f".format(cheapest.price ?: 0.0), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                    Text(
                        (cheapest.retailer ?: "Retailer") + " • " + (cheapest.name ?: query) + " " + (cheapest.pack ?: ""),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (offers.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text("Compare offers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))

            offers.forEach { offer ->
                Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(offer.retailer ?: "Retailer", fontWeight = FontWeight.Bold)
                            Text((offer.name ?: "") + " • " + (offer.pack ?: ""), style = MaterialTheme.typography.bodySmall)
                        }
                        Text("₹" + "%.0f".format(offer.price ?: 0.0), fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        Spacer(Modifier.height(22.dp))

        Text("Smart Basket", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        Text(
            "Compare several items and see whether one retailer or a split basket costs less.",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = basketInput,
            onValueChange = { basketInput = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Basket items") },
            placeholder = { Text("milk, atta, salt") },
            minLines = 2,
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = { compareBasket() },
            enabled = !basketLoading && basketInput.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text(if (basketLoading) "CALCULATING..." else "COMPARE MY BASKET", fontWeight = FontWeight.ExtraBold)
        }

        if (basketSplitTotal != null) {
            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE9F8EE)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("CHEAPEST SPLIT BASKET", fontWeight = FontWeight.ExtraBold, color = Color(0xFF138A3D))
                    Text("₹" + "%.0f".format(basketSplitTotal), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                    Text("Each item is assigned to its lowest demo price.", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(8.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("CHEAPEST SINGLE STORE", fontWeight = FontWeight.ExtraBold)
                    if (basketSingleRetailer != null && basketSingleTotal != null) {
                        Text(basketSingleRetailer!!, fontWeight = FontWeight.Bold)
                        Text("₹" + "%.0f".format(basketSingleTotal), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                    } else {
                        Text("No single retailer has every matched item in the current demo data.")
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            basketLines.forEach { line ->
                Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(line.query, fontWeight = FontWeight.Bold)
                            Text(line.offer.retailer ?: "Retailer", style = MaterialTheme.typography.bodySmall)
                        }
                        Text("₹" + "%.0f".format(line.offer.price ?: 0.0), fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            if (basketMissing.isNotEmpty()) {
                Text("Not found in demo data: " + basketMissing.joinToString(", "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(Modifier.height(18.dp))
        Text("Demo data is currently used for MVP testing — not live retailer pricing.", style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(24.dp))
    }
}
