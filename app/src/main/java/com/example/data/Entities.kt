package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "passwords")
data class Password(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val serviceName: String,
    val username: String,
    val encryptedValue: String, // Base64 simulated encrypted string
    val category: String, // Social, Financial, Work, Personal
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "web_domains")
data class BlacklistDomain(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val domain: String,
    val category: String, // Adult, Phishing, Malware, Custom_Block, Custom_Allow
    val isAllowed: Boolean = false, // If true, acts as Whitelist; else, Blacklist
    val addedTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "vault_media")
data class VaultMedia(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fileName: String,
    val fileType: String, // IMAGE, VIDEO
    val simulatedPath: String,
    val fileSize: String,
    val encryptedKey: String, // Simulated AES-256 wrapping key identifier
    val addedTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "automation_routines")
data class AutomationRoutine(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val triggerPhrase: String,
    val actionType: String, // SILENT_MODE, TOGGLE_WIFI, TOGGLE_BLUETOOTH, FLASH_LIGHT, VOLUME_MAX, OPEN_CAMERA
    val parameter: String = "",
    val isActive: Boolean = true,
    val creationTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "threat_logs")
data class ThreatLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val threatType: String, // OVERLAY_ATTACK, DANGEROUS_PERMISSION, SUSPICIOUS_INSTALL, WEBSHIELD_BLOCK
    val description: String,
    val riskLevel: String // CRITICAL, WARNING, INFO
)
