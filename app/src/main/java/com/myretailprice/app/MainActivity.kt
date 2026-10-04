package com.myretailprice.app

import android.os.Bundle
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg=Color(0xFF050A12)
private val Panel=Color(0xFF0A1220)
private val Panel2=Color(0xFF111B2D)
private val Cyan=Color(0xFF12E4F5)
private val Purple=Color(0xFF9C5CFF)
private val Green=Color(0xFF37F58A)
private val Gold=Color(0xFFFFC95C)
private val White=Color(0xFFF2F7FB)
private val Muted=Color(0xFF9BAEC2)

private data class LiveOffer(
 val retailer:String,val name:String,val brand:String,val price:Double,val mrp:Double?,
 val matchType:String,val productUrl:String,val available:Boolean
)

private suspend fun fetchLiveOffers(query:String):Result<List<LiveOffer>> = withContext(Dispatchers.IO){
 try{
  val encoded=URLEncoder.encode(query,"UTF-8")
  val conn=URL("https://my-retail-price-api.dksingh2012.workers.dev/api/compare?q="+encoded+"&pincode=122001").openConnection() as HttpURLConnection
  conn.requestMethod="GET";conn.connectTimeout=15000;conn.readTimeout=20000
  val code=conn.responseCode
  val text=(if(code in 200..299) conn.inputStream else conn.errorStream).bufferedReader().use{it.readText()}
  if(code !in 200..299) return@withContext Result.failure(Exception("Live search failed ($code)"))
  val root=JSONObject(text);val arr=root.optJSONArray("offers")?:org.json.JSONArray()
  val list=buildList{
   for(i in 0 until arr.length()){
    val o=arr.getJSONObject(i)
    add(LiveOffer(o.optString("retailer","Retailer"),o.optString("name",query),o.optString("brand",""),
      o.optDouble("price",0.0),if(o.has("mrp")&&!o.isNull("mrp"))o.optDouble("mrp") else null,
      o.optString("match_type","alternative"),o.optString("product_url",""),o.optBoolean("available",true)))
   }
  }
  Result.success(list)
 }catch(e:Exception){Result.failure(e)}
}

data class Cat(val name:String,val subtitle:String,val accent:Color)
private val cats=listOf(
 Cat("GEYSER","Capacity & rating",Gold),
 Cat("MOBILE","Phones & variants",Cyan),
 Cat("TV","Size & panel",Purple),
 Cat("AC","Ton & star rating",Green),
 Cat("FRIDGE","Capacity & type",Gold)
)

class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{App()}}
}

@Composable private fun App(){
 MaterialTheme(colorScheme=darkColorScheme(background=Bg,surface=Panel,primary=Cyan)){
  Surface(Modifier.fillMaxSize(),color=Bg){MainShell()}
 }
}

@Composable private fun LogoHeader(){
 Row(Modifier.fillMaxWidth().padding(horizontal=18.dp,vertical=14.dp),verticalAlignment=Alignment.Top){
  Column(Modifier.weight(1f)){
   Row(verticalAlignment=Alignment.CenterVertically){
    Text("🛒",fontSize=23.sp);Spacer(Modifier.width(5.dp))
    Text("MY RETAIL\nPRICE",style=TextStyle(
     brush=Brush.horizontalGradient(listOf(Gold,White,Cyan)),fontSize=27.sp,lineHeight=28.sp,
     fontWeight=FontWeight.Black,fontFamily=FontFamily.Monospace,letterSpacing=1.sp))
   }
   Text("—  C O M P A R E   &   S H O P  —",color=Cyan,fontSize=9.sp,letterSpacing=2.6.sp,fontFamily=FontFamily.Monospace,fontWeight=FontWeight.Bold)
  }
  Surface(shape=RoundedCornerShape(16.dp),color=Color(0xFF071A2A),border=BorderStroke(1.3.dp,Cyan.copy(.8f))){
   Column(Modifier.width(112.dp).padding(vertical=9.dp),horizontalAlignment=Alignment.CenterHorizontally){
    Text("⌖  Gurugram",color=White,fontSize=10.sp,fontWeight=FontWeight.Bold)
    Spacer(Modifier.height(6.dp));Text("122001 ⌄",color=Cyan,fontSize=11.sp,fontWeight=FontWeight.Bold)
   }
  }
 }
}

