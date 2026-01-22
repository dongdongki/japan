package com.example.myapplication.repository

import android.content.Context
import com.example.myapplication.model.Sentence
import com.example.myapplication.model.UnknownWord
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 배치 정보를 담는 데이터 클래스
 */
data class SentenceBatchInfo(
    val batchId: String,           // 고유 ID (UUID)
    val batchNumber: Int,          // 표시용 번호 (1, 2, 3...)
    val sentenceCount: Int,        // 문장 개수
    val wordCount: Int,            // 사용된 단어 개수
    val usedWords: List<String>,   // 사용된 단어 목록
    val createdAt: Long,           // 생성 시간
    val unknownWordCount: Int = 0  // 모르는 단어 개수
)

/**
 * 배치별 문장 데이터를 담는 클래스
 * Note: unknownWords는 nullable로 선언하여 기존 데이터 호환성 유지
 */
data class SentenceBatchData(
    val info: SentenceBatchInfo,
    val sentences: List<Sentence>,
    val unknownWords: List<UnknownWord>? = null  // AI가 추가한 모르는 단어들 (nullable for backward compatibility)
) {
    // 안전하게 unknownWords 접근
    fun getUnknownWordsSafe(): List<UnknownWord> = unknownWords ?: emptyList()
}

/**
 * Repository for managing Sentence data
 * 새로운 설계: 배치별로 별도 파일 저장, 전역 유일 ID 사용
 */
