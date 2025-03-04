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

@Composable
fun AuthenticationAppScreen(navController: NavController, hexCode: String) {
    val firestore = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Log in detected! Are you trying to log in?",
            fontSize = 24.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                firestore.collection("runs").document(hexCode)
                    .update("authentication_app", "finished")
                    .addOnSuccessListener {
                        navController.navigate("loadingScreen/$hexCode")
                    }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
        ) {
            Text("APPROVE", color = Color.White, fontSize = 18.sp)
        }
    }

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