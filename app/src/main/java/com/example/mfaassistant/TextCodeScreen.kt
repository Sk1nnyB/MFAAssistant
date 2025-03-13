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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mfaassistant.ui.components.BottomBar
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
                title = { Text(stringResource(id = R.string.text_top_text), fontSize = 18.sp, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E73E6))
            )
        },
        bottomBar = { BottomBar(navController, runCode) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.Start
        ) {
            TextBubble(stringResource(id = R.string.first_text))
            Spacer(modifier = Modifier.height(8.dp))
            textCode?.let { stringResource(id = R.string.second_text, it) }?.let { TextBubble(it) }
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