@Singleton
class SentenceRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val gson = Gson()
    private val batchDir: File by lazy {
        File(context.filesDir, "sentence_batches").also {
            if (!it.exists()) it.mkdirs()
        }
    }

    // 전역 유일 ID를 위한 SharedPreferences
    private val prefs by lazy {
        context.getSharedPreferences("sentence_repository_prefs", Context.MODE_PRIVATE)
    }

    // 캐시된 배치 목록
    private var cachedBatches: MutableList<SentenceBatchData> = mutableListOf()
    private var cacheLoaded = false

    // 삭제된 문장 ID 목록 (weak sentences 정리용)
    private var lastDeletedSentenceIds: List<Int> = emptyList()

    companion object {
        private const val KEY_NEXT_SENTENCE_ID = "next_sentence_id"
        private const val SENTENCE_ID_START = 20000  // 문장 ID는 20000부터 시작 (단어/노래와 구분)
    }

    init {
        migrateOldData()
        loadAllBatches()
    }

    /**
     * 기존 데이터 마이그레이션 (한 번만 실행)
     */
    private fun migrateOldData() {
        val oldFile = File(context.filesDir, "generated_sentences.json")
        val oldPrefs = context.getSharedPreferences("sentence_batches", Context.MODE_PRIVATE)
        val migrationDone = context.getSharedPreferences("migration_prefs", Context.MODE_PRIVATE)
            .getBoolean("sentence_migration_v2_done", false)

        if (migrationDone) return

        android.util.Log.d("SentenceRepository", "마이그레이션 v2 시작...")

        // 기존 SharedPreferences 데이터 삭제
        oldPrefs.edit().clear().apply()

        // 기존 파일 삭제
        if (oldFile.exists()) {
            oldFile.delete()
            android.util.Log.d("SentenceRepository", "기존 generated_sentences.json 삭제됨")
        }

        // 기존 배치 폴더 내용 삭제 (기존 ID 시스템과 충돌 방지)
        if (batchDir.exists()) {
            batchDir.listFiles()?.forEach { it.delete() }
        }

        // 전역 ID 초기화
        prefs.edit().putInt(KEY_NEXT_SENTENCE_ID, SENTENCE_ID_START).apply()

        // 마이그레이션 완료 표시
        context.getSharedPreferences("migration_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("sentence_migration_v2_done", true)
            .apply()

        android.util.Log.d("SentenceRepository", "마이그레이션 v2 완료 - 전역 ID 시스템 적용")
    }

    /**
     * 다음 전역 유일 문장 ID 가져오기 및 증가
     */
    private fun getNextGlobalSentenceId(): Int {
        val currentId = prefs.getInt(KEY_NEXT_SENTENCE_ID, SENTENCE_ID_START)
        prefs.edit().putInt(KEY_NEXT_SENTENCE_ID, currentId + 1).apply()
        return currentId
    }

    /**
     * 모든 배치 로드
     */
    private fun loadAllBatches() {
        cachedBatches.clear()

        val files = batchDir.listFiles { file -> file.extension == "json" } ?: emptyArray()

        files.forEach { file ->
            try {
                val batchData = gson.fromJson(file.readText(), SentenceBatchData::class.java)
                cachedBatches.add(batchData)
                android.util.Log.d("SentenceRepository", "배치 로드: ${batchData.info.batchNumber}회차, ${batchData.sentences.size}개 문장")
            } catch (e: Exception) {
                android.util.Log.e("SentenceRepository", "배치 파일 로드 실패: ${file.name}", e)
            }
        }

        // 배치 번호순 정렬
        cachedBatches.sortBy { it.info.batchNumber }
        cacheLoaded = true

        android.util.Log.d("SentenceRepository", "총 ${cachedBatches.size}개 배치 로드됨")
    }

    /**
     * 모든 배치 정보 가져오기
     */
    fun getAllBatches(): List<SentenceBatchInfo> {
        if (!cacheLoaded) loadAllBatches()
        return cachedBatches.map { it.info }
    }

    /**
     * 다음 배치 번호 가져오기
     */
    fun getNextBatchNumber(): Int {
        if (!cacheLoaded) loadAllBatches()
        return if (cachedBatches.isEmpty()) 1 else cachedBatches.maxOf { it.info.batchNumber } + 1
    }

    /**
     * 새 배치 저장 (전역 유일 ID 사용)
     */
    fun saveBatch(
        sentences: List<Sentence>,
        usedWords: List<String>,
        unknownWords: List<UnknownWord> = emptyList()
    ): SentenceBatchInfo {
        if (!cacheLoaded) loadAllBatches()

        val batchId = UUID.randomUUID().toString()
        val batchNumber = getNextBatchNumber()

        val info = SentenceBatchInfo(
            batchId = batchId,
            batchNumber = batchNumber,
            sentenceCount = sentences.size,
            wordCount = usedWords.size,
            usedWords = usedWords,
            createdAt = System.currentTimeMillis(),
            unknownWordCount = unknownWords.size
        )

        // 문장에 전역 유일 ID 부여 (20000부터 시작)
        val sentencesWithId = sentences.map { sentence ->
            sentence.copy(id = getNextGlobalSentenceId())
        }

        val batchData = SentenceBatchData(info, sentencesWithId, unknownWords)

        // 파일로 저장
        val file = File(batchDir, "batch_$batchId.json")
        file.writeText(gson.toJson(batchData))

        // 캐시 업데이트
        cachedBatches.add(batchData)
        cachedBatches.sortBy { it.info.batchNumber }

        android.util.Log.d("SentenceRepository", "배치 저장 완료: ${batchNumber}회차, ${sentences.size}개 문장, ${unknownWords.size}개 모르는 단어, ID=$batchId")
        android.util.Log.d("SentenceRepository", "문장 ID 범위: ${sentencesWithId.firstOrNull()?.id} ~ ${sentencesWithId.lastOrNull()?.id}")

        return info
    }

    /**
     * 특정 배치의 모르는 단어 가져오기
     */
    fun getUnknownWordsByBatchId(batchId: String): List<UnknownWord> {
        if (!cacheLoaded) loadAllBatches()
        return cachedBatches.find { it.info.batchId == batchId }?.getUnknownWordsSafe() ?: emptyList()
    }

    /**
     * 모든 배치의 모르는 단어 가져오기 (중복 제거용)
     */
    fun getAllUnknownWords(): Set<String> {
        if (!cacheLoaded) loadAllBatches()
        return cachedBatches
            .flatMap { it.getUnknownWordsSafe() }
            .flatMap { listOf(it.word, it.reading) }
            .toSet()
    }

    /**
     * 배치 삭제 (삭제된 문장 ID 반환)
     */
    fun deleteBatch(batchId: String): Boolean {
        val file = File(batchDir, "batch_$batchId.json")

        // 삭제 전 문장 ID 목록 저장 (weak sentences 정리용)
        val batchToDelete = cachedBatches.find { it.info.batchId == batchId }
        lastDeletedSentenceIds = batchToDelete?.sentences?.map { it.id } ?: emptyList()

        if (file.exists()) {
            val deleted = file.delete()
            if (deleted) {
                // 캐시에서 제거
                cachedBatches.removeAll { it.info.batchId == batchId }

                // 배치 번호 재정렬
                renumberBatches()

                android.util.Log.d("SentenceRepository", "배치 삭제 완료: $batchId, 삭제된 문장 ID: ${lastDeletedSentenceIds.size}개")
                return true
            }
        }

        android.util.Log.e("SentenceRepository", "배치 삭제 실패: $batchId")
        return false
    }

    /**
     * 마지막으로 삭제된 문장 ID 목록 가져오기
     */
    fun getLastDeletedSentenceIds(): List<Int> = lastDeletedSentenceIds

    /**
     * 배치 번호 재정렬
     */
    private fun renumberBatches() {
        cachedBatches.sortBy { it.info.createdAt }

        cachedBatches.forEachIndexed { index, batchData ->
            val newNumber = index + 1
            if (batchData.info.batchNumber != newNumber) {
                val updatedInfo = batchData.info.copy(batchNumber = newNumber)
                val updatedData = batchData.copy(info = updatedInfo)

                // 파일 업데이트
                val file = File(batchDir, "batch_${batchData.info.batchId}.json")
                file.writeText(gson.toJson(updatedData))

                // 캐시 업데이트
                cachedBatches[index] = updatedData
            }
        }
    }

    /**
     * 특정 배치의 문장 가져오기
     */
    fun getSentencesByBatchId(batchId: String): List<Sentence> {
        if (!cacheLoaded) loadAllBatches()
        return cachedBatches.find { it.info.batchId == batchId }?.sentences ?: emptyList()
    }

    /**
     * 모든 문장 가져오기 (모든 배치 합침)
     */
    fun getSentences(): List<Sentence> {
        if (!cacheLoaded) loadAllBatches()
        return cachedBatches.flatMap { it.sentences }
    }

    /**
     * 문장 ID로 검색
     */
    fun getSentenceById(id: Int): Sentence? {
        return getSentences().find { it.id == id }
    }

    /**
     * 유효한 문장 ID인지 확인 (현재 존재하는 문장인지)
     */
    fun isValidSentenceId(id: Int): Boolean {
        return getSentences().any { it.id == id }
    }

    /**
     * 유효한 문장 ID만 필터링 (삭제된 배치의 ID 제거용)
     */
    fun filterValidSentenceIds(ids: Set<Int>): Set<Int> {
        val allValidIds = getSentences().map { it.id }.toSet()
        return ids.filter { it in allValidIds || it < SENTENCE_ID_START }.toSet()
    }

    /**
     * 모든 문장 삭제
     */
    fun clearSentences() {
        batchDir.listFiles()?.forEach { it.delete() }
        cachedBatches.clear()
        // ID 카운터 초기화
        prefs.edit().putInt(KEY_NEXT_SENTENCE_ID, SENTENCE_ID_START).apply()
        android.util.Log.d("SentenceRepository", "모든 문장 삭제됨, ID 카운터 초기화")
    }
}
