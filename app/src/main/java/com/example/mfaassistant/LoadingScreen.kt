package com.example.mfaassistant

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text("Waiting for activity", style = MaterialTheme.typography.bodyLarge)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Now playing run: $hexCode")
            Button(onClick = { navController.navigate("hexInput") }) {
                Text("Exit")
            }
        }
    }

    DisposableEffect(hexCode) {
        onDispose {
            listenerRegistration?.remove()
        }
    }
}