@Composable private fun SearchBox(value:String,onChange:(String)->Unit,onSearch:()->Unit){
 OutlinedTextField(value=value,onValueChange=onChange,modifier=Modifier.fillMaxWidth().height(58.dp),singleLine=true,
  placeholder={Text("Search brand, model or product",color=Muted,fontSize=15.sp)},
  leadingIcon={Text("⌕",color=Cyan,fontSize=28.sp)},
  trailingIcon={Text("⌕",Modifier.clickable{onSearch()},color=Cyan,fontSize=24.sp)},
  shape=RoundedCornerShape(20.dp),
  colors=OutlinedTextFieldDefaults.colors(focusedBorderColor=Cyan,unfocusedBorderColor=Cyan.copy(.8f),
   focusedTextColor=White,unfocusedTextColor=White,cursorColor=Cyan))
}

@Composable private fun Art(cat:Cat,modifier:Modifier=Modifier){
 Canvas(modifier.fillMaxWidth()){
  drawRoundRect(Brush.radialGradient(listOf(Color(0xFF18324A),Color(0xFF07101D))),Offset.Zero,Size(size.width,size.height),androidx.compose.ui.geometry.CornerRadius(20f))
  val w=size.width;val h=size.height
  when(cat.name){
   "GEYSER"->{drawRoundRect(Color(0xFFF4F7F9),Offset(w*.32f,h*.08f),Size(w*.36f,h*.68f),androidx.compose.ui.geometry.CornerRadius(18f))
    drawRoundRect(Color(0xFF182232),Offset(w*.43f,h*.36f),Size(w*.14f,h*.23f),androidx.compose.ui.geometry.CornerRadius(8f))
    drawCircle(Green,4f,Offset(w*.50f,h*.45f));drawCircle(Gold,5f,Offset(w*.50f,h*.56f),style=Stroke(2f))
    drawLine(Color.Red,Offset(w*.42f,h*.75f),Offset(w*.42f,h*.92f),5f);drawLine(Cyan,Offset(w*.58f,h*.75f),Offset(w*.58f,h*.92f),5f)}
   "MOBILE"->{drawRoundRect(Color(0xFFBFC9D7),Offset(w*.29f,h*.08f),Size(w*.30f,h*.82f),androidx.compose.ui.geometry.CornerRadius(17f))
    drawRoundRect(Color(0xFF111A2B),Offset(w*.48f,h*.08f),Size(w*.30f,h*.82f),androidx.compose.ui.geometry.CornerRadius(17f))
    drawCircle(Color(0xFF33445E),8f,Offset(w*.35f,h*.22f));drawCircle(Color(0xFF33445E),8f,Offset(w*.41f,h*.22f));drawCircle(Purple,22f,Offset(w*.64f,h*.48f))}
   "TV"->{drawRoundRect(Color(0xFF182133),Offset(w*.08f,h*.15f),Size(w*.84f,h*.58f),androidx.compose.ui.geometry.CornerRadius(7f))
    drawRoundRect(Brush.linearGradient(listOf(Purple,Cyan,Gold)),Offset(w*.12f,h*.19f),Size(w*.76f,h*.50f),androidx.compose.ui.geometry.CornerRadius(4f))
    drawLine(Color(0xFF35435A),Offset(w*.43f,h*.82f),Offset(w*.57f,h*.82f),5f)}
   "AC"->{drawRoundRect(Color(0xFFF4F8FA),Offset(w*.09f,h*.23f),Size(w*.82f,h*.40f),androidx.compose.ui.geometry.CornerRadius(15f))
    drawLine(Cyan,Offset(w*.20f,h*.57f),Offset(w*.80f,h*.57f),5f);drawLine(Cyan,Offset(w*.32f,h*.68f),Offset(w*.25f,h*.84f),3f)
    drawLine(Cyan,Offset(w*.50f,h*.68f),Offset(w*.50f,h*.86f),3f);drawLine(Cyan,Offset(w*.68f,h*.68f),Offset(w*.75f,h*.84f),3f)}
   else->{drawRoundRect(Color(0xFF3C475A),Offset(w*.27f,h*.08f),Size(w*.46f,h*.82f),androidx.compose.ui.geometry.CornerRadius(11f))
    drawLine(Color(0xFF111827),Offset(w*.50f,h*.11f),Offset(w*.50f,h*.87f),4f);drawCircle(Cyan,4f,Offset(w*.62f,h*.27f))}
  }
 }
}

@Composable private fun CategoryCard(cat:Cat,onClick:()->Unit){
 Surface(Modifier.clickable{onClick()},shape=RoundedCornerShape(20.dp),color=Color(0xFF07111F),border=BorderStroke(1.5.dp,cat.accent.copy(.62f))){
  Column(Modifier.padding(8.dp)){
   Art(cat,Modifier.height(103.dp));Spacer(Modifier.height(6.dp))
   Row(verticalAlignment=Alignment.CenterVertically){
    Column(Modifier.weight(1f)){Text(cat.name,color=White,fontSize=13.sp,fontWeight=FontWeight.Black,fontFamily=FontFamily.Monospace)
     Spacer(Modifier.height(3.dp));Text(cat.subtitle,color=Muted,fontSize=9.sp)}
    Text("›",color=cat.accent,fontSize=27.sp,fontWeight=FontWeight.Bold)
   }
  }
 }
}

