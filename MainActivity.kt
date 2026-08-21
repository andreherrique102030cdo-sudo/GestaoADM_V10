package br.com.gestao.adm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private val Navy = Color(0xFF17365D)
private val Gold = Color(0xFFD99A18)
private val Cream = Color(0xFFF8F4EA)
private val Brown = Color(0xFF4B3925)
private val Green = Color(0xFF2E8B57)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GestaoADM() }
    }
}

@Composable
fun GestaoADM() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Navy,
            secondary = Gold,
            background = Cream,
            surface = Color.White,
            onPrimary = Color.White
        )
    ) {
        val repo = remember { AuthRepository() }
        val scope = rememberCoroutineScope()
        var profile by remember { mutableStateOf<UserProfile?>(null) }
        var error by remember { mutableStateOf("") }
        var loading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            profile = runCatching { repo.currentProfile() }.getOrNull()
            loading = false
        }

        when {
            loading -> LoadingScreen()
            profile == null -> LoginScreen(error) { email, password ->
                scope.launch {
                    loading = true
                    error = ""
                    repo.signIn(email, password)
                        .onSuccess { profile = it }
                        .onFailure { error = it.message ?: "Falha no login." }
                    loading = false
                }
            }
            else -> MainShell(profile!!, repo::signOut) { profile = null }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Gold)
    }
}

@Composable
private fun LoginScreen(error: String, onLogin: (String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Surface(Modifier.fillMaxSize(), color = Cream) {
        Column(
            Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("✚", color = Gold, style = MaterialTheme.typography.displayMedium)
            Text("GESTÃO ADM", color = Navy, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Assembleia de Deus — Ministério de Madureira", textAlign = TextAlign.Center, color = Brown)
            Spacer(Modifier.height(28.dp))
            OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("E-mail ou usuário") }, singleLine = true)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth(), label = { Text("Senha") }, singleLine = true)
            if (error.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(error, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = { onLogin(email, password) },
                enabled = email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("ENTRAR") }
            Spacer(Modifier.height(14.dp))
            Text("Acesso administrativo protegido por autenticação.", color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}

private enum class AppPage(val title: String, val icon: String) {
    INICIO("Início", "⌂"),
    PESSOAS("Pessoas", "♙"),
    AGENDA("Agenda", "▣"),
    FINANCAS("Finanças", "R$"),
    MAIS("Mais", "☰")
}

@Composable
private fun MainShell(profile: UserProfile, signOut: () -> Unit, onLoggedOut: () -> Unit) {
    var page by remember { mutableStateOf(AppPage.INICIO) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (page == AppPage.INICIO) "Gestão ADM" else page.title, fontWeight = FontWeight.Bold) },
                navigationIcon = { Text("✚", Modifier.padding(start = 16.dp), color = Gold) }
            )
        },
        bottomBar = {
            Row(Modifier.fillMaxWidth().background(Navy).padding(vertical = 7.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                AppPage.entries.forEach { item ->
                    Column(
                        Modifier.clickable { page = item }.padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(item.icon, color = if (page == item) Gold else Color.White, fontWeight = FontWeight.Bold)
                        Text(item.title, color = if (page == item) Gold else Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    ) { padding ->
        when (page) {
            AppPage.INICIO -> DashboardPage(profile, padding)
            AppPage.PESSOAS -> PeoplePage(padding)
            AppPage.AGENDA -> AgendaPage(padding)
            AppPage.FINANCAS -> FinancePage(padding)
            AppPage.MAIS -> MorePage(profile, signOut) { onLoggedOut() }
        }
    }
}

@Composable
private fun DashboardPage(profile: UserProfile, padding: androidx.compose.foundation.layout.PaddingValues) {
    LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Olá, ${profile.name}!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Navy)
            Text("Aqui está o resumo da sua igreja.", color = Color.Gray)
            Spacer(Modifier.height(6.dp))
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("MEMBROS", "412", "cadastrados", Gold, Modifier.weight(1f))
                MetricCard("VISITANTES", "89", "este mês", Green, Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("LÍDERES", "25", "ativos", Gold, Modifier.weight(1f))
                MetricCard("DEPARTAMENTOS", "12", "cadastrados", Navy, Modifier.weight(1f))
            }
        }
        item {
            SectionCard("Acompanhamento") {
                SummaryRow("Visitantes sem contato", "7")
                SummaryRow("Aguardando retorno", "4")
                SummaryRow("Acompanhados", "12")
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, detail: String, accent: Color, modifier: Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text(label, color = accent, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Navy)
            Text(detail, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun PeoplePage(padding: androidx.compose.foundation.layout.PaddingValues) {
    val repo = remember { PeopleRepository() }
    val scope = rememberCoroutineScope()
    var people by remember { mutableStateOf<List<Person>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    var showForm by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    fun reload() {
        scope.launch {
            loading = true
            error = ""
            runCatching { repo.list() }
                .onSuccess { people = it }
                .onFailure { error = it.message ?: "Não foi possível carregar as pessoas." }
            loading = false
        }
    }

    LaunchedEffect(Unit) { reload() }

    if (showForm) {
        PersonFormPage(
            padding = padding,
            onBack = { showForm = false },
            onSave = { person ->
                scope.launch {
                    runCatching { repo.create(person) }
                        .onSuccess { showForm = false; reload() }
                        .onFailure { error = it.message ?: "Não foi possível salvar o cadastro." }
                }
            },
            error = error
        )
        return
    }

    val filtered = people.filter { it.name.contains(query, ignoreCase = true) }
    Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Pessoas", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Navy)
                Text("Cadastro da igreja", color = Color.Gray)
            }
            Button(onClick = { error = ""; showForm = true }) { Text("+ NOVO") }
        }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), label = { Text("Buscar pessoa...") }, singleLine = true)
        if (error.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(8.dp))
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Gold) }
        } else if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (people.isEmpty()) "Nenhuma pessoa cadastrada ainda." else "Nenhuma pessoa encontrada.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(filtered, key = { it.id }) { person ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(Modifier.size(42.dp), shape = RoundedCornerShape(21.dp), color = Cream) {
                                Box(contentAlignment = Alignment.Center) { Text("●", color = Gold) }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(person.name, fontWeight = FontWeight.SemiBold)
                                Text(person.role + if (person.phone.isNotBlank()) " • ${person.phone}" else "", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                            }
                            Text("›", color = Navy, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonFormPage(
    padding: androidx.compose.foundation.layout.PaddingValues,
    onBack: () -> Unit,
    onSave: (Person) -> Unit,
    error: String
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var maritalStatus by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Membro") }

    Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onBack) { Text("VOLTAR") }
            Spacer(Modifier.width(12.dp))
            Text("Novo cadastro", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Navy)
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Nome completo *") }, singleLine = true) }
            item { OutlinedTextField(phone, { phone = it }, Modifier.fillMaxWidth(), label = { Text("Telefone") }, singleLine = true) }
            item { OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("E-mail") }, singleLine = true) }
            item { OutlinedTextField(birthDate, { birthDate = it }, Modifier.fillMaxWidth(), label = { Text("Data de nascimento") }, singleLine = true) }
            item { OutlinedTextField(maritalStatus, { maritalStatus = it }, Modifier.fillMaxWidth(), label = { Text("Estado civil") }, singleLine = true) }
            item { OutlinedTextField(role, { role = it }, Modifier.fillMaxWidth(), label = { Text("Função na igreja") }, singleLine = true) }
            if (error.isNotBlank()) item { Text(error, color = MaterialTheme.colorScheme.error) }
            item {
                Button(onClick = { if (name.isNotBlank()) onSave(Person(name = name.trim(), role = role.trim().ifBlank { "Membro" }, phone = phone.trim(), email = email.trim(), birthDate = birthDate.trim(), maritalStatus = maritalStatus.trim())) }, enabled = name.isNotBlank(), Modifier.fillMaxWidth()) {
                    Text("SALVAR CADASTRO")
                }
            }
        }
    }
}

