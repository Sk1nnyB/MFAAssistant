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
fun TextCodeScreen(navController: NavController, hexCode: String) {
    var textCode by remember { mutableStateOf<String?>(null) }
    var textStatus by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var listenerRegistration: ListenerRegistration? by remember { mutableStateOf(null) }
    var navigated by remember { mutableStateOf(false) }

    // Initialize Firestore
    val firestore = FirebaseFirestore.getInstance()

    // Coroutine for fetching data
    LaunchedEffect(hexCode) {
        // Fetch document based on "code" field matching runCode
        firestore.collection("runs") // Replace with your collection name
            .whereEqualTo("code", hexCode) // Searching for matching "code"
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents.first()
                    val retrievedTextCode = document.getLong("text_code")?.toString()
                    textCode = retrievedTextCode

                }
                isLoading = false
            }
        listenerRegistration = firestore.collection("runs")
            .document(hexCode)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    textStatus = snapshot.getString("text_task").orEmpty()

                    when {
                        textStatus == "finished" -> {
                            navController.navigate("loadingScreen/$hexCode")
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
        Text("Text Code Activity", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Text("Text Code: $textCode", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Bottom exit bar
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
    }
}
