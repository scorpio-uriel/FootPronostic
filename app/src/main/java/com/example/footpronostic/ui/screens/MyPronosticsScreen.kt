package com.example.footpronostic.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.footpronostic.data.model.Pronostic
import com.example.footpronostic.ui.viewmodel.PronosticViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Écran affichant tous les pronostics de l'utilisateur.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPronosticsScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onEditPronostic: (String) -> Unit,
    pronosticViewModel: PronosticViewModel = viewModel()
) {
    val pronostics by pronosticViewModel.pronostics.collectAsState()
    val isLoading by pronosticViewModel.isLoading.collectAsState()

    LaunchedEffect(userId) {
        pronosticViewModel.loadUserPronostics(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes pronostics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                pronostics.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📊 Aucun pronostic",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Vous n'avez pas encore de pronostics.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(pronostics) { pronostic ->
                            PronosticCard(
                                pronostic = pronostic,
                                onEdit = { onEditPronostic(pronostic.id) },
                                onDelete = { pronosticViewModel.deletePronostic(pronostic.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PronosticCard(
    pronostic: Pronostic,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val canEdit = System.currentTimeMillis() < pronostic.matchDateTime

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (pronostic.isValidated)
                MaterialTheme.colorScheme.tertiaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // En-tête
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDateTime(pronostic.matchDateTime),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )

                if (pronostic.isValidated) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "✓ ${pronostic.points} pts",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Match
            Text(
                text = "${pronostic.matchTeamA} vs ${pronostic.matchTeamB}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Pronostic
            Text(
                text = "Votre pronostic : ${pronostic.predictedScoreA} - ${pronostic.predictedScoreB}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            // Actions
            if (canEdit && !pronostic.isValidated) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Modifier",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Modifier")
                    }

                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Supprimer")
                    }
                }
            } else if (!canEdit) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ Match commencé - modification impossible",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEEE dd MMM · HH:mm", Locale("fr", "FR"))
    return sdf.format(Date(timestamp))
}
