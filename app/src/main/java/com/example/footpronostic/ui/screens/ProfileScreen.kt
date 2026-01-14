package com.example.footpronostic.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.footpronostic.data.model.AvatarConfig
import com.example.footpronostic.ui.avatar.CoilConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser ?: return
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    val imageLoader = remember { CoilConfig.getImageLoader(context) }

    var avatar by remember { mutableStateOf(AvatarConfig()) }
    var role by remember { mutableStateOf("USER") }
    var isSaving by remember { mutableStateOf(false) }

    // Styles DiceBear disponibles
    val styles = listOf("avataaars", "bottts", "personas", "pixel-art", "lorelei", "micah")

    // Charger les données Firestore
    LaunchedEffect(user.uid) {
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    role = doc.getString("role") ?: "USER"
                    val avatarMap = doc.get("avatar") as? Map<*, *>
                    if (avatarMap != null) {
                        avatar = AvatarConfig(
                            style = avatarMap["style"] as? String ?: "avataaars",
                            seed = avatarMap["seed"] as? String ?: user.email ?: "default"
                        )
                    } else {
                        // Fallback : on utilise l'email comme seed par défaut
                        avatar = avatar.copy(seed = user.email ?: "default")
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mon Profil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Box(contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = AvatarUtils.getAvatarUrl(avatar),
                    imageLoader = imageLoader,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    error = coil.compose.rememberAsyncImagePainter(
                        model = "https://ui-avatars.com/api/?name=${user.email}&background=random&size=256",
                        imageLoader = imageLoader
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { avatar = avatar.copy(seed = UUID.randomUUID().toString()) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.Casino, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Générer aléatoirement")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                user.email ?: "Email",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            AssistChip(onClick = {}, label = { Text("Rôle : $role") })

            Spacer(modifier = Modifier.height(32.dp))

            Text("Personnaliser le style", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                maxItemsInEachRow = 3
            ) {
                styles.forEach { style ->
                    FilterChip(
                        modifier = Modifier.padding(4.dp),
                        selected = avatar.style == style,
                        onClick = { avatar = avatar.copy(style = style) },
                        label = { Text(style.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                onClick = {
                    isSaving = true
                    db.collection("users").document(user.uid)
                        .set(mapOf("avatar" to avatar), SetOptions.merge())
                        .addOnSuccessListener {
                            isSaving = false
                            Toast.makeText(context, "Profil mis à jour", Toast.LENGTH_SHORT).show()
                        }
                }
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Sauvegarder les modifications")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    onLogout()
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Se déconnecter")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    maxItemsInEachRow: Int = Int.MAX_VALUE,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        maxItemsInEachRow = maxItemsInEachRow
    ) {
        content()
    }
}
