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
    // Variable d'état pour contrôler l'affichage du dialogue d'ajout.
    var showAddMatchDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            // Affiche le bouton d'action flottant uniquement si l'utilisateur est un admin.
            if (matchViewModel.isAdmin) {
                FloatingActionButton(onClick = { showAddMatchDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Ajouter un match")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp)
        ) {
            // Itère sur la liste des matchs et affiche une MatchCard pour chacun.
            items(matches) { match ->
                MatchCard(match = match, viewModel = matchViewModel)
            }
        }

        // Affiche le dialogue si showAddMatchDialog est vrai.
        if (showAddMatchDialog) {
            AddMatchDialog(
                onDismiss = { showAddMatchDialog = false },
                onMatchAdd = {
                    matchViewModel.addMatch(it)
                    showAddMatchDialog = false
                }
            )
        }
    }
}

/**
 * Affiche une carte (Card) pour un seul match.
 */
@Composable
fun MatchCard(match: SportMatch, viewModel: MatchViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "${match.teamA} vs ${match.teamB}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = formatDateTime(match.dateTime), fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Score: ${match.scoreA} - ${match.scoreB}", fontSize = 16.sp)
                Text(text = "Statut: ${match.status}", fontSize = 14.sp, color = if (match.status == "finished") Color.Red else Color.Green)
            }
            // Si l'utilisateur est admin, affiche le bouton de suppression.
            if (viewModel.isAdmin) {
                IconButton(onClick = { viewModel.deleteMatch(match.id) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

/**
 * Dialogue pour ajouter un nouveau match.
 */
@Composable
fun AddMatchDialog(
    onDismiss: () -> Unit,
    onMatchAdd: (SportMatch) -> Unit
) {
    var teamA by remember { mutableStateOf("") }
    var teamB by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Ajouter un match") },
        text = {
            Column {
                OutlinedTextField(value = teamA, onValueChange = { teamA = it }, label = { Text("Équipe A") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = teamB, onValueChange = { teamB = it }, label = { Text("Équipe B") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val newMatch = SportMatch(
                    teamA = teamA,
                    teamB = teamB,
                    dateTime = System.currentTimeMillis(), // Utilise l'heure actuelle
                    status = "upcoming"
                )
                onMatchAdd(newMatch)
            }) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

/**
 * Formate un timestamp en une chaîne de caractères lisible.
 */
private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
