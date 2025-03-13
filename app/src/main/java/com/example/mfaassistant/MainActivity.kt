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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
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
                var runCode by remember { mutableStateOf("") }

                NavHost(navController, startDestination = "runInput") {
                    composable("runInput") {
                        InputScreen(navController, onSubmit = { code ->
                            navController.navigate("loadingScreen/$code") // Correct navigation
                        })
                    }
                    composable("loadingScreen/{runCode}") { backStackEntry ->
                        val runCode = backStackEntry.arguments?.getString("runCode") ?: ""
                        LoadingScreen(navController, runCode)
                    }
                    composable("authenticationAppScreen/{runCode}") { backStackEntry ->
                        val runCode = backStackEntry.arguments?.getString("runCode") ?: ""
                        AuthenticationAppScreen(navController, runCode)
                    }
                    composable("textCodeScreen/{runCode}") { backStackEntry ->
                        val runCode = backStackEntry.arguments?.getString("runCode") ?: ""
                        TextCodeScreen(navController, runCode)
                    }
                }
            }
        }
    }
}

@Composable
fun InputScreen(navController: androidx.navigation.NavController, onSubmit: (String) -> Unit) {
    var runCode by remember { mutableStateOf("") }
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
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title at the top
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)) // Semi-transparent
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                    text = stringResource(id = R.string.app_name),
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
                        text = stringResource(id = R.string.main_paragraph_1),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                        modifier = Modifier.padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = stringResource(id = R.string.main_paragraph_2),
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                        modifier = Modifier.padding(bottom = 24.dp),
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = runCode,
                        onValueChange = {
                            if (it.length <= 8 && it.matches(Regex("[0-9A-Fa-f]*"))) {
                                runCode = it
                            }
                        },
                        label = { Text(stringResource(id = R.string.run_code_input_label)) },
                        textStyle = TextStyle(color = Color.Black),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            isChecking = true
                            coroutineScope.launch {
                                val isValid = isValidRun(runCode)
                                isChecking = false

                                if (isValid) {
                                    onSubmit(runCode)
                                    navController.navigate("loadingScreen/$runCode")
                                } else {
                                    showAlert = true
                                }
                            }
                        },
                        enabled = runCode.length == 8 && !isChecking,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isChecking) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Text(stringResource(id = R.string.submit_button))
                        }
                    }
                }
            }
        }

        if (showAlert) {
            AlertDialog(
                onDismissRequest = { showAlert = false },
                title = { Text(stringResource(id = R.string.error_title)) },
                text = { Text(stringResource(id = R.string.run_code_error)) },
                confirmButton = {
                    Button(onClick = { showAlert = false }) {
                        Text(stringResource(id = R.string.okay_button))
                    }
                }
            )
        }
    }
}

suspend fun isValidRun(runCode: String): Boolean {
    val db = FirebaseFirestore.getInstance()

    return try {
        val querySnapshot = db.collection("runs")
            .whereEqualTo("code", runCode)
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