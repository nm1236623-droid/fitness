package com.example.fitness.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

data class DietRecord(
    val id: String = UUID.randomUUID().toString(),
    val foodName: String,
    val calories: Int,
    val date: LocalDate,
    val mealType: String, // e.g., 早餐/午餐/晚餐/點心
    // estimated macros (grams)
    val proteinG: Double? = null,
    val carbsG: Double? = null,
    val fatG: Double? = null,
    /**
     * 存毫秒時間戳（epochMillis），避免不同 Android/Gradle 設定下 java.time.Instant 造成編譯/反序列化問題。
     * Firestore 也直接存這個 Long。
     */
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String? = null,
    // 可選：AI 分析出的細項（例如食材清單/描述），先用 List<String> 儲存
    val items: List<String>? = null,
    val rawAnalysisText: String? = null
)

/**
 * 飲食日誌 Repository（Facade）
 *
 * 原則：不在本地保存任何 list/state，單一資料源 = Firestore Snapshot.
 */
class DietRecordRepository {

    val records: StateFlow<List<DietRecord>> = FirebaseDietRecordRepository.records

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun addRecord(record: DietRecord) {
        scope.launch { FirebaseDietRecordRepository.addRecord(record) }
    }

    fun remove(id: String) {
        scope.launch { FirebaseDietRecordRepository.deleteRecord(id) }
    }

    fun clear() {
        scope.launch { FirebaseDietRecordRepository.clear() }
    }

    suspend fun getRecordsForDate(date: LocalDate): List<DietRecord> {
        return FirebaseDietRecordRepository.records.first().filter { it.date == date }
    }

    fun updateRecord(record: DietRecord) {
        scope.launch { FirebaseDietRecordRepository.updateRecord(record) }
    }
}
