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
import com.example.footpronostic.data.model.SportMatch
import com.example.footpronostic.ui.viewmodel.MatchViewModel
import com.example.footpronostic.ui.viewmodel.PronosticViewModel
import kotlinx.coroutines.launch

/**
 * Écran pour créer un pronostic sur un match.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePronosticScreen(
    matchId: String,
    userId: String,
    onNavigateBack: () -> Unit,
    matchViewModel: MatchViewModel = viewModel(),
    pronosticViewModel: PronosticViewModel = viewModel()
) {
    val matches by matchViewModel.matches.collectAsState()
    val match = matches.find { it.id == matchId }

    val isLoading by pronosticViewModel.isLoading.collectAsState()
    val errorMessage by pronosticViewModel.errorMessage.collectAsState()
    val successMessage by pronosticViewModel.successMessage.collectAsState()

    var scoreA by remember { mutableStateOf("") }
    var scoreB by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Gestion des messages : retour direct après succès
    LaunchedEffect(successMessage) {
        successMessage?.let {
            // On retourne à l'écran précédent immédiatement sans attendre la fin du snackbar
            onNavigateBack()
            pronosticViewModel.clearMessages()
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
                title = { Text("Nouveau pronostic") },
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
        if (match == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Match introuvable")
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
                                text = match.teamA,
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
                                text = match.teamB,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Text(
                    text = "Votre pronostic",
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
                        label = { Text(match.teamA) },
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
                        label = { Text(match.teamB) },
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
                        intScoreA > intScoreB -> "Victoire de ${match.teamA}"
                        intScoreB > intScoreA -> "Victoire de ${match.teamB}"
                        else -> "Match nul"
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Text(
                            text = "🔮 Prédiction : $prediction",
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
                                val canBet = pronosticViewModel.canBetOnMatch(userId, matchId)
                                if (canBet) {
                                    pronosticViewModel.createPronostic(
                                        match = match,
                                        userId = userId,
                                        scoreA = intScoreA,
                                        scoreB = intScoreB
                                    )
                                } else {
                                    snackbarHostState.showSnackbar("Vous avez déjà parié sur ce match")
                                }
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
                        Text("Valider mon pronostic")
                    }
                }
            }
        }
    }
}