@Composable private fun HomeSpecs(onSelect:(String,String)->Unit){
 val specs=listOf("BRAND" to "Apple","MODEL" to "Galaxy S25 Ultra","RAM" to "12GB","STORAGE" to "256GB","VARIANT" to "Black")
 Column{
  Row(verticalAlignment=Alignment.CenterVertically){
   Text("POPULAR SPECIFICATIONS",color=Muted,fontSize=9.sp,letterSpacing=1.7.sp,fontFamily=FontFamily.Monospace)
   Spacer(Modifier.weight(1f));Text("TAP TO SELECT",color=Cyan,fontSize=8.sp,fontFamily=FontFamily.Monospace)
  }
  Spacer(Modifier.height(8.dp))
  Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(10.dp)){
   specs.forEach{(a,b)->Surface(Modifier.width(150.dp).height(104.dp),shape=RoundedCornerShape(17.dp),color=Panel2,border=BorderStroke(1.2.dp,Cyan.copy(.55f))){
    Column(Modifier.padding(12.dp)){Text(a,color=Cyan,fontSize=8.sp,fontWeight=FontWeight.Bold,fontFamily=FontFamily.Monospace)
     Spacer(Modifier.height(14.dp));Text(b,color=White,fontSize=12.sp,maxLines=1);Spacer(Modifier.weight(1f));Text("SELECT  ⌄",Modifier.clickable{onSelect(a,b)},color=Cyan,fontSize=7.sp,fontFamily=FontFamily.Monospace)}
   }}
  }
 }
}

@Composable private fun ExactHomePanel(onCompare:()->Unit){
 Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(22.dp),color=Panel,border=BorderStroke(1.2.dp,Color(0xFF31405A))){
  Column(Modifier.padding(17.dp)){
   Row(verticalAlignment=Alignment.CenterVertically){
    Column(Modifier.weight(1f)){Text("EXACT PRODUCT MATCH",color=Cyan,fontSize=12.sp,fontWeight=FontWeight.Bold,letterSpacing=1.2.sp,fontFamily=FontFamily.Monospace)
     Spacer(Modifier.height(5.dp));Text("Brand + Model + Variant",color=White,fontSize=18.sp,fontWeight=FontWeight.ExtraBold)}
    Surface(shape=RoundedCornerShape(11.dp),color=Green.copy(.08f),border=BorderStroke(1.dp,Green.copy(.5f))){
     Text("SMART SEARCH",Modifier.padding(horizontal=8.dp,vertical=6.dp),color=Green,fontSize=7.sp,fontFamily=FontFamily.Monospace)}
   }
   Spacer(Modifier.height(14.dp))
   Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){
    listOf("EXACT MATCH" to Cyan,"REAL PRICE" to Green,"PRICE HISTORY" to Purple).forEach{(t,c)->
     Surface(Modifier.weight(1f),shape=RoundedCornerShape(12.dp),color=c.copy(.07f),border=BorderStroke(1.dp,c.copy(.35f))){
      Text(t,Modifier.padding(vertical=11.dp),textAlign=TextAlign.Center,color=c,fontSize=7.sp,fontWeight=FontWeight.Bold,fontFamily=FontFamily.Monospace)}}
   }
   Spacer(Modifier.height(13.dp))
   Button(onClick=onCompare,Modifier.fillMaxWidth().height(51.dp),shape=RoundedCornerShape(16.dp),colors=ButtonDefaults.buttonColors(containerColor=Cyan,contentColor=Color.Black)){
    Text("⌕  COMPARE & SHOP  ›",fontSize=12.sp,fontWeight=FontWeight.Black,fontFamily=FontFamily.Monospace)}
  }
 }
}

