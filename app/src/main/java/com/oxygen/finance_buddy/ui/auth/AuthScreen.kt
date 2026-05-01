package com.oxygen.finance_buddy.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.oxygen.finance_buddy.R

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.authState.collectAsState()
    var pinText by remember { mutableStateOf("") }
    
    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            onAuthSuccess()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            is AuthState.Loading -> CircularProgressIndicator()
            is AuthState.SetupPin -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Logo Image
                    Image(
                        painter = painterResource(id = R.drawable.f_logo),
                        contentDescription = "Finclear Logo",
                        modifier = Modifier.size(120.dp).padding(bottom = 16.dp)
                    )
                    Text(
                        text = "Benvenuto in Finclear!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Configura il tuo PIN")
                    OutlinedTextField(
                        value = pinText,
                        onValueChange = { pinText = it },
                        label = { Text("Nuovo PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    Button(onClick = { if (pinText.length >= 4) viewModel.setupPin(pinText) }) {
                        Text("Salva ed Entra")
                    }
                }
            }
            is AuthState.EnterPin, is AuthState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Logo Image
                    Image(
                        painter = painterResource(id = R.drawable.f_logo),
                        contentDescription = "Finclear Logo",
                        modifier = Modifier.size(120.dp).padding(bottom = 16.dp)
                    )
                    Text(
                        text = "Bentornato in Finclear!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Inserisci il PIN per accedere")
                    if (state is AuthState.Error) {
                        Text((state as AuthState.Error).message, color = MaterialTheme.colorScheme.error)
                    }
                    OutlinedTextField(
                        value = pinText,
                        onValueChange = { pinText = it },
                        label = { Text("PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    Button(onClick = { viewModel.verifyPin(pinText) }) {
                        Text("Accedi")
                    }
                }
            }
            else -> {}
        }
    }
}
