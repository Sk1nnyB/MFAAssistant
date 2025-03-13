package com.example.mfaassistant

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.example.mfaassistant.ui.components.BottomBar

@Composable
fun LoadingScreen(navController: NavController, runCode: String) {
    var textCode by remember { mutableStateOf<String?>(null) }
    var authAppStatus by remember { mutableStateOf<String?>(null) }
    var complete by remember { mutableStateOf<String?>(null) }
    var listenerRegistration: ListenerRegistration? by remember { mutableStateOf(null) }
    var navigated by remember { mutableStateOf(false) }

    val firestore = FirebaseFirestore.getInstance()

    LaunchedEffect(runCode) {
        listenerRegistration = firestore.collection("runs")
            .document(runCode)
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
                                navController.navigate("authenticationAppScreen/$runCode")
                            }
                            textCode == "started" -> {
                                navigated = true
                                navController.navigate("textCodeScreen/$runCode")
                            }
                            complete == "completed" -> {
                                navigated = true
                                navController.navigate("runInput")
                            }
                        }
                    }
                }
            }
    }

    Scaffold(
        topBar = {
        },
        bottomBar = { BottomBar(navController, runCode) }
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
            Text(stringResource(id = R.string.loading_text), style = MaterialTheme.typography.bodyLarge)
        }
    }
    DisposableEffect(runCode) {
        onDispose {
            listenerRegistration?.remove()
        }
    }
}

