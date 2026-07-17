package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BlacklistDomain
import com.example.viewmodel.GuardianViewModel

@Composable
fun WebProtectionScreen(viewModel: GuardianViewModel) {
    val safeSearch by viewModel.safeSearchEnabled.collectAsState()
    val phishingBlocked by viewModel.phishingBlockedEnabled.collectAsState()
    val malwareBlocked by viewModel.malwareBlockedEnabled.collectAsState()
    val dnsVpnFilter by viewModel.dnsVpnFilterEnabled.collectAsState()
    val webDomains by viewModel.webDomains.collectAsState()

    var testUrlInput by remember { mutableStateOf("") }
    var scanResultText by remember { mutableStateOf("") }
    var scanResultSeverity by remember { mutableStateOf("INFO") } // INFO, WARNING, CRITICAL

    var addDomainName by remember { mutableStateOf("") }
    var addDomainCategory by remember { mutableStateOf("Malware") } // Malware, Phishing, Adult, Custom_Block, Custom_Allow
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Web Filter Shield",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Active URL Web Interceptor", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "Resolves domain queries locally, bypassing phishing caches and adult index lists.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Toggles Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FILTER GATEWAYS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Phishing Block
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Phishing Blockade", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Locks out fake login forms and banking clones", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = phishingBlocked,
                            onCheckedChange = { viewModel.phishingBlockedEnabled.value = it },
                            modifier = Modifier.testTag("phishing_toggle")
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 8.dp))

                    // Malware Block
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Malware & Botnet Shield", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Blocks script payloads, drive-by downloads, and tracking ads", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = malwareBlocked,
                            onCheckedChange = { viewModel.malwareBlockedEnabled.value = it },
                            modifier = Modifier.testTag("malware_toggle")
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 8.dp))

                    // Safe Search Enforcement
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enforced Safe Search", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Blocks explicit adult queries and images on search indexes", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = safeSearch,
                            onCheckedChange = { viewModel.safeSearchEnabled.value = it },
                            modifier = Modifier.testTag("safesearch_toggle")
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 8.dp))

                    // Local VPN Loopback
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Local VPN DNS Sinkhole", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Simulates local VPN overlay to sink malicious DNS lookups", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = dnsVpnFilter,
                            onCheckedChange = { viewModel.dnsVpnFilterEnabled.value = it },
                            modifier = Modifier.testTag("dns_vpn_toggle")
                        )
                    }
                }
            }
        }

        // URL AI Scanner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AI LINK THREAT CHECKER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Guardian AI will inspect entered URLs against custom security heuristics instantly.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = testUrlInput,
                        onValueChange = { testUrlInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("url_checker_input"),
                        placeholder = { Text("Enter URL (e.g., banking-secure-login.com)") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (testUrlInput.isNotBlank()) {
                                val urlLower = testUrlInput.lowercase()
                                val matchesBlacklist = webDomains.any { urlLower.contains(it.domain.lowercase()) && !it.isAllowed }
                                val matchesWhitelist = webDomains.any { urlLower.contains(it.domain.lowercase()) && it.isAllowed }

                                when {
                                    matchesWhitelist -> {
                                        scanResultText = "Guardian Verdict: SAFE (WHITELISTED). This domain is part of your trusted communication lists."
                                        scanResultSeverity = "INFO"
                                    }
                                    matchesBlacklist -> {
                                        scanResultText = "Guardian Verdict: BLOCKED (BLACKLISTED). Dangerous malware/phishing origin identified. Connection rejected."
                                        scanResultSeverity = "CRITICAL"
                                    }
                                    urlLower.contains("gift") || urlLower.contains("login") || urlLower.contains("verify") || urlLower.contains("free") -> {
                                        scanResultText = "Guardian Verdict: SUSPICIOUS. AI heuristics identified suspicious redirection flags. Opening this link could risk credentials leakage."
                                        scanResultSeverity = "WARNING"
                                    }
                                    else -> {
                                        scanResultText = "Guardian Verdict: SECURE. No active phishing markers or hostile records found on the remote host."
                                        scanResultSeverity = "INFO"
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("url_checker_button")
                    ) {
                        Icon(imageVector = Icons.Default.YoutubeSearchedFor, contentDescription = "Scan")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("EVALUATE LINK SECURITY")
                    }

                    if (scanResultText.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = when (scanResultSeverity) {
                                        "CRITICAL" -> MaterialTheme.colorScheme.errorContainer
                                        "WARNING" -> Color(0xFFFFF7E6)
                                        else -> MaterialTheme.colorScheme.primaryContainer
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = when (scanResultSeverity) {
                                        "CRITICAL" -> MaterialTheme.colorScheme.error
                                        "WARNING" -> Color(0xFFFFAA00)
                                        else -> MaterialTheme.colorScheme.primary
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (scanResultSeverity) {
                                        "CRITICAL" -> Icons.Default.GppBad
                                        "WARNING" -> Icons.Default.ReportProblem
                                        else -> Icons.Default.GppGood
                                    },
                                    contentDescription = "Alert icon",
                                    tint = when (scanResultSeverity) {
                                        "CRITICAL" -> MaterialTheme.colorScheme.error
                                        "WARNING" -> Color(0xFFD48800)
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = scanResultText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (scanResultSeverity) {
                                        "CRITICAL" -> MaterialTheme.colorScheme.onErrorContainer
                                        "WARNING" -> Color(0xFF5C3C00)
                                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Domain Rule Lists Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CUSTOM SINKHOLE FILTERS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.2.sp
                )

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_web_rule_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Domain Rule")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Rule", fontSize = 11.sp)
                }
            }
        }

        // Rules List
        if (webDomains.isEmpty()) {
            item {
                Text(
                    text = "No custom domains listed in filter files.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                )
            }
        } else {
            items(webDomains) { rule ->
                DomainRuleCard(rule) { viewModel.deleteWebDomain(rule.id) }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Custom Web Domain Rule") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = addDomainName,
                        onValueChange = { addDomainName = it },
                        label = { Text("Domain (e.g., suspicious-site.net)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Rules Category
                    Text("Select Filter Type", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Malware", "Phishing", "Adult", "Custom_Allow").forEach { cat ->
                            InputChip(
                                selected = addDomainCategory == cat,
                                onClick = { addDomainCategory = cat },
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
                        if (addDomainName.isNotBlank()) {
                            val isAllowed = addDomainCategory == "Custom_Allow"
                            viewModel.addWebDomain(addDomainName, addDomainCategory, isAllowed)
                            addDomainName = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("COMMIT SINKHOLE")
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
fun DomainRuleCard(rule: BlacklistDomain, onDelete: () -> Unit) {
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
            Icon(
                imageVector = if (rule.isAllowed) Icons.Default.CheckCircle else Icons.Default.Block,
                contentDescription = "Status icon",
                tint = if (rule.isAllowed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.domain,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Type: ${rule.category} • Action: ${if (rule.isAllowed) "SINKHOLE PASS" else "DROP PACKET"}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Wipe web rule", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