@Composable private fun HomeScreen(onCategory:(String)->Unit,onSearch:(String)->Unit){
 var query by remember{mutableStateOf("")}
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(Bg)){
  LogoHeader()
  Column(Modifier.padding(horizontal=18.dp)){
   Row(verticalAlignment=Alignment.Bottom){Text("Compare prices. ",color=White,fontSize=24.sp,fontWeight=FontWeight.Black);Text("Buy smarter.",color=Cyan,fontSize=24.sp,fontWeight=FontWeight.Black)}
   Spacer(Modifier.height(6.dp));Text("Find the exact model and compare verified retailer prices.",color=Color(0xFFBED3E5),fontSize=12.sp)
   Spacer(Modifier.height(22.dp));SearchBox(query,{query=it},{if(query.isNotBlank())onSearch(query)})
   Spacer(Modifier.height(25.dp))
   Row(verticalAlignment=Alignment.CenterVertically){Text("POPULAR CATEGORIES",color=Cyan,fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=1.5.sp,fontFamily=FontFamily.Monospace);Spacer(Modifier.weight(1f));Text("View All  ›",color=Muted,fontSize=9.sp)}
   Spacer(Modifier.height(10.dp))
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)){cats.take(3).forEach{cat->Box(Modifier.weight(1f)){CategoryCard(cat,{onCategory(cat.name)})}}}
   Spacer(Modifier.height(9.dp))
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)){cats.drop(3).forEach{cat->Box(Modifier.weight(1f)){CategoryCard(cat,{onCategory(cat.name)})}};Spacer(Modifier.weight(1f))}
   Spacer(Modifier.height(22.dp));HomeSpecs{a,b->query="$a: $b"};Spacer(Modifier.height(18.dp));ExactHomePanel{onCategory("SEARCH")};Spacer(Modifier.height(28.dp))
  }
 }
}

@Composable private fun SelectBox(label:String,value:String,options:List<String>,onSelect:(String)->Unit){
 var open by remember(label){mutableStateOf(false)}
 Box{
  Surface(Modifier.fillMaxWidth().height(123.dp).clickable{open=true},shape=RoundedCornerShape(19.dp),color=Panel2,border=BorderStroke(1.5.dp,Gold.copy(.6f))){
   Column(Modifier.padding(14.dp)){Text(label.uppercase(),color=Gold,fontSize=8.sp,fontWeight=FontWeight.Bold,fontFamily=FontFamily.Monospace)
    Spacer(Modifier.height(18.dp));Text(value,color=White,fontSize=14.sp,maxLines=1);Spacer(Modifier.weight(1f));Text("SELECT  ⌄",color=Cyan,fontSize=7.sp,fontFamily=FontFamily.Monospace)}
  }
  DropdownMenu(open,{open=false},modifier=Modifier.background(Panel2)){options.forEach{option->DropdownMenuItem(text={Text(option,color=White)},onClick={onSelect(option);open=false})}}
 }
}

@Composable private fun GeyserHero(){
 Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(24.dp),color=Color(0xFF091521),border=BorderStroke(1.5.dp,Cyan.copy(.55f))){
  Column(Modifier.padding(16.dp)){
   Row(verticalAlignment=Alignment.CenterVertically){
    Column(Modifier.weight(1.12f)){Text("FIND YOUR",color=White,fontSize=26.sp,fontWeight=FontWeight.Black);Text("GEYSER",color=Cyan,fontSize=27.sp,fontWeight=FontWeight.Black)
     Spacer(Modifier.height(8.dp));Text("Select specifications to find the exact model.",color=Muted,fontSize=11.sp,lineHeight=20.sp)}
    Art(cats[0],Modifier.weight(.88f).height(142.dp))
   }
   Spacer(Modifier.height(14.dp))
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){listOf("◇" to "Top Brands","₹" to "Best Prices","✓" to "Verified Retailers","▣" to "Compare & Buy").forEach{(i,t)->Column(horizontalAlignment=Alignment.CenterHorizontally){Text(i,color=Cyan,fontSize=22.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(5.dp));Text(t,color=Muted,fontSize=7.sp)}}}
  }
 }
}

@Composable private fun GeyserCard(name:String,price:String,accent:Color,onCompare:()->Unit){
 Surface(Modifier.width(225.dp),shape=RoundedCornerShape(20.dp),color=Panel,border=BorderStroke(1.2.dp,accent.copy(.5f))){
  Column(Modifier.padding(10.dp)){Art(cats[0],Modifier.height(112.dp));Spacer(Modifier.height(7.dp));Text(name,color=White,fontSize=13.sp,fontWeight=FontWeight.Bold)
   Text("15L  •  5★  •  2000W",color=Muted,fontSize=9.sp);Spacer(Modifier.height(5.dp));Text("From ₹$price",color=White,fontSize=15.sp,fontWeight=FontWeight.ExtraBold);Spacer(Modifier.height(7.dp))
   OutlinedButton(onClick=onCompare,Modifier.fillMaxWidth().height(36.dp),shape=RoundedCornerShape(10.dp),border=BorderStroke(1.dp,Cyan)){Text("Compare prices  →",color=Cyan,fontSize=8.sp,fontWeight=FontWeight.Bold)}
  }
 }
}

