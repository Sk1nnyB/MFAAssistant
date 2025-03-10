package com.example.mfaassistant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextCodeScreen(navController: NavController, runCode: String) {
    var textCode by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var listenerRegistration: ListenerRegistration? by remember { mutableStateOf(null) }

    val firestore = FirebaseFirestore.getInstance()

    LaunchedEffect(runCode) {
        firestore.collection("runs")
            .whereEqualTo("code", runCode)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents.first()
                    textCode = document.getLong("text_code")?.toString()
                }
                isLoading = false
            }

        listenerRegistration = firestore.collection("runs")
            .document(runCode)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    val textStatus = snapshot.getString("text_task").orEmpty()
                    if (textStatus == "finished") {
                        navController.navigate("loadingScreen/$runCode")
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("< Man-Check-Ster", fontSize = 18.sp, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E73E6))
            )
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.height(80.dp),
                containerColor = Color(0xFFE0E0E0)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Now Playing Run: $runCode")
                    Button(
                        onClick = { navController.navigate("hexInput") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E73E6))
                    ) {
                        Text("Exit", color = Color.White)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.Start
        ) {
            TextBubble("Hey! We've noticed you've attempted to log into your Manchester Email!")
            Spacer(modifier = Modifier.height(8.dp))
            TextBubble("Your code is: ${textCode ?: "..."}")
        }
    }
}

@Composable
fun TextBubble(message: String) {
    Box(
        modifier = Modifier
            .wrapContentWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF4CAF50))
            .padding(12.dp)
    ) {
        Text(message, color = Color.White, fontSize = 16.sp)
    }
}
