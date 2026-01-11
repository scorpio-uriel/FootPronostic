package com.example.footpronostic.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.footpronostic.data.model.SportMatch
import com.example.footpronostic.ui.viewmodel.MatchViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Écran principal qui affiche la liste des matchs.
 */
@Composable
fun MatchListScreen(matchViewModel: MatchViewModel = viewModel()) {
    // Observe l'état de la liste des matchs depuis le ViewModel.
    val matches by matchViewModel.matches.collectAsState()
    // Observe l'état isAdmin depuis le ViewModel.
    val isAdmin by matchViewModel.isAdmin.collectAsState()
    
    // État pour le dialogue d'ajout
    var showAddMatchDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(onClick = { showAddMatchDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Ajouter un match")
                }
            }
        }
    ) { padding ->
        if (matches.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Aucun match disponible.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 8.dp)
            ) {
                items(matches) { match ->
                    MatchCard(match = match, viewModel = matchViewModel, isAdmin = isAdmin)
                }
            }
        }

        if (showAddMatchDialog) {
            AddMatchDialog(
                onDismiss = { showAddMatchDialog = false },
                onMatchAdd = { newMatch ->
                    matchViewModel.addMatch(newMatch)
                    showAddMatchDialog = false
                }
            )
        }
    }
}

@Composable
fun MatchCard(match: SportMatch, viewModel: MatchViewModel, isAdmin: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${match.teamA} vs ${match.teamB}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatDateTime(match.dateTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Score: ${match.scoreA} - ${match.scoreB}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = match.status.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (match.status == "finished") Color.Red else Color.Blue
                )
            }
            if (isAdmin) {
                IconButton(onClick = { viewModel.deleteMatch(match.id) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun AddMatchDialog(onDismiss: () -> Unit, onMatchAdd: (SportMatch) -> Unit) {
    var teamA by remember { mutableStateOf("") }
    var teamB by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouveau Match") },
        text = {
            Column {
                TextField(value = teamA, onValueChange = { teamA = it }, label = { Text("Équipe A") })
                Spacer(modifier = Modifier.height(8.dp))
                TextField(value = teamB, onValueChange = { teamB = it }, label = { Text("Équipe B") })
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (teamA.isNotBlank() && teamB.isNotBlank()) onMatchAdd(SportMatch(teamA = teamA, teamB = teamB, dateTime = System.currentTimeMillis())) },
                enabled = teamA.isNotBlank() && teamB.isNotBlank()
            ) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