@Composable
private fun AgendaPage(padding: androidx.compose.foundation.layout.PaddingValues) {
    val events = listOf("24 MAI" to "Culto de Oração", "26 MAI" to "Escola Bíblica", "28 MAI" to "Ensaio do Louvor", "31 MAI" to "Culto da Família")
    LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("Agenda", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Navy) }
        items(events) { (date, event) ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(date, color = Gold, fontWeight = FontWeight.Bold, modifier = Modifier.width(58.dp))
                    Column { Text(event, fontWeight = FontWeight.SemiBold); Text("Templo Sede • 19:30", color = Color.Gray, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
    }
}

@Composable
private fun FinancePage(padding: androidx.compose.foundation.layout.PaddingValues) {
    Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
        Text("Finanças", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Navy)
        Spacer(Modifier.height(12.dp))
        SectionCard("Resumo do mês") {
            SummaryRow("Entradas", "R$ 12.450,00", Green)
            SummaryRow("Saídas", "R$ 7.850,00", Color.Red)
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            SummaryRow("Saldo", "R$ 4.600,00", Navy)
        }
        Spacer(Modifier.height(12.dp))
        SectionCard("Categorias") {
            SummaryRow("Dízimos", "45%")
            SummaryRow("Ofertas", "25%")
            SummaryRow("Doações", "15%")
            SummaryRow("Outros", "15%")
        }
    }
}

@Composable
private fun MorePage(profile: UserProfile, signOut: () -> Unit, onLoggedOut: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Mais", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Navy)
        Spacer(Modifier.height(10.dp))
        Text("Perfil: ${profile.role.label}", color = Color.Gray)
        Spacer(Modifier.height(18.dp))
        listOf("Relatórios", "Configurações", "Usuários e permissões", "Backup e segurança", "Dados da igreja").forEach {
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(it, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    Text("›", color = Navy)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Button(onClick = { signOut(); onLoggedOut() }, Modifier.fillMaxWidth()) { Text("SAIR") }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, color = Brown, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, valueColor: Color = Navy) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), color = Color.DarkGray)
        Text(value, color = valueColor, fontWeight = FontWeight.Bold)
    }
}
