from pathlib import Path
import re

p=Path("app/src/main/java/com/myretailprice/app/MainActivity.kt")
s=p.read_text()
if "private val Gold" not in s:
    s=s.replace("private val CyberMuted = Color(0xFF8EA3B8)","private val CyberMuted = Color(0xFF8EA3B8)\nprivate val Gold = Color(0xFFFFC857)")

# imports
if "import androidx.compose.foundation.Canvas" not in s:
    s=s.replace("import androidx.compose.foundation.BorderStroke\n", "import androidx.compose.foundation.BorderStroke\nimport androidx.compose.foundation.Canvas\n")
if "import androidx.compose.ui.geometry.Offset" not in s:
    s=s.replace("import androidx.compose.ui.graphics.Color\n", "import androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.geometry.Offset\nimport androidx.compose.ui.graphics.drawscope.Stroke\n")

# stronger branding and smaller tagline
s=re.sub(r'Text\("MY RETAIL PRICE", color = CyberText, fontSize = 22\.sp,\\s*fontWeight = FontWeight\.ExtraBold, fontFamily = FontFamily\.Monospace, letterSpacing = 1\.sp\)',
'''Box {
                    Text("MY RETAIL PRICE", Modifier.offset(1.dp, 3.dp), color = NeonCyan.copy(alpha=.35f),
                        fontSize = 30.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, letterSpacing = 1.1.sp)
                    Text("MY RETAIL PRICE", color = CyberText, fontSize = 30.sp,
                        fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, letterSpacing = 1.1.sp)
                }''', s)
s=s.replace('fontSize = 25.sp,\n            lineHeight = 30.sp, fontWeight = FontWeight.ExtraBold)', 'fontSize = 18.sp,\n            lineHeight = 23.sp, fontWeight = FontWeight.ExtraBold)')

# add product art before CategoryCard
art=r'''
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

'''
if "fun ProductArt(" not in s:
    s=s.replace("@Composable\nfun CategoryCard(", art+"@Composable\nfun CategoryCard(")

# category cards show product art
old='''Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(category.icon, color = category.color, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                if (selected) Text("●", color = category.color, fontSize = 9.sp)
            }
            Spacer(Modifier.height(10.dp))
            Text(category.name, color = CyberText, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace)
            Text(category.subtitle, color = CyberMuted, fontSize = 9.sp)
        }'''
new='''Column(Modifier.padding(10.dp)) {
            ProductArt(category)
            Text(category.name, color = CyberText, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace)
            Text(category.subtitle, color = CyberMuted, fontSize = 9.sp)
        }'''
s=s.replace(old,new)

# replace ProductSpecPanel with interactive five horizontal boxes
start=s.index("@Composable\nfun ProductSpecPanel")
end=s.index("@Composable\nfun ExactMatchPanel", start)
spec=r'''@Composable
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

'''
s=s[:start]+spec+s[end:]

# insert geyser screen before HighValueHome
insert_at=s.index("@Composable\nfun HighValueHome")
geyser=r'''@Composable
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

'''
if "fun GeyserScreen(" not in s:\n    s=s[:insert_at]+geyser+s[insert_at:]\n\n# Make selecting Geyser open its screen
s=s.replace('fun HighValueHome() {','fun HighValueHome(onGeyser: () -> Unit) {')
s=s.replace('''fun select(category: ProductCategory) {
        selected = category''','''fun select(category: ProductCategory) {
        if (category.name == "GEYSER") { onGeyser(); return }
        selected = category''')
# MainHome state + conditional
s=s.replace('''var selectedTab by remember { mutableStateOf(0) }

    Column''','''var selectedTab by remember { mutableStateOf(0) }
    var showGeyser by remember { mutableStateOf(false) }

    Column''')
s=s.replace('''if (selectedTab == 0) {
                HighValueHome()
            } else {''','''if (showGeyser) {
                GeyserScreen { showGeyser = false }
            } else if (selectedTab == 0) {
                HighValueHome { showGeyser = true }
            } else {''')
s=s.replace('''        FixedFooter(selectedTab) { selectedTab = it }''','''        if (!showGeyser) FixedFooter(selectedTab) { selectedTab = it }''')

p.write_text(s)