@Composable private fun GeyserScreen(onBack:()->Unit,onCompare:(String)->Unit){
 var brand by remember{mutableStateOf("Select Brand")};var type by remember{mutableStateOf("Storage")};var capacity by remember{mutableStateOf("15L")}
 var rating by remember{mutableStateOf("5★")};var power by remember{mutableStateOf("2000W")};var pressure by remember{mutableStateOf("6 Bar")}
 val rows=listOf(Triple("Brand",brand,listOf("Select Brand","Orient","Bajaj","Racold","Havells")),Triple("Type",type,listOf("Storage","Instant")),
  Triple("Capacity",capacity,listOf("6L","10L","15L","25L")),Triple("Star Rating",rating,listOf("3★","4★","5★")),
  Triple("Power",power,listOf("1500W","2000W","3000W")),Triple("Pressure",pressure,listOf("6 Bar","8 Bar")))
 val query=buildString{if(brand!="Select Brand")append(brand).append(" ");append(capacity).append(" ");append("geyser ").append(power).append(" ").append(rating)}
 Column(Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState())){
  Row(Modifier.fillMaxWidth().padding(horizontal=17.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically){
   Text("‹",Modifier.clickable{onBack()},color=Cyan,fontSize=40.sp);Spacer(Modifier.width(5.dp))
   Column(Modifier.weight(1f)){Text("GEYSER",color=White,fontSize=29.sp,fontWeight=FontWeight.Black,fontFamily=FontFamily.Monospace);Text("C O M P A R E  &  S H O P",color=Cyan,fontSize=9.sp,letterSpacing=2.5.sp,fontFamily=FontFamily.Monospace)}
   Surface(shape=RoundedCornerShape(17.dp),color=Color(0xFF071A2A),border=BorderStroke(1.3.dp,Cyan.copy(.8f))){Column(Modifier.padding(horizontal=13.dp,vertical=9.dp),horizontalAlignment=Alignment.CenterHorizontally){Text("Gurugram",color=White,fontSize=9.sp);Spacer(Modifier.height(7.dp));Text("122001",color=White,fontSize=10.sp)}}
  }
  Column(Modifier.padding(horizontal=14.dp)){
   GeyserHero();Spacer(Modifier.height(27.dp))
   Row(verticalAlignment=Alignment.CenterVertically){Text("PRODUCT SPECIFICATIONS",color=Muted,fontSize=9.sp,letterSpacing=1.5.sp,fontFamily=FontFamily.Monospace);Spacer(Modifier.weight(1f));Text("TAP TO SELECT",color=Gold,fontSize=8.sp,fontFamily=FontFamily.Monospace)}
   Spacer(Modifier.height(10.dp))
   Column(verticalArrangement=Arrangement.spacedBy(9.dp)){rows.chunked(2).forEach{pair->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)){pair.forEach{(label,value,opts)->Box(Modifier.weight(1f)){SelectBox(label,value,opts,{v->when(label){"Brand"->brand=v;"Type"->type=v;"Capacity"->capacity=v;"Star Rating"->rating=v;"Power"->power=v;"Pressure"->pressure=v}})}};if(pair.size==1)Spacer(Modifier.weight(1f))}}}
   Spacer(Modifier.height(18.dp))
   Button(onClick={onCompare(query)},Modifier.fillMaxWidth().height(58.dp),shape=RoundedCornerShape(18.dp),colors=ButtonDefaults.buttonColors(containerColor=Cyan,contentColor=Color.Black)){
    Text("⌕  FIND EXACT GEYSER",fontSize=12.sp,fontWeight=FontWeight.Black,fontFamily=FontFamily.Monospace)}
   Spacer(Modifier.height(25.dp))
   Row(verticalAlignment=Alignment.CenterVertically){Text("POPULAR GEYSERS",color=Cyan,fontSize=13.sp,fontWeight=FontWeight.ExtraBold,fontFamily=FontFamily.Monospace);Spacer(Modifier.weight(1f));Text("Live search  •  122001",color=Muted,fontSize=8.sp)}
   Spacer(Modifier.height(9.dp))
   Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(10.dp)){GeyserCard("Orient Aquator Neo","7,499",Gold){onCompare("Orient Aquator Neo 15L")};GeyserCard("Bajaj Shield Series","7,299",Cyan){onCompare("Bajaj Shield Series 15L")};GeyserCard("Havells Adonia","8,199",Purple){onCompare("Havells Adonia 15L")}}
   Spacer(Modifier.height(25.dp))
  }
 }
}

