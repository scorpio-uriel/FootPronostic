package com.example.footpronostic.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.footpronostic.auth.AuthViewModel
import com.example.footpronostic.data.model.SportMatch
import com.example.footpronostic.ui.viewmodel.MatchViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Écran principal affichant la liste des matchs et les options d'administration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel = viewModel(),
    matchViewModel: MatchViewModel = viewModel(),
    onLogout: () -> Unit
) {
    val matches by matchViewModel.matches.collectAsState()
    val userRole by authViewModel.userRole.collectAsState()
    //val isAdmin = userRole == "admin"
    val isAdmin = true

    LaunchedEffect(userRole) {
        matchViewModel.setAdminStatus(isAdmin)
    }

    // État pour gérer le dialogue d'ajout/modification
    var showMatchDialog by remember { mutableStateOf(false) }
    var matchToEdit by remember { mutableStateOf<SportMatch?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FootPronostic ⚽") },
                actions = {
                    IconButton(onClick = {
                        authViewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Déconnexion")
                    }
                }
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(onClick = { 
                    matchToEdit = null
                    showMatchDialog = true 
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Ajouter un match")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (matches.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aucun match disponible pour le moment.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(matches) { match ->
                        MatchItemCard(
                            match = match,
                            isAdmin = isAdmin,
                            onDelete = { matchViewModel.deleteMatch(match.id) },
                            onEdit = {
                                matchToEdit = match
                                showMatchDialog = true
                            }
                        )
                    }
                }
            }
        }

        if (showMatchDialog) {
            MatchDialog(
                match = matchToEdit,
                onDismiss = { showMatchDialog = false },
                onConfirm = { teamA, teamB, scoreA, scoreB, status ->
                    if (matchToEdit == null) {
                        // Mode Ajout
                        matchViewModel.addMatch(SportMatch(
                            teamA = teamA, 
                            teamB = teamB, 
                            dateTime = System.currentTimeMillis()
                        ))
                    } else {
                        // Mode Modification
                        val updatedMatch = matchToEdit!!.copy(
                            teamA = teamA,
                            teamB = teamB,
                            scoreA = scoreA,
                            scoreB = scoreB,
                            status = status
                        )
                        matchViewModel.updateMatch(updatedMatch)
                    }
                    showMatchDialog = false
                }
            )
        }
    }
}

@Composable
fun MatchItemCard(
    match: SportMatch,
    isAdmin: Boolean,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${match.teamA} vs ${match.teamB}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatDate(match.dateTime),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Statut: ${match.status.uppercase()}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (match.status == "finished") Color.Red else Color.DarkGray
                )
                if (match.status == "finished" || (match.scoreA > 0 || match.scoreB > 0)) {
                    Text(
                        text = "Score: ${match.scoreA} - ${match.scoreB}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isAdmin) {
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun MatchDialog(
    match: SportMatch?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, Int, String) -> Unit
) {
    var teamA by remember { mutableStateOf(match?.teamA ?: "") }
    var teamB by remember { mutableStateOf(match?.teamB ?: "") }
    var scoreA by remember { mutableStateOf(match?.scoreA?.toString() ?: "0") }
    var scoreB by remember { mutableStateOf(match?.scoreB?.toString() ?: "0") }
    var status by remember { mutableStateOf(match?.status ?: "upcoming") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (match == null) "Ajouter un Match" else "Modifier le Match") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = teamA, onValueChange = { teamA = it }, label = { Text("Équipe A") })
                OutlinedTextField(value = teamB, onValueChange = { teamB = it }, label = { Text("Équipe B") })
                
                if (match != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = scoreA, 
                            onValueChange = { scoreA = it }, 
                            label = { Text("Score A") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = scoreB, 
                            onValueChange = { scoreB = it }, 
                            label = { Text("Score B") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Sélecteur de statut simple
                    Text("Statut du match :", style = MaterialTheme.typography.labelMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = status == "upcoming", onClick = { status = "upcoming" })
                        Text("À venir")
                        Spacer(modifier = Modifier.width(8.dp))
                        RadioButton(selected = status == "finished", onClick = { status = "finished" })
                        Text("Terminé")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onConfirm(teamA, teamB, scoreA.toIntOrNull() ?: 0, scoreB.toIntOrNull() ?: 0, status) 
                }, 
                enabled = teamA.isNotBlank() && teamB.isNotBlank()
            ) {
                Text(if (match == null) "Créer" else "Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
