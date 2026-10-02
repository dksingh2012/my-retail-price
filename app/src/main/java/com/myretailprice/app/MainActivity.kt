package com.myretailprice.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun GroceryHome() {
    var query by remember { mutableStateOf("") }
    var offers by remember { mutableStateOf<List<Offer>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var searched by remember { mutableStateOf(false) }

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
                val response: HttpResponse = client.get(API + "/api/compare") {
                    parameter("q", query.trim())
                }
                val body = response.bodyAsText()
                if (response.status.isSuccess()) {
                    val json = Json { ignoreUnknownKeys = true }
                    val root = json.decodeFromString<Map<String, kotlinx.serialization.json.JsonElement>>(body)
                    val arr = root["offers"]
                    offers = if (arr != null) json.decodeFromJsonElement(arr) else emptyList()
                    if (offers.isEmpty()) error = "No matching offers found yet. Try milk or atta."
                } else {
                    error = "Could not compare prices right now. Please try again."
                }
            } catch (e: Exception) {
                error = "Connection problem. Please check your internet connection and try again."
            } finally {
                loading = false
            }
        }
    }

    val cheapest = offers.minByOrNull { it.price ?: Double.MAX_VALUE }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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

        Spacer(Modifier.height(14.dp))

        if (cheapest != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE9F8EE)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("CHEAPEST FOUND", fontWeight = FontWeight.ExtraBold, color = Color(0xFF138A3D))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "₹" + "%.0f".format(cheapest.price ?: 0.0),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        (cheapest.retailer ?: "Retailer") + " • " + (cheapest.name ?: query) + " " + (cheapest.pack ?: ""),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        if (offers.isNotEmpty()) {
            Text("Compare offers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(offers) { offer ->
                    Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
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
        } else if (!searched) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(20.dp))
                Text("Find the lower price before you shop.", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("Search a grocery item and compare retailer offers in one place.")
                Spacer(Modifier.height(18.dp))
                Text("Demo data is currently used for MVP testing.", style = MaterialTheme.typography.labelSmall)
            }
        } else {
            Spacer(Modifier.height(12.dp))
            Text("Try another grocery item.", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
        }
    }
}