@Composable private fun CompareScreen(query:String,onBack:()->Unit){
 var loading by remember(query){mutableStateOf(true)};var error by remember(query){mutableStateOf<String?>(null)};var offers by remember(query){mutableStateOf<List<LiveOffer>>(emptyList())}
 LaunchedEffect(query){val r=fetchLiveOffers(query);r.onSuccess{offers=it;error=null}.onFailure{error=it.message};loading=false}
 Column(Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState())){
  Row(Modifier.fillMaxWidth().padding(17.dp),verticalAlignment=Alignment.CenterVertically){Text("‹",Modifier.clickable{onBack()},color=Cyan,fontSize=40.sp);Spacer(Modifier.width(8.dp));Column{Text("LIVE COMPARISON",color=White,fontSize=23.sp,fontWeight=FontWeight.Black,fontFamily=FontFamily.Monospace);Text("Gurugram • 122001",color=Cyan,fontSize=9.sp,letterSpacing=1.8.sp,fontFamily=FontFamily.Monospace)}}
  Column(Modifier.padding(horizontal=14.dp)){
   Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),color=Panel,border=BorderStroke(1.dp,Cyan.copy(.5f))){Column(Modifier.padding(15.dp)){Text("YOUR SEARCH",color=Muted,fontSize=8.sp,fontFamily=FontFamily.Monospace);Spacer(Modifier.height(6.dp));Text(query,color=White,fontSize=16.sp,fontWeight=FontWeight.Bold)}}
   Spacer(Modifier.height(15.dp))
   when{
    loading->Column(Modifier.fillMaxWidth().padding(35.dp),horizontalAlignment=Alignment.CenterHorizontally){CircularProgressIndicator(color=Cyan);Spacer(Modifier.height(12.dp));Text("Searching live retailers…",color=Muted,fontSize=11.sp)}
    error!=null->Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp),color=Color(0xFF28131A),border=BorderStroke(1.dp,Color.Red.copy(.5f))){Text("Live search error: "+error,Modifier.padding(15.dp),color=White,fontSize=11.sp)}
    offers.isEmpty()->Text("No matching live products found. Try a broader model or brand search.",color=Muted,fontSize=12.sp,modifier=Modifier.padding(18.dp))
    else->{
     val exact=offers.filter{it.matchType=="exact_match"};val close=offers.filter{it.matchType=="close_match"};val alt=offers.filter{it.matchType=="alternative"}
     Text(if(exact.isNotEmpty())"EXACT MATCH" else "MATCHED PRODUCTS",color=Cyan,fontSize=13.sp,fontWeight=FontWeight.Black,fontFamily=FontFamily.Monospace)
     Spacer(Modifier.height(9.dp))
     offers.sortedWith(compareBy({it.matchType!="exact_match"},{it.price})).forEach{offer->
      val accent=when(offer.matchType){"exact_match"->Green;"close_match"->Gold;else->Muted}
      Surface(Modifier.fillMaxWidth().padding(bottom=9.dp),shape=RoundedCornerShape(17.dp),color=Panel,border=BorderStroke(1.dp,accent.copy(.55f))){Column(Modifier.padding(14.dp)){
       Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(offer.retailer.uppercase(),color=accent,fontSize=8.sp,fontWeight=FontWeight.Bold,fontFamily=FontFamily.Monospace);Spacer(Modifier.height(5.dp));Text(offer.name,color=White,fontSize=12.sp,fontWeight=FontWeight.Bold);Text(offer.brand,color=Muted,fontSize=9.sp)};Text("₹"+String.format("%.0f",offer.price),color=White,fontSize=20.sp,fontWeight=FontWeight.Black)}
       Spacer(Modifier.height(8.dp));Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){Surface(shape=RoundedCornerShape(8.dp),color=accent.copy(.08f),border=BorderStroke(1.dp,accent.copy(.3f))){Text(offer.matchType.replace("_"," ").uppercase(),Modifier.padding(horizontal=8.dp,vertical=5.dp),color=accent,fontSize=7.sp,fontWeight=FontWeight.Bold,fontFamily=FontFamily.Monospace)};Text(if(offer.available)"IN STOCK" else "OUT OF STOCK",Modifier.padding(vertical=5.dp),color=if(offer.available)Green else Muted,fontSize=7.sp,fontFamily=FontFamily.Monospace)}}
      }
     }
    }
   }
   Spacer(Modifier.height(25.dp))
  }
 }
}

