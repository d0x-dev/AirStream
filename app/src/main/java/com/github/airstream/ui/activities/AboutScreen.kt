package com.github.airstream.ui.activities

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.github.airstream.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val openLink = { url: String ->
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
    
    val gold = Color(0xFFD4AF37)
    val goldBg = Color(0x20D4AF37)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xA0403020)
                ),
                modifier = Modifier.clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Logo
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text("🎵", fontSize = 48.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("AirBeats", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            
            Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape) {
                    Text("VERSION {BuildConfig.VERSION_NAME}", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape) {
                    Text("NIGHTLY", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Text("Dev By DxV STUDIO 👑", modifier = Modifier.padding(top = 12.dp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            
            // Global links
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text("FB", color = gold, modifier = Modifier.clickable { openLink("https://facebook.com") })
                    Text("IG", color = gold, modifier = Modifier.clickable { openLink("https://instagram.com") })
                    Text("GH", color = gold, modifier = Modifier.clickable { openLink("https://github.com") })
                    Text("GO", color = gold, modifier = Modifier.clickable { openLink("https://google.com") })
                    Text("WEB", color = gold, modifier = Modifier.clickable { openLink("https://darkboy.pro") })
                }
            }
            
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Founders", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("2 Founders", color = gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Darkboy
                Card(
                    modifier = Modifier.weight(1f).height(240.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, gold)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp).clickable { openLink("https://github.com/d0x-dev") },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = "https://avatars.githubusercontent.com/u/218248866",
                            contentDescription = null,
                            modifier = Modifier.size(76.dp).clip(CircleShape).border(1.5.dp, gold, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Darkboy", fontWeight = FontWeight.Bold)
                        Surface(color = goldBg, shape = CircleShape, modifier = Modifier.padding(top = 4.dp)) {
                            Text("Lead Developer", color = gold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                        }
                        Surface(color = goldBg, shape = CircleShape, modifier = Modifier.padding(top = 4.dp)) {
                            Text("3423 Commits", color = gold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                        }
                    }
                }
                
                // Venom
                Card(
                    modifier = Modifier.weight(1f).height(240.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, gold)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp).clickable { openLink("https://github.com/drkvenom786") },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = "https://avatars.githubusercontent.com/u/241423835",
                            contentDescription = null,
                            modifier = Modifier.size(76.dp).clip(CircleShape).border(1.5.dp, gold, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Venom", fontWeight = FontWeight.Bold)
                        Surface(color = goldBg, shape = CircleShape, modifier = Modifier.padding(top = 4.dp)) {
                            Text("UI/UX Specialist", color = gold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                        }
                        Surface(color = goldBg, shape = CircleShape, modifier = Modifier.padding(top = 4.dp)) {
                            Text("3 Commits", color = gold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }
    }
}