package com.example.footpronostic.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.footpronostic.ui.viewmodel.PronosticViewModel
import kotlinx.coroutines.launch

/**
 * Écran pour modifier un pronostic existant.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPronosticScreen(
    pronosticId: String,
    userId: String,
    onNavigateBack: () -> Unit,
    pronosticViewModel: PronosticViewModel = viewModel()
) {
    val pronostics by pronosticViewModel.pronostics.collectAsState()
    val pronostic = pronostics.find { it.id == pronosticId }

    val isLoading by pronosticViewModel.isLoading.collectAsState()
    val errorMessage by pronosticViewModel.errorMessage.collectAsState()
    val successMessage by pronosticViewModel.successMessage.collectAsState()

    var scoreA by remember { mutableStateOf(pronostic?.predictedScoreA?.toString() ?: "") }
    var scoreB by remember { mutableStateOf(pronostic?.predictedScoreB?.toString() ?: "") }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Charger les pronostics si nécessaire
    LaunchedEffect(userId) {
        if (pronostic == null) {
            pronosticViewModel.loadUserPronostics(userId)
        }
    }

    // Initialiser les scores une fois le pronostic chargé
    LaunchedEffect(pronostic) {
        pronostic?.let {
            scoreA = it.predictedScoreA.toString()
            scoreB = it.predictedScoreB.toString()
        }
    }

    // Gestion des messages
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            pronosticViewModel.clearMessages()
            onNavigateBack()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            pronosticViewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modifier le pronostic") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (pronostic == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text("Pronostic introuvable")
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Carte du match
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Match",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = pronostic.matchTeamA,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "VS",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = pronostic.matchTeamB,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Text(
                    text = "Modifier votre pronostic",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Saisie des scores
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = scoreA,
                        onValueChange = { if (it.length <= 2) scoreA = it.filter { c -> c.isDigit() } },
                        label = { Text(pronostic.matchTeamA) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "—",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    OutlinedTextField(
                        value = scoreB,
                        onValueChange = { if (it.length <= 2) scoreB = it.filter { c -> c.isDigit() } },
                        label = { Text(pronostic.matchTeamB) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Prédiction du gagnant
                if (scoreA.isNotBlank() && scoreB.isNotBlank()) {
                    val intScoreA = scoreA.toIntOrNull() ?: 0
                    val intScoreB = scoreB.toIntOrNull() ?: 0

                    val prediction = when {
                        intScoreA > intScoreB -> "Victoire de ${pronostic.matchTeamA}"
                        intScoreB > intScoreA -> "Victoire de ${pronostic.matchTeamB}"
                        else -> "Match nul"
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Text(
                            text = "🔮 Nouvelle prédiction : $prediction",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bouton de validation
                Button(
                    onClick = {
                        scope.launch {
                            val intScoreA = scoreA.toIntOrNull()
                            val intScoreB = scoreB.toIntOrNull()

                            if (intScoreA != null && intScoreB != null) {
                                pronosticViewModel.updatePronostic(
                                    pronostic = pronostic,
                                    newScoreA = intScoreA,
                                    newScoreB = intScoreB
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = scoreA.isNotBlank() && scoreB.isNotBlank() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Enregistrer les modifications")
                    }
                }
            }
        }
    }
}