@Composable private fun GenericCategoryScreen(category:String,onBack:()->Unit,onCompare:(String)->Unit){
 var found by remember{mutableStateOf(false)}
 val title=when(category){"MOBILE"->"MOBILE";"TV"->"TV";"AC"->"AIR CONDITIONER";"FRIDGE"->"REFRIGERATOR";else->category}
 val specs=when(category){
  "MOBILE"->listOf("Brand" to listOf("Apple","Samsung","OnePlus","Xiaomi"),"Model" to listOf("Galaxy S25 Ultra","iPhone 17","OnePlus 13"),"RAM" to listOf("8GB","12GB","16GB"),"Storage" to listOf("128GB","256GB","512GB"),"Variant" to listOf("Black","Blue","Silver"))
  "TV"->listOf("Brand" to listOf("Sony","Samsung","LG","TCL"),"Size" to listOf("43 inch","50 inch","55 inch","65 inch"),"Panel" to listOf("LED","QLED","OLED"),"Resolution" to listOf("4K","Full HD"),"Refresh" to listOf("60Hz","120Hz"))
  "AC"->listOf("Brand" to listOf("Daikin","LG","Voltas","Carrier"),"Capacity" to listOf("1 Ton","1.5 Ton","2 Ton"),"Star Rating" to listOf("3★","4★","5★"),"Type" to listOf("Split","Window"),"Inverter" to listOf("Inverter","Non-Inverter"))
  else->listOf("Brand" to listOf("LG","Samsung","Whirlpool","Haier"),"Capacity" to listOf("190L","240L","300L","350L"),"Type" to listOf("Double Door","Single Door"),"Star Rating" to listOf("2★","3★","4★","5★"),"Compressor" to listOf("Inverter","Digital Inverter"))
 }
 var values by remember{mutableStateOf(specs.associate{it.first to it.second.first()})}
 Column(Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState())){
  Row(Modifier.fillMaxWidth().padding(17.dp),verticalAlignment=Alignment.CenterVertically){
   Text("‹",Modifier.clickable{onBack()},color=Cyan,fontSize=40.sp);Spacer(Modifier.width(6.dp))
   Column(Modifier.weight(1f)){Text(title,color=White,fontSize=27.sp,fontWeight=FontWeight.Black,fontFamily=FontFamily.Monospace);Text("C O M P A R E  &  S H O P",color=Cyan,fontSize=9.sp,letterSpacing=2.5.sp,fontFamily=FontFamily.Monospace)}
   Surface(shape=RoundedCornerShape(16.dp),color=Color(0xFF071A2A),border=BorderStroke(1.dp,Cyan)){Column(Modifier.padding(9.dp),horizontalAlignment=Alignment.CenterHorizontally){Text("Gurugram",color=White,fontSize=9.sp);Text("122001",color=Cyan,fontSize=10.sp)}}
  }
  Column(Modifier.padding(horizontal=14.dp)){
   Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(22.dp),color=Panel,border=BorderStroke(1.3.dp,Cyan.copy(.5f))){
    Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically){
     Column(Modifier.weight(1f)){Text("FIND YOUR",color=White,fontSize=23.sp,fontWeight=FontWeight.Black);Text(title,color=Cyan,fontSize=23.sp,fontWeight=FontWeight.Black);Spacer(Modifier.height(7.dp));Text("Select specifications to find the exact model.",color=Muted,fontSize=10.sp,lineHeight=18.sp)}
     Art(when(category){"MOBILE"->cats[1];"TV"->cats[2];"AC"->cats[3];else->cats[4]},Modifier.weight(.8f).height(125.dp))
    }
   }
   Spacer(Modifier.height(22.dp));Text("PRODUCT SPECIFICATIONS",color=Muted,fontSize=9.sp,letterSpacing=1.5.sp,fontFamily=FontFamily.Monospace);Spacer(Modifier.height(10.dp))
   Column(verticalArrangement=Arrangement.spacedBy(9.dp)){specs.chunked(2).forEach{pair->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)){pair.forEach{(label,opts)->Box(Modifier.weight(1f)){SelectBox(label,values[label]!!,opts){v->values=values.toMutableMap().also{it[label]=v};found=false}}};if(pair.size==1)Spacer(Modifier.weight(1f))}}}
   Spacer(Modifier.height(18.dp));val liveQuery=(values.values.filter{it.isNotBlank()}.joinToString(" ")+" "+title).trim();Button(onClick={if(liveQuery.isNotBlank()){found=true;onCompare(liveQuery)}},Modifier.fillMaxWidth().height(58.dp),shape=RoundedCornerShape(18.dp),colors=ButtonDefaults.buttonColors(containerColor=Cyan,contentColor=Color.Black)){Text(if(found)"✓  LIVE RESULTS" else "⌕  FIND EXACT "+title,fontSize=12.sp,fontWeight=FontWeight.Black,fontFamily=FontFamily.Monospace)}
   Spacer(Modifier.height(22.dp));Text(if(found)"MATCHED PRODUCTS" else "POPULAR "+title,color=Cyan,fontSize=13.sp,fontWeight=FontWeight.ExtraBold,fontFamily=FontFamily.Monospace);Spacer(Modifier.height(9.dp))
   Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(10.dp)){
    listOf("Featured ","Top Value ","Premium ","Best Seller").forEachIndexed{i,n->Surface(Modifier.width(215.dp),shape=RoundedCornerShape(19.dp),color=Panel,border=BorderStroke(1.dp,listOf(Gold,Cyan,Purple,Green)[i])){Column(Modifier.padding(10.dp)){Art(when(category){"MOBILE"->cats[1];"TV"->cats[2];"AC"->cats[3];else->cats[4]},Modifier.height(105.dp));Text(n+title,color=White,fontSize=12.sp,fontWeight=FontWeight.Bold);Text(if(category=="MOBILE")"12GB • 256GB" else if(category=="TV")"55 inch • 4K" else if(category=="AC")"1.5 Ton • 5★" else "300L • 4★",color=Muted,fontSize=9.sp);Spacer(Modifier.height(6.dp));Text("From ₹ 7,499",color=White,fontSize=14.sp,fontWeight=FontWeight.ExtraBold);Spacer(Modifier.height(5.dp));OutlinedButton(onClick={onCompare((n+title+" "+values.values.joinToString(" ")).trim())},Modifier.fillMaxWidth().height(35.dp),shape=RoundedCornerShape(10.dp),border=BorderStroke(1.dp,Cyan)){Text("Compare prices →",color=Cyan,fontSize=8.sp)}}}}
   }
   Spacer(Modifier.height(25.dp))
  }
 }
}

