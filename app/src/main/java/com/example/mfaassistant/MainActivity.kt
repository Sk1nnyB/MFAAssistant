package com.example.mfaassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mfaassistant.ui.theme.MFAAssistantTheme
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MFAAssistantTheme {
                val navController = rememberNavController()
                var hexCode by remember { mutableStateOf("") }

                NavHost(navController, startDestination = "hexInput") {
                    composable("hexInput") {
                        HexInputScreen(navController, onHexSubmit = { code ->
                            navController.navigate("loadingScreen/$code") // Correct navigation
                        })
                    }
                    composable("loadingScreen/{hexCode}") { backStackEntry ->
                        val hexCode = backStackEntry.arguments?.getString("hexCode") ?: ""
                        LoadingScreen(navController, hexCode)
                    }
                    composable("authenticationAppScreen/{hexCode}") { backStackEntry ->
                        val hexCode = backStackEntry.arguments?.getString("hexCode") ?: ""
                        AuthenticationAppScreen(navController, hexCode)
                    }
                    composable("textCodeScreen/{hexCode}") { backStackEntry ->
                        val hexCode = backStackEntry.arguments?.getString("hexCode") ?: ""
                        TextCodeScreen(navController, hexCode)
                    }
                }
            }
        }
    }
}

@Composable
fun HexInputScreen(navController: androidx.navigation.NavController, onHexSubmit: (String) -> Unit) {
    var hexCode by remember { mutableStateOf("") }
    var showAlert by remember { mutableStateOf(false) }
    var isChecking by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.authenticators),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title at the top
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)) // Semi-transparent
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                    text = "The MFA Assistant",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                    )
                }
            }

            // Content Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)) // Semi-transparent
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "The MFA Assistant helps you complete tasks using your real phone!",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                        modifier = Modifier.padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Just put in your 8-letter run code below to connect to your web browser!",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                        modifier = Modifier.padding(bottom = 24.dp),
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = hexCode,
                        onValueChange = {
                            if (it.length <= 8 && it.matches(Regex("[0-9A-Fa-f]*"))) {
                                hexCode = it
                            }
                        },
                        label = { Text("Enter 8-digit hex code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            isChecking = true
                            coroutineScope.launch {
                                val isValid = isValidRun(hexCode)
                                isChecking = false

                                if (isValid) {
                                    onHexSubmit(hexCode)
                                    navController.navigate("loadingScreen/$hexCode")
                                } else {
                                    showAlert = true
                                }
                            }
                        },
                        enabled = hexCode.length == 8 && !isChecking,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isChecking) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Text("Submit")
                        }
                    }
                }
            }
        }

        if (showAlert) {
            AlertDialog(
                onDismissRequest = { showAlert = false },
                title = { Text("Error") },
                text = { Text("No run with a matching code found") },
                confirmButton = {
                    Button(onClick = { showAlert = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}




suspend fun isValidRun(hexCode: String): Boolean {
    val db = FirebaseFirestore.getInstance()

    return try {
        val querySnapshot = db.collection("runs")
            .whereEqualTo("code", hexCode)
            .whereEqualTo("status", "active")
            .whereEqualTo("phone", true)
            .limit(1)
            .get()
            .await()

        !querySnapshot.isEmpty
    } catch (e: Exception) {
        false
    }
}

@Preview(showBackground = true)
@Composable
fun HexInputScreenPreview() {
    MFAAssistantTheme {
        HexInputScreen(navController = rememberNavController(), onHexSubmit = {})
    }
}
