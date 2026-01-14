package com.example.footpronostic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.footpronostic.data.model.AvatarConfig
import com.example.footpronostic.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * Écran du classement des utilisateurs utilisant DiceBear.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onNavigateBack: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    var users by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("users")
            .orderBy("points", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, _ ->
                users = snapshot?.documents?.mapNotNull { doc ->
                    val avatarMap = doc.get("avatar") as? Map<*, *>
                    UserProfile(
                        uid = doc.id,
                        email = doc.getString("email") ?: "Anonyme",
                        points = doc.getLong("points")?.toInt() ?: 0,
                        avatar = AvatarConfig(
                            style = avatarMap?.get("style") as? String ?: "avataaars",
                            seed = avatarMap?.get("seed") as? String ?: "default"
                        )
                    )
                } ?: emptyList()
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Classement Mondial 🏆") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(users) { index, user ->
                    LeaderboardItem(rank = index + 1, user = user)
                }
            }
        }
    }
}

@Composable
fun LeaderboardItem(rank: Int, user: UserProfile) {
    val backgroundColor = when (rank) {
        1 -> Color(0xFFFFD700).copy(alpha = 0.1f)
        2 -> Color(0xFFC0C0C0).copy(alpha = 0.1f)
        3 -> Color(0xFFCD7F32).copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (rank <= 3) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rang
            Box(modifier = Modifier.width(40.dp), contentAlignment = Alignment.Center) {
                if (rank <= 3) {
                    Icon(
                        Icons.Default.EmojiEvents, 
                        contentDescription = null,
                        tint = when(rank) {
                            1 -> Color(0xFFFFD700)
                            2 -> Color(0xFFC0C0C0)
                            else -> Color(0xFFCD7F32)
                        }
                    )
                } else {
                    Text("#$rank", fontWeight = FontWeight.Bold, color = Color.Gray)
                }
            }

            // Avatar DiceBear
            Box(modifier = Modifier.size(45.dp)) {
                AsyncImage(
                    model = AvatarUtils.getAvatarUrl(user.avatar),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.width(16.dp))

            // Infos
            Column(Modifier.weight(1f)) {
                Text(
                    text = user.email.substringBefore("@"),
                    fontWeight = if (rank <= 3) FontWeight.Bold else FontWeight.Medium,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(user.email, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }

            // Points
            Text(
                text = "${user.points} pts",
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
