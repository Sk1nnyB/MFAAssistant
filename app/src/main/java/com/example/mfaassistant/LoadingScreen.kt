package com.example.mfaassistant

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

@Composable
fun LoadingScreen(navController: NavController, hexCode: String) {
    var textCode by remember { mutableStateOf<String?>(null) }
    var authAppStatus by remember { mutableStateOf<String?>(null) }
    var complete by remember { mutableStateOf<String?>(null) }
    var listenerRegistration: ListenerRegistration? by remember { mutableStateOf(null) }
    var navigated by remember { mutableStateOf(false) }

    val firestore = FirebaseFirestore.getInstance()

    LaunchedEffect(hexCode) {
        listenerRegistration = firestore.collection("runs")
            .document(hexCode)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    textCode = snapshot.getString("text_task").orEmpty()
                    authAppStatus = snapshot.getString("authentication_app").orEmpty()
                    complete = snapshot.getString("status").orEmpty()

                    if (!navigated) {
                        when {
                            authAppStatus == "started" -> {
                                navigated = true
                                navController.navigate("authenticationAppScreen/$hexCode")
                            }
                            textCode == "started" -> {
                                navigated = true
                                navController.navigate("textCodeScreen/$hexCode")
                            }
                            complete == "completed" -> {
                                navigated = true
                                navController.navigate("hexInput")
                            }
                        }
                    }
                }
            }
    }

    Scaffold(
        topBar = {
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
                    Text("Now Playing Run: $hexCode")
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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Waiting for activity", style = MaterialTheme.typography.bodyLarge)
        }
    }
    DisposableEffect(hexCode) {
        onDispose {
            listenerRegistration?.remove()
        }
    }
}

