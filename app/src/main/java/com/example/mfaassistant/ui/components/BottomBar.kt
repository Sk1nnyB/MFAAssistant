package com.example.mfaassistant.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mfaassistant.R

@Composable
fun BottomBar(navController: NavController, runCode: String) {
    BottomAppBar(
        modifier = Modifier.height(80.dp),
        containerColor = Color(0xFFE0E0E0)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(id = R.string.bottom_bar_text, runCode), color = Color.Black)
            Button(
                onClick = { navController.navigate("runInput") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E73E6))
            ) {
                Text(stringResource(id = R.string.exit_text), color = Color.White)
            }
        }
    }
}

