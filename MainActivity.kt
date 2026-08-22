package br.com.gestao.adm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GestaoADM() }
    }
}

@Composable
fun GestaoADM() {
    MaterialTheme(colorScheme = lightColorScheme(
        primary = Color(0xFF102A43),
        secondary = Color(0xFFD99A2B),
        surface = Color(0xFFF8F7F3)
    )) {
        val repo = remember { AuthRepository() }
        val scope = rememberCoroutineScope()
        var profile by remember { mutableStateOf<UserProfile?>(null) }
        var error by remember { mutableStateOf("") }
        var loading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            profile = runCatching { repo.currentProfile() }.getOrNull()
            loading = false
        }

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (profile == null) {
            LoginReal(
                error = error,
                onLogin = { email, password ->
                    scope.launch {
                        loading = true
                        val result = repo.signIn(email, password)
                        result.onSuccess { profile = it }.onFailure { error = it.message ?: "Falha no login." }
                        loading = false
                    }
                }
            )
        } else {
            DashboardReal(profile!!, onLogout = {
                repo.signOut()
                profile = null
            })
        }
    }
}

@Composable
fun LoginReal(error: String, onLogin: (String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Gestão ADM", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("Assembleia de Deus — Ministério de Madureira", color = Color.Gray)
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("E-mail") })
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth(), label = { Text("Senha") })
        if (error.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onLogin(email, password) },
            enabled = email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Entrar") }
        Spacer(Modifier.height(12.dp))
        Text("Acesso protegido por autenticação. As permissões são definidas no servidor.", color = Color.Gray)
    }
}

@Composable
fun DashboardReal(profile: UserProfile, onLogout: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Gestão ADM") }) }) { p ->
        Column(Modifier.padding(p).padding(20.dp)) {
            Text("Olá, ${profile.name}", style = MaterialTheme.typography.titleLarge)
            Text("Perfil: ${profile.role.name.replace('_', ' ')}", color = Color.Gray)
            Spacer(Modifier.height(22.dp))
            Text("Assembleia de Deus — Ministério de Madureira", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(20.dp))
            Text("Acesso controlado pelo perfil do usuário.")
            Spacer(Modifier.height(20.dp))
            Button(onClick = onLogout, Modifier.fillMaxWidth()) { Text("Sair") }
        }
    }
}
