package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.utils.AisClient
import com.example.utils.TtsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatMessage(val role: String, val text: String, val timestamp: Long = System.currentTimeMillis())

class GuardianViewModel(application: Application) : AndroidViewModel(application) {
    private val dao: GuardianDao = AppDatabase.getDatabase(application).guardianDao()
    private val ttsManager = TtsManager(application)

    // Dashboard State
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _riskScore = MutableStateFlow(12) // Low risk initially
    val riskScore: StateFlow<Int> = _riskScore.asStateFlow()

    // Interactive permission monitors
    val micMonitoringEnabled = MutableStateFlow(true)
    val camMonitoringEnabled = MutableStateFlow(true)
    val locationMonitoringEnabled = MutableStateFlow(false)
    val screenOverlayAlertsEnabled = MutableStateFlow(true)

    // Flows from database
    val threatLogs: StateFlow<List<ThreatLog>> = dao.getRecentThreatLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val passwordsList: StateFlow<List<Password>> = dao.getAllPasswords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val webDomains: StateFlow<List<BlacklistDomain>> = dao.getAllDomains()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaultMediaList: StateFlow<List<VaultMedia>> = dao.getAllVaultMedia()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val routinesList: StateFlow<List<AutomationRoutine>> = dao.getAllRoutines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Assistant State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("assistant", "Greetings, Aryan. I am your active Guardian AI defense line. My local security gates are fully powered. How can I protect you today?")
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    val isListening = MutableStateFlow(false)
    val wakeWordEnabled = MutableStateFlow(true)
    val speakResponsesEnabled = MutableStateFlow(true)
    val currentWakeWord = MutableStateFlow("Hey Guardian")

    // Secure Vault State
    val isVaultUnlocked = MutableStateFlow(false)
    val passwordManagerPIN = "1234" // Default vault PIN for demo lock
    private val _wrongAttempts = MutableStateFlow(0)
    val wrongAttempts: StateFlow<Int> = _wrongAttempts.asStateFlow()
    val showWrongAttemptAlert = MutableStateFlow(false)

    // Web Protection Settings
    val safeSearchEnabled = MutableStateFlow(true)
    val phishingBlockedEnabled = MutableStateFlow(true)
    val malwareBlockedEnabled = MutableStateFlow(true)
    val dnsVpnFilterEnabled = MutableStateFlow(false)

    // Language & Settings
    val selectedLanguage = MutableStateFlow("English") // English or Hindi
    val speechRate = MutableStateFlow(1.0f)

    init {
        // Pre-populate some demo data if database is empty
        viewModelScope.launch {
            dao.getAllDomains().first().let { list ->
                if (list.isEmpty()) {
                    dao.insertDomain(BlacklistDomain(domain = "malware-phishing-zone.cc", category = "Phishing"))
                    dao.insertDomain(BlacklistDomain(domain = "unauthorized-tracker.io", category = "Malware"))
                    dao.insertDomain(BlacklistDomain(domain = "safe-educational-hub.org", category = "Custom_Allow", isAllowed = true))
                }
            }

            dao.getAllRoutines().first().let { list ->
                if (list.isEmpty()) {
                    dao.insertRoutine(AutomationRoutine(name = "Midnight Hardening Mode", triggerPhrase = "lockdown", actionType = "SILENT_MODE"))
                    dao.insertRoutine(AutomationRoutine(name = "Voice Flashlight Activate", triggerPhrase = "lumos", actionType = "FLASH_LIGHT"))
                    dao.insertRoutine(AutomationRoutine(name = "Secure Camera Routine", triggerPhrase = "camera protect", actionType = "OPEN_CAMERA"))
                }
            }
        }
    }

    // --- Dashboard Actions ---
    fun runFullSecurityScan() {
        viewModelScope.launch {
            _isScanning.value = true
            // Dynamic scan animation simulation
            delay(3000)

            // Evaluate risk dynamically
            var score = 5
            if (!micMonitoringEnabled.value) score += 15
            if (!camMonitoringEnabled.value) score += 15
            if (locationMonitoringEnabled.value) score += 10
            if (!screenOverlayAlertsEnabled.value) score += 20
            if (!phishingBlockedEnabled.value) score += 15
            if (!malwareBlockedEnabled.value) score += 15
            if (wrongAttempts.value > 0) score += 10

            _riskScore.value = score

            val scanDetails = "Full AI security sweep completed. Core systems vetted. Dynamic risk index updated to $score."
            dao.insertThreatLog(
                ThreatLog(
                    threatType = "SECURITY_SCAN",
                    description = scanDetails,
                    riskLevel = if (score > 35) "WARNING" else "INFO"
                )
            )

            _isScanning.value = false

            if (speakResponsesEnabled.value) {
                ttsManager.speak("Security scan complete. Your Guardian risk score is $score percent. " +
                        if (score > 35) "I recommend securing your untuned controls immediately." else "Your systems are fully secured.")
            }
        }
    }

    // --- Chat & NLP Voice Actions ---
    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        val userMsg = ChatMessage("user", text)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            // Processing prompt with AisClient (either real Gemini or Offline smart NLP fallback)
            val reply = AisClient.generateResponse(text)
            
            // Check for voice automation keywords within user chat for Phone Management integration
            handleAutomationVoiceKeywords(text)

            val assistantMsg = ChatMessage("assistant", reply)
            _chatMessages.value = _chatMessages.value + assistantMsg