@Composable private fun Footer(tab:Int,onTab:(Int)->Unit){
 Surface(color=Color(0xFF080E18),border=BorderStroke(1.dp,Color(0xFF26364F))){
  Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(8.dp),horizontalArrangement=Arrangement.SpaceEvenly){
   listOf("⌂" to "Home","◷" to "History","♡" to "Saved","⚙" to "Settings").forEachIndexed{i,(icon,label)->
    Surface(Modifier.weight(1f).clickable{onTab(i)},shape=RoundedCornerShape(14.dp),color=if(tab==i)Cyan.copy(.10f) else Color.Transparent){
     Column(Modifier.padding(vertical=8.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(icon,color=if(tab==i)Cyan else Muted,fontSize=21.sp);Text(label,color=if(tab==i)White else Muted,fontSize=8.sp,fontFamily=FontFamily.Monospace)}
    }
   }
  }
 }
}

@Composable private fun MainShell(){
 var tab by remember{mutableStateOf(0)}
 var screen by remember{mutableStateOf("HOME")}
 var compareQuery by remember{mutableStateOf("")}
 Column(Modifier.fillMaxSize().background(Bg)){
  Box(Modifier.weight(1f)){
   when(screen){
    "HOME"->HomeScreen(
      onCategory={it->screen=if(it=="SEARCH"||it=="ALL")"SEARCH" else it},
      onSearch={q->compareQuery=q;screen="COMPARE"}
    )
    "GEYSER"->GeyserScreen({screen="HOME"}){q->compareQuery=q;screen="COMPARE"}
    "MOBILE","TV","AC","FRIDGE"->GenericCategoryScreen(screen,{screen="HOME"}){q->compareQuery=q;screen="COMPARE"}
    "SEARCH"->Box(Modifier.fillMaxSize().background(Bg)){LaunchedEffect(Unit){compareQuery=compareQuery;if(compareQuery.isNotBlank())screen="COMPARE"}}
    "COMPARE"->CompareScreen(compareQuery){screen="HOME"}
    else->Box(Modifier.fillMaxSize().background(Bg),contentAlignment=Alignment.Center){
      Column(horizontalAlignment=Alignment.CenterHorizontally){
       Text(when(tab){1->"PRICE HISTORY";2->"SAVED PRODUCTS";else->"SETTINGS"},color=Cyan,fontSize=18.sp,fontFamily=FontFamily.Monospace,fontWeight=FontWeight.Bold)
       Spacer(Modifier.height(12.dp));Text("Your comparison data and settings will appear here.",color=Muted,fontSize=11.sp,textAlign=TextAlign.Center)
       Spacer(Modifier.height(18.dp));Button(onClick={screen="HOME"},colors=ButtonDefaults.buttonColors(containerColor=Cyan,contentColor=Color.Black)){Text("← BACK HOME")}}
    }
   }
  }
  if(screen!="GEYSER")Footer(tab){tab=it;screen=if(it==0)"HOME" else "TAB"}
 }
}
