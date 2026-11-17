package com.example.githubusers

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GitHubScreen()
                }
            }
        }
    }
}

@Composable
fun GitHubScreen(vm: GitHubViewModel = viewModel()) {
    val uiState by vm.uiState.collectAsState()
    val selectedUser by vm.selectedUser.collectAsState()
    val retryCount by vm.retryCount.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        vm.load()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = { vm.selectUser("no-such-login-xyz") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Test 404 (no-such-login-xyz)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Success -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.users, key = { it.id }) { user ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { vm.selectUser(user.login) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = user.login, style = MaterialTheme.typography.titleMedium)
                                if (selectedUser?.login == user.login) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "ID: ${selectedUser?.id}")
                                    Text(text = "Avatar: ${selectedUser?.avatarUrl}")
                                }
                            }
                        }
                    }
                }
            }
            is UiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = state.message, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (retryCount < 3) {
                        Button(onClick = { vm.load(isRetry = true) }) {
                            Text("Retry ($retryCount/3)")
                        }
                    } else {
                        Button(onClick = {
                            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                        }) {
                            Text("Open Wi-Fi settings")
                        }
                    }
                }
            }
        }
    }
}
