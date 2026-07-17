package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Password
import com.example.data.VaultMedia
import com.example.viewmodel.GuardianViewModel

@Composable
fun VaultScreen(viewModel: GuardianViewModel) {
    val isUnlocked by viewModel.isVaultUnlocked.collectAsState()
    val wrongAttempts by viewModel.wrongAttempts.collectAsState()
    val showBruteForceAlert by viewModel.showWrongAttemptAlert.collectAsState()

    var activeVaultSubTab by remember { mutableStateOf("MEDIA") } // MEDIA or PASSWORDS

    if (!isUnlocked) {
        VaultAuthGate(
            wrongAttempts = wrongAttempts,
            showBruteForceAlert = showBruteForceAlert,
            onUnlockAttempt = { pin -> viewModel.unlockVault(pin) }
        )
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            // Unlocked Vault Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = { activeVaultSubTab = "MEDIA" },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("vault_media_tab"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeVaultSubTab == "MEDIA") MaterialTheme.colorScheme.primary
                        else Color.Transparent,
                        contentColor = if (activeVaultSubTab == "MEDIA") MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = "Media Vault")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Secure Media", fontSize = 12.sp)
                }

                Button(
                    onClick = { activeVaultSubTab = "PASSWORDS" },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("vault_passwords_tab"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeVaultSubTab == "PASSWORDS") MaterialTheme.colorScheme.primary
                        else Color.Transparent,
                        contentColor = if (activeVaultSubTab == "PASSWORDS") MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(imageVector = Icons.Default.VpnKey, contentDescription = "Password Manager")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Passwords", fontSize = 12.sp)
                }

                // Quick Lock Button
                IconButton(
                    onClick = { viewModel.lockVault() },
                    modifier = Modifier.testTag("vault_relock_button")
                ) {
                    Icon(imageVector = Icons.Default.LockOpen, contentDescription = "Relock Vault", tint = MaterialTheme.colorScheme.error)
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (activeVaultSubTab == "MEDIA") {
                    MediaVaultPanel(viewModel)
                } else {
                    PasswordManagerPanel(viewModel)
                }
            }
        }
    }
}

@Composable
fun VaultAuthGate(
    wrongAttempts: Int,
    showBruteForceAlert: Boolean,
    onUnlockAttempt: (String) -> Boolean
) {
    var pinValue by remember { mutableStateOf("") }
    var loginFeedback by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Vault Locked",
                    tint = if (showBruteForceAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "BIOMETRIC VAULT ENCLAVE",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Enter secure PIN or verify fingerprint to unlock.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                if (showBruteForceAlert) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "BRUTE FORCE MITIGATION: 3+ wrong password attempts registered. Threat logged. Input throttled.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (wrongAttempts > 0) {
                    Text(
                        text = "Failed attempts: $wrongAttempts / 3",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Passcode Entry Field
                OutlinedTextField(
                    value = pinValue,
                    onValueChange = { if (it.length <= 4) pinValue = it },
                    label = { Text("4-Digit Secure PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vault_pin_input"),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                // Simulate Fingerprint Verification
                                val success = onUnlockAttempt("1234")
                                if (success) {
                                    loginFeedback = "Biometric matched!"
                                } else {
                                    loginFeedback = "Biometric mismatch."
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Fingerprint, contentDescription = "Simulate Fingerprint Scan", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Unlock Button
                Button(
                    onClick = {
                        val success = onUnlockAttempt(pinValue)
                        if (!success) {
                            loginFeedback = "Incorrect secure passcode."
                        }
                        pinValue = ""
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("vault_unlock_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("AUTHENTICATE")
                }

                if (loginFeedback.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = loginFeedback,
                        fontSize = 12.sp,
                        color = if (loginFeedback.contains("matched")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Demo PIN: 1234 or touch Fingerprint sensor.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun MediaVaultPanel(viewModel: GuardianViewModel) {
    val mediaFiles by viewModel.vaultMediaList.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    var mockFileName by remember { mutableStateOf("") }
    var mockFileType by remember { mutableStateOf("IMAGE") }
    var mockFileSize by remember { mutableStateOf("2.4 MB") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ENCRYPTED MEDIA REGISTRY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.2.sp
            )

            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("lock_file_button")
            ) {
                Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = "Lock File")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Lock Media", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (mediaFiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.FolderZip, contentDescription = "Empty", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No private pictures or videos encrypted yet.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mediaFiles) { media ->
                    MediaVaultRow(media) { viewModel.deleteVaultMedia(media) }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Encrypt File Into Secure Vault") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = mockFileName,
                        onValueChange = { mockFileName = it },
                        label = { Text("File Name (e.g., private_family.jpg)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { mockFileType = "IMAGE" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (mockFileType == "IMAGE") MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text("Image", color = if (mockFileType == "IMAGE") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Button(
                            onClick = { mockFileType = "VIDEO" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (mockFileType == "VIDEO") MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text("Video", color = if (mockFileType == "VIDEO") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    OutlinedTextField(
                        value = mockFileSize,
                        onValueChange = { mockFileSize = it },
                        label = { Text("Simulated Size") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (mockFileName.isNotBlank()) {
                            viewModel.addVaultMedia(
                                name = mockFileName,
                                type = mockFileType,
                                path = "/sdcard/encrypted/${mockFileName}.aes",
                                size = mockFileSize
                            )
                            mockFileName = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("ENCRYPT & SECURE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
fun MediaVaultRow(media: VaultMedia, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (media.fileType == "IMAGE") Icons.Default.Image else Icons.Default.Videocam,
                    contentDescription = "Encrypted file type",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = media.fileName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "AES-256 Block Locked", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    Text(text = "•", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    Text(text = media.fileSize, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Wipe media", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun PasswordManagerPanel(viewModel: GuardianViewModel) {
    val passwords by viewModel.passwordsList.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    var serviceName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var plainPassword by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Social") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BIOMETRIC VAULT PASSWORDS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.2.sp
            )

            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("add_password_button")
            ) {
                Icon(imageVector = Icons.Default.VpnKey, contentDescription = "Add Password")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Entry", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (passwords.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.EnhancedEncryption, contentDescription = "Empty Key", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No credentials securely recorded yet.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(passwords) { pwd ->
                    PasswordEntryRow(pwd) { viewModel.deletePassword(pwd) }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Save Biometric Password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = serviceName,
                        onValueChange = { serviceName = it },
                        label = { Text("Service Name (e.g., Google, Amazon)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username / Email") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = plainPassword,
                        onValueChange = { plainPassword = it },
                        label = { Text("Secret Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Categories Selector
                    Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Social", "Finance", "Work", "Personal").forEach { cat ->
                            InputChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 10.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (serviceName.isNotBlank() && username.isNotBlank() && plainPassword.isNotBlank()) {
                            viewModel.addPassword(serviceName, username, plainPassword, selectedCategory)
                            serviceName = ""
                            username = ""
                            plainPassword = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("SAVE & LOCK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
fun PasswordEntryRow(pwd: Password, onDelete: () -> Unit) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (pwd.category) {
                        "Finance" -> Icons.Default.AccountBalance
                        "Work" -> Icons.Default.Work
                        "Social" -> Icons.Default.AlternateEmail
                        else -> Icons.Default.Lock
                    },
                    contentDescription = pwd.category,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pwd.serviceName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = pwd.username,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isPasswordVisible) pwd.encryptedValue.replace("AES-Encrypted[", "").replace("]", "")
                    else "••••••••••••",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPasswordVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }

            // Visible Toggle
            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                Icon(
                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = "Reveal passcode",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Delete Button
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Wipe passcode", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
