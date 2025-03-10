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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticationAppScreen(navController: NavController, hexCode: String) {
    val firestore = FirebaseFirestore.getInstance()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Authentic Authenticator", fontSize = 18.sp, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E73E6))
            )
        },
        bottomBar = {
            Column {
                BottomAppBar(
                    modifier = Modifier.height(72.dp),
                    containerColor = Color(0xFF1E73E6)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Codes                     Accounts", color = Color.White)
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE0E0E0))
                        .padding(16.dp),
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ChatBubble("Log in detected! Are you trying to log in?")
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    firestore.collection("runs").document(hexCode)
                        .update("authentication_app", "finished")
                        .addOnSuccessListener {
                            navController.navigate("loadingScreen/$hexCode")
                        }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E73E6)),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text("Approve", color = Color.White, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun ChatBubble(message: String) {
    Box(
        modifier = Modifier
            .wrapContentWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE53935))
            .padding(12.dp)
    ) {
        Text(message, color = Color.White, fontSize = 16.sp)
    }
}
