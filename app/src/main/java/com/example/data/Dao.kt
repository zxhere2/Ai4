package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GuardianDao {
    // Passwords Manager Queries
    @Query("SELECT * FROM passwords ORDER BY serviceName ASC")
    fun getAllPasswords(): Flow<List<Password>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassword(password: Password)

    @Delete
    suspend fun deletePassword(password: Password)

    // Web Domain Queries
    @Query("SELECT * FROM web_domains ORDER BY addedTime DESC")
    fun getAllDomains(): Flow<List<BlacklistDomain>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDomain(domain: BlacklistDomain)

    @Delete
    suspend fun deleteDomain(domain: BlacklistDomain)

    @Query("DELETE FROM web_domains WHERE id = :domainId")
    suspend fun deleteDomainById(domainId: Int)

    // Vault Media Queries
    @Query("SELECT * FROM vault_media ORDER BY addedTime DESC")
    fun getAllVaultMedia(): Flow<List<VaultMedia>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaultMedia(media: VaultMedia)

    @Delete
    suspend fun deleteVaultMedia(media: VaultMedia)

    // Automation Routines Queries
    @Query("SELECT * FROM automation_routines ORDER BY creationTime DESC")
    fun getAllRoutines(): Flow<List<AutomationRoutine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: AutomationRoutine)

    @Update
    suspend fun updateRoutine(routine: AutomationRoutine)

    @Delete
    suspend fun deleteRoutine(routine: AutomationRoutine)

    // Threat Logs Queries
    @Query("SELECT * FROM threat_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentThreatLogs(): Flow<List<ThreatLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThreatLog(log: ThreatLog)

    @Query("DELETE FROM threat_logs")
    suspend fun clearThreatLogs()
}
