package com.example.footpronostic.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.footpronostic.R
import com.example.footpronostic.data.model.AvatarConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser ?: return
    val db = FirebaseFirestore.getInstance()

    var avatar by remember { mutableStateOf(AvatarConfig()) }
    var role by remember { mutableStateOf("USER") }
    var isSaving by remember { mutableStateOf(false) }

    // Charger les données de l'utilisateur depuis Firestore
    LaunchedEffect(user.uid) {
        db.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    role = doc.getString("role") ?: "USER"
                    
                    // Récupération sécurisée de la configuration d'avatar
                    val map = doc.get("avatar") as? Map<*, *>
                    if (map != null) {
                        avatar = AvatarConfig(
                            skin = map["skin"] as? String ?: "light",
                            hair = map["hair"] as? String ?: "short",
                            eyes = map["eyes"] as? String ?: "default",
                            mouth = map["mouth"] as? String ?: "smile",
                            outfit = map["outfit"] as? String ?: "hoodie"
                        )
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
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

            // Visualisation de l'avatar
            AvatarPreview(avatar)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = user.email ?: "Utilisateur",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            AssistChip(
                onClick = { },
                label = { Text("Rôle : $role") },
                colors = AssistChipDefaults.assistChipColors(
                    labelColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Personnaliser mon avatar",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Options de personnalisation
            AvatarSection("Couleur de peau") {
                AvatarOptionRow(listOf("light", "brown", "dark"), avatar.skin) { 
                    avatar = avatar.copy(skin = it) 
                }
            }

            AvatarSection("Cheveux") {
                AvatarOptionRow(listOf("short", "long"), avatar.hair) { 
                    avatar = avatar.copy(hair = it) 
                }
            }

            AvatarSection("Yeux") {
                AvatarOptionRow(listOf("default", "happy"), avatar.eyes) { 
                    avatar = avatar.copy(eyes = it) 
                }
            }

            AvatarSection("Bouche") {
                AvatarOptionRow(listOf("smile", "sad"), avatar.mouth) { 
                    avatar = avatar.copy(mouth = it) 
                }
            }

            AvatarSection("Vêtements") {
                AvatarOptionRow(listOf("hoodie", "shirt"), avatar.outfit) { 
                    avatar = avatar.copy(outfit = it) 
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Boutons d'action
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                onClick = {
                    isSaving = true
                    db.collection("users")
                        .document(user.uid)
                        .set(mapOf("avatar" to avatar), SetOptions.merge())
                        .addOnCompleteListener { isSaving = false }
                }
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Sauvegarder les modifications")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    onLogout()
                }
            ) {
                Text("Se déconnecter")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun AvatarPreview(avatar: AvatarConfig) {
    Box(
        modifier = Modifier
            .size(160.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        // Superposition des calques de l'avatar
        Image(painterResource(AvatarUtils.getSkinRes(avatar.skin)), null, modifier = Modifier.fillMaxSize())
        Image(painterResource(AvatarUtils.getHairRes(avatar.hair)), null, modifier = Modifier.fillMaxSize())
        Image(painterResource(AvatarUtils.getEyesRes(avatar.eyes)), null, modifier = Modifier.fillMaxSize())
        Image(painterResource(AvatarUtils.getMouthRes(avatar.mouth)), null, modifier = Modifier.fillMaxSize())
        Image(painterResource(AvatarUtils.getOutfitRes(avatar.outfit)), null, modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun AvatarSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        content()
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarOptionRow(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(option.replaceFirstChar { it.uppercase() }) }
            )
        }
    }
}