            if (speakResponsesEnabled.value) {
                ttsManager.speak(reply)
            }
        }
    }

    private fun handleAutomationVoiceKeywords(prompt: String) {
        val lower = prompt.lowercase()
        viewModelScope.launch {
            when {
                lower.contains("lockdown") -> {
                    micMonitoringEnabled.value = true
                    camMonitoringEnabled.value = true
                    screenOverlayAlertsEnabled.value = true
                    dao.insertThreatLog(ThreatLog(threatType = "ROUTINE_TRIGGER", description = "Midnight Hardening Routine activated by voice.", riskLevel = "INFO"))
                }
                lower.contains("flashlight") || lower.contains("lumos") || lower.contains("torch") -> {
                    dao.insertThreatLog(ThreatLog(threatType = "ROUTINE_TRIGGER", description = "Voice Flashlight triggered.", riskLevel = "INFO"))
                }
                lower.contains("hindi") -> {
                    selectedLanguage.value = "Hindi"
                }
                lower.contains("english") -> {
                    selectedLanguage.value = "English"
                }
            }
        }
    }

    fun startVoiceListening() {
        viewModelScope.launch {
            isListening.value = true
            // Simulate voice capture
            delay(2500)
            isListening.value = false
            
            // Random voice instructions simulated beautifully for Aryan
            val simulatedVoiceCommands = listOf(
                "Is my camera safe?",
                "Analyze my security risk score",
                "Encrypt standard photo vault files",
                "What is Web Protection?",
                "Trigger lockdown routine"
            )
            val randomCmd = simulatedVoiceCommands.random()
            sendMessage(randomCmd)
        }
    }

    // --- Secure Vault Passwords CRUD ---
    fun addPassword(service: String, user: String, plainVal: String, cat: String) {
        viewModelScope.launch {
            // Simulated AES wrapping of values
            val simulatedEncValue = "AES-Encrypted[$plainVal]"
            dao.insertPassword(
                Password(
                    serviceName = service,
                    username = user,
                    encryptedValue = simulatedEncValue,
                    category = cat
                )
            )
            dao.insertThreatLog(ThreatLog(threatType = "VAULT_ACCESS", description = "New credentials saved under biometric vault.", riskLevel = "INFO"))
        }
    }

    fun deletePassword(pwd: Password) {
        viewModelScope.launch {
            dao.deletePassword(pwd)
        }
    }

    // --- Secure Vault Files CRUD ---
    fun addVaultMedia(name: String, type: String, path: String, size: String) {
        viewModelScope.launch {
            dao.insertVaultMedia(
                VaultMedia(
                    fileName = name,
                    fileType = type,
                    simulatedPath = path,
                    fileSize = size,
                    encryptedKey = "AES-256-GCM"
                )
            )
            dao.insertThreatLog(ThreatLog(threatType = "VAULT_ACCESS", description = "Encrypted file locked inside Media Vault: $name ($size).", riskLevel = "INFO"))
        }
    }

    fun deleteVaultMedia(media: VaultMedia) {
        viewModelScope.launch {
            dao.deleteVaultMedia(media)
        }
    }

    fun lockVault() {
        isVaultUnlocked.value = false
    }

    fun unlockVault(pin: String): Boolean {
        if (pin == passwordManagerPIN) {
            isVaultUnlocked.value = true
            _wrongAttempts.value = 0
            showWrongAttemptAlert.value = false
            return true
        } else {
            val nextAttempts = _wrongAttempts.value + 1
            _wrongAttempts.value = nextAttempts
            if (nextAttempts >= 3) {
                showWrongAttemptAlert.value = true
                viewModelScope.launch {
                    dao.insertThreatLog(
                        ThreatLog(
                            threatType = "BRUTE_FORCE_ALERT",
                            description = "Multiple failed biometric/passcode attempts on Secure Vault. High Alert.",
                            riskLevel = "CRITICAL"
                        )
                    )
                }
            }
            return false
        }
    }

    // --- Web Protection CRUD ---
    fun addWebDomain(domainName: String, cat: String, isAllowed: Boolean) {
        viewModelScope.launch {
            dao.insertDomain(
                BlacklistDomain(
                    domain = domainName,
                    category = cat,
                    isAllowed = isAllowed
                )
            )
            dao.insertThreatLog(
                ThreatLog(
                    threatType = "WEBSHIELD_UPDATE",
                    description = "Updated Web Domain filter: $domainName added to $cat.",
                    riskLevel = "INFO"
                )
            )
        }
    }

    fun deleteWebDomain(domainId: Int) {
        viewModelScope.launch {
            dao.deleteDomainById(domainId)
        }
    }

    // --- Routines CRUD ---
    fun addRoutine(name: String, phrase: String, action: String, param: String = "") {
        viewModelScope.launch {
            dao.insertRoutine(
                AutomationRoutine(
                    name = name,
                    triggerPhrase = phrase,
                    actionType = action,
                    parameter = param
                )
            )
        }
    }

    fun deleteRoutine(routine: AutomationRoutine) {
        viewModelScope.launch {
            dao.deleteRoutine(routine)
        }
    }

    fun toggleRoutine(routine: AutomationRoutine) {
        viewModelScope.launch {
            dao.updateRoutine(routine.copy(isActive = !routine.isActive))
        }
    }

    fun clearAllThreatLogs() {
        viewModelScope.launch {
            dao.clearThreatLogs()
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
