package com.example.myapplication.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.model.DailyWord
import com.example.myapplication.model.GeneratedSentence
import com.example.myapplication.model.SentenceGenerationResult
import com.example.myapplication.model.UnknownWord
import com.example.myapplication.service.Message
import com.example.myapplication.service.OpenAIRequest
import com.example.myapplication.service.OpenAIResponse
import com.example.myapplication.service.OpenAIService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class SentenceGeneratorRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sentence_generator_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_API_KEY = "api_key"
        private const val DEFAULT_API_KEY =
            "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"

        private const val KEY_TOTAL_REQUESTS = "total_requests"
        private const val KEY_TOTAL_TOKENS = "total_tokens"
        private const val KEY_TOTAL_COST = "total_cost"

        // 그룹 크기 설정 (의미 그룹화용)
        private const val GROUP_SIZE = 8  // 연관 단어 그룹 크기
        private const val MAX_RETRY_ROUNDS = 3  // 미사용 단어 재시도 최대 횟수
    }

    private val openAIService: OpenAIService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAIService::class.java)
    }

    fun saveApiKey(apiKey: String) {
        prefs.edit().putString(KEY_API_KEY, apiKey).apply()
    }

    fun getApiKey(): String? {
        return prefs.getString(KEY_API_KEY, DEFAULT_API_KEY)
    }

    fun getTotalRequests(): Int = prefs.getInt(KEY_TOTAL_REQUESTS, 0)
    fun getTotalTokens(): Int = prefs.getInt(KEY_TOTAL_TOKENS, 0)
    fun getTotalCost(): Double = prefs.getFloat(KEY_TOTAL_COST, 0f).toDouble()

    private fun updateUsageStats(tokens: Int, cost: Double) {
        prefs.edit()
            .putInt(KEY_TOTAL_REQUESTS, getTotalRequests() + 1)
            .putInt(KEY_TOTAL_TOKENS, getTotalTokens() + tokens)
            .putFloat(KEY_TOTAL_COST, (getTotalCost() + cost).toFloat())
            .apply()
    }

    suspend fun generateSentences(
        words: List<DailyWord>,
        existingUnknownWords: Set<String> = emptySet(),
        onProgress: ((current: Int, total: Int) -> Unit)? = null
    ): Result<SentenceGenerationResult> =
        withContext(Dispatchers.IO) {
            try {
                val apiKey = getApiKey()
                if (apiKey.isNullOrEmpty()) {
                    return@withContext Result.failure(Exception("API 키가 설정되지 않았습니다"))
                }

                onProgress?.invoke(0, words.size)

                android.util.Log.d("SentenceGeneratorRepository", "===== 문장 생성 시작 =====")
                android.util.Log.d("SentenceGeneratorRepository", "전체 단어 수: ${words.size}")

                // 1단계: AI로 의미적으로 연관된 단어들 그룹화
                onProgress?.invoke(1, words.size)
                val groupedWords = groupWordsBySemantic(words, apiKey)

                val allSentences = mutableListOf<GeneratedSentence>()
                val allUsedWordIds = mutableSetOf<Int>()
                val totalGroups = groupedWords.size

                android.util.Log.d("SentenceGeneratorRepository", "의미 그룹화 완료: ${totalGroups}개 그룹")

                // 2단계: 각 그룹별로 문장 생성
                for ((groupIndex, group) in groupedWords.withIndex()) {
                    val progressCurrent = 2 + (groupIndex + 1) * (words.size - 2) / totalGroups
                    onProgress?.invoke(progressCurrent, words.size)

                    android.util.Log.d("SentenceGeneratorRepository", "그룹 ${groupIndex + 1}/${totalGroups} 처리 중: ${group.map { it.word }.joinToString(", ")}")

                    val sentences = generateSentencesForGroup(group, apiKey)
                    allSentences.addAll(sentences)

                    // 사용된 단어 ID 수집
                    sentences.flatMap { it.usedWordIds }.forEach { allUsedWordIds.add(it) }

                    // 이 그룹의 미사용 단어 확인
                    val groupWordIds = group.map { it.id }.toSet()
                    val usedInGroup = sentences.flatMap { it.usedWordIds }.toSet()
                    val unusedInGroup = groupWordIds - usedInGroup

                    if (unusedInGroup.isNotEmpty()) {
                        android.util.Log.w("SentenceGeneratorRepository", "그룹 ${groupIndex + 1}: 미사용 단어 ${unusedInGroup.size}개")
                    } else {
                        android.util.Log.d("SentenceGeneratorRepository", "그룹 ${groupIndex + 1}: 모든 단어 사용됨 ✓")
                    }
                }

                // 2차: 미사용 단어에 대해 재시도
                var retryRound = 0
                var remainingUnusedWords = words.filter { it.id !in allUsedWordIds }

                while (remainingUnusedWords.isNotEmpty() && retryRound < MAX_RETRY_ROUNDS) {
                    retryRound++
                    android.util.Log.d("SentenceGeneratorRepository", "===== 재시도 ${retryRound}/${MAX_RETRY_ROUNDS}: 미사용 단어 ${remainingUnusedWords.size}개 =====")

                    // 미사용 단어를 작은 그룹으로 나누어 처리 (3-4개씩)
                    val retryGroups = remainingUnusedWords.chunked(4)

                    for ((retryIndex, retryGroup) in retryGroups.withIndex()) {
                        android.util.Log.d("SentenceGeneratorRepository", "재시도 그룹 ${retryIndex + 1}/${retryGroups.size}: ${retryGroup.map { it.word }.joinToString(", ")}")

                        val sentences = generateSentencesForUnusedWords(retryGroup, apiKey)
                        allSentences.addAll(sentences)

                        // 새로 사용된 단어 ID 수집
                        sentences.flatMap { it.usedWordIds }.forEach { allUsedWordIds.add(it) }
                    }

                    // 여전히 미사용인 단어 확인
                    remainingUnusedWords = words.filter { it.id !in allUsedWordIds }
                }

                // 최종 결과 확인
                android.util.Log.d("SentenceGeneratorRepository", "===== 최종 결과 =====")
                val finalUnusedWordIds = words.map { it.id }.toSet() - allUsedWordIds

                android.util.Log.d("SentenceGeneratorRepository", "전체 단어: ${words.size}개")
                android.util.Log.d("SentenceGeneratorRepository", "사용된 단어: ${allUsedWordIds.size}개")
                android.util.Log.d("SentenceGeneratorRepository", "미사용 단어: ${finalUnusedWordIds.size}개")

                val unusedWordDetails = mutableListOf<Pair<Int, String>>()
                if (finalUnusedWordIds.isNotEmpty()) {
                    android.util.Log.w("SentenceGeneratorRepository", "⚠️ 최종 미사용 단어:")
                    finalUnusedWordIds.forEach { id ->
                        val word = words.find { it.id == id }
                        android.util.Log.w("SentenceGeneratorRepository", "  - ${word?.word} (${word?.meaning})")
                        if (word != null) {
                            unusedWordDetails.add(id to "${word.word} (${word.meaning})")
                        }
                    }
                } else {
                    android.util.Log.d("SentenceGeneratorRepository", "✓ 모든 단어가 문장에 사용되었습니다!")
                }

                // 모르는 단어 추출 (문장에서 제공 단어 외 사용된 단어들, 기존 모르는 단어 제외)
                android.util.Log.d("SentenceGeneratorRepository", "모르는 단어 추출 시작... (기존 모르는 단어 ${existingUnknownWords.size}개 제외)")
                val unknownWords = extractUnknownWords(allSentences, words, apiKey, existingUnknownWords)
                android.util.Log.d("SentenceGeneratorRepository", "새로운 모르는 단어 ${unknownWords.size}개 추출됨")

                val result = SentenceGenerationResult(
                    sentences = allSentences,
                    totalWords = words.size,
                    usedWords = allUsedWordIds.size,
                    unusedWords = finalUnusedWordIds.size,
                    groupCount = totalGroups,
                    unusedWordDetails = unusedWordDetails,
                    unknownWords = unknownWords
                )

                Result.success(result)

            } catch (e: Exception) {
                android.util.Log.e("SentenceGeneratorRepository", "문장 생성 예외 발생", e)
                Result.failure(Exception("문장 생성 실패: ${e.message}", e))
            }
        }

    /**
     * 그룹의 단어들로 문장 생성
     */
    private suspend fun generateSentencesForGroup(words: List<DailyWord>, apiKey: String): List<GeneratedSentence> {
        val prompt = buildSentencePrompt(words)
        return callOpenAIForSentences(prompt, words, apiKey)
    }

    /**
     * 미사용 단어 전용 문장 생성 (더 강력한 프롬프트)
     */
    private suspend fun generateSentencesForUnusedWords(words: List<DailyWord>, apiKey: String): List<GeneratedSentence> {
        val prompt = buildRetryPrompt(words)
        return callOpenAIForSentences(prompt, words, apiKey)
    }

    /**
     * OpenAI API 호출 및 문장 파싱
     */
    private suspend fun callOpenAIForSentences(prompt: String, words: List<DailyWord>, apiKey: String): List<GeneratedSentence> {
        val request = OpenAIRequest(
            model = "gpt-4o-mini",
            messages = listOf(
                Message(role = "system", content = "You are a Japanese language teacher. You MUST use every single word provided - this is mandatory, not optional."),
                Message(role = "user", content = prompt)
            ),
            temperature = 0.7,
            maxCompletionTokens = 2000
        )

        val response = openAIService.createChatCompletion(
            authorization = "Bearer $apiKey",
            request = request
        )

        if (response.isSuccessful && response.body() != null) {
            val responseBody = response.body()!!
            val content = responseBody.choices.firstOrNull()?.message?.content

            if (content != null && content.isNotEmpty()) {
                // Cost calculation
                val inputCost = responseBody.usage.promptTokens * 0.150 / 1_000_000
                val outputCost = responseBody.usage.completionTokens * 0.600 / 1_000_000
                val totalCost = inputCost + outputCost
                updateUsageStats(responseBody.usage.totalTokens, totalCost)

                return parseSentences(content, words)
            }
        } else {
            val errorDetail = parseErrorResponse(response)
            android.util.Log.e("SentenceGeneratorRepository", "API 오류: $errorDetail")
        }

        return emptyList()
    }

    private fun buildSentencePrompt(words: List<DailyWord>): String {
        val wordList = words.mapIndexed { index, word ->
            "${index}. ${word.word} (${word.reading}): ${word.meaning}"
        }.joinToString("\n")

        return """
아래 ${words.size}개의 일본어 단어들을 활용하여 짧고 자연스러운 문장들을 만들어주세요.
연관된 단어들을 최대한 한 문장에 조합해서 문장 수를 줄여주세요.

【단어 목록】
$wordList

【핵심 규칙】
✓ 모든 단어는 반드시 사용되어야 합니다 (빠지는 단어 없이)
✓ 연관된 단어 2-4개를 한 문장에 자연스럽게 조합
✓ 문장은 짧고 간결하게 (10-20자 정도)
✓ 일상에서 자주 쓰는 자연스러운 표현으로

【좋은 예시】
- "学校"(학교) + "行く"(가다) + "毎日"(매일) → "毎日学校に行きます" (매일 학교에 갑니다)
- "食べる"(먹다) + "美味しい"(맛있다) + "料理"(요리) → "美味しい料理を食べた" (맛있는 요리를 먹었다)
- "友達"(친구) + "会う"(만나다) + "駅"(역) → "駅で友達に会った" (역에서 친구를 만났다)

【응답 형식 - JSON 배열】
[
  {"japanese": "日本語文", "reading": "ひらがな", "korean": "한국어", "used_word_ids": [0, 2, 5]},
  {"japanese": "日本語文", "reading": "ひらがな", "korean": "한국어", "used_word_ids": [1, 3]},
  ...
]

반드시 모든 단어(0~${words.size - 1})가 used_word_ids에 포함되어야 합니다. JSON만 응답하세요.
        """.trimIndent()
    }

    /**
     * 미사용 단어 재시도용 강화 프롬프트
     */
    private fun buildRetryPrompt(words: List<DailyWord>): String {
        val wordList = words.mapIndexed { index, word ->
            "${index}. ${word.word} (${word.reading}): ${word.meaning}"
        }.joinToString("\n")

        return """
【긴급】 아래 단어들이 아직 문장에 사용되지 않았습니다.
이 단어들을 조합하여 짧고 자연스러운 문장을 만들어주세요.

【미사용 단어】
$wordList

【규칙】
✓ 모든 단어를 반드시 사용 (빠지면 안됨)
✓ 가능하면 2-3개 단어를 한 문장에 조합
✓ 조합이 어려우면 단어당 1개 문장도 OK
✓ 짧고 간결한 문장으로

【응답 형식 - JSON 배열】
[
  {"japanese": "日本語文", "reading": "ひらがな", "korean": "한국어", "used_word_ids": [0, 2]},
  {"japanese": "日本語文", "reading": "ひらがな", "korean": "한국어", "used_word_ids": [1]},
  ...
]

모든 단어 ID(0~${words.size - 1})가 반드시 포함되어야 합니다. JSON만 응답하세요.
        """.trimIndent()
    }

    private fun parseSentences(content: String, words: List<DailyWord>): List<GeneratedSentence> {
        return try {
            android.util.Log.d("SentenceGeneratorRepository", "AI 응답: ${content.take(200)}...")

            val cleanJson = content
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val type = object : TypeToken<List<Map<String, Any>>>() {}.type
            val parsed: List<Map<String, Any>> = gson.fromJson(cleanJson, type)

            android.util.Log.d("SentenceGeneratorRepository", "파싱된 문장 개수: ${parsed.size}")

            parsed.map { item ->
                val japanese = item["japanese"] as? String ?: ""
                val reading = item["reading"] as? String ?: ""
                val korean = item["korean"] as? String ?: ""

                val usedWordIds = (item["used_word_ids"] as? List<*>)
                    ?.mapNotNull { value ->
                        when (value) {
                            is Double -> value.toInt()
                            is Int -> value
                            else -> null
                        }
                    }?.map { index ->
                        if (index in words.indices) words[index].id else -1
                    }?.filter { it != -1 }
                    ?: emptyList()

                GeneratedSentence(japanese, reading, korean, usedWordIds)
            }

        } catch (e: Exception) {
            android.util.Log.e("SentenceGeneratorRepository", "문장 파싱 실패: ${e.message}", e)
            emptyList()
        }
    }

    private fun parseErrorResponse(response: retrofit2.Response<OpenAIResponse>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody != null) {
                val errorJson = gson.fromJson(errorBody, Map::class.java)
                val error = errorJson["error"] as? Map<*, *>

                if (error != null) {
                    val message = error["message"] as? String ?: "알 수 없는 오류"
                    val type = error["type"] as? String
                    val code = error["code"] as? String

                    buildString {
                        append(message)
                        if (type != null) append(" (타입: $type)")
                        if (code != null) append(" [코드: $code]")
                    }
                } else {
                    errorBody.take(200)
                }
            } else {
                response.message()
            }
        } catch (e: Exception) {
            response.message()
        }
    }

    /**
     * 문장에서 제공된 단어 외의 모르는 단어 추출
     */
    private suspend fun extractUnknownWords(
        sentences: List<GeneratedSentence>,
        providedWords: List<DailyWord>,
        apiKey: String,
        existingUnknownWords: Set<String> = emptySet()
    ): List<UnknownWord> {
        if (sentences.isEmpty()) return emptyList()

        try {
            // 제공된 단어 목록 (한자, 히라가나 모두 포함) + 기존에 추출된 모르는 단어
            val knownWordsSet = providedWords.flatMap { listOf(it.word, it.reading) }.toSet() + existingUnknownWords

            // 모든 문장 텍스트
            val allSentenceText = sentences.joinToString("\n") { it.japanese }

            val prompt = """
아래 일본어 문장들에서 사용된 단어 중, 제공된 단어 목록에 없는 "새로운 단어"만 추출해주세요.

【생성된 문장들】
$allSentenceText

【제공된 단어 목록 (이미 알고 있는 단어)】
${providedWords.joinToString(", ") { "${it.word}(${it.reading})" }}

【추출 규칙】
✓ 조사(は, が, を, に, で, と, の, へ, から, まで 등)는 제외
✓ 동사의 활용형은 기본형으로 변환 (食べました → 食べる)
✓ 형용사 활용형도 기본형으로 (美味しかった → 美味しい)
✓ 너무 기본적인 단어(です, ます, ある, いる, する, なる 등)는 제외
✓ 숫자, 고유명사는 제외
✓ 제공된 단어와 동일하거나 활용형인 경우 제외

【응답 형식 - JSON 배열】
[
  {"word": "漢字表記", "reading": "ひらがな", "meaning": "한국어뜻"},
  {"word": "漢字表記", "reading": "ひらがな", "meaning": "한국어뜻"}
]

새로운 단어가 없으면 빈 배열 []을 반환하세요. JSON만 응답하세요.
            """.trimIndent()

            val request = OpenAIRequest(
                model = "gpt-4o-mini",
                messages = listOf(
                    Message(role = "system", content = "You are a Japanese vocabulary extractor."),
                    Message(role = "user", content = prompt)
                ),
                temperature = 0.3,
                maxCompletionTokens = 2000
            )

            val response = openAIService.createChatCompletion(
                authorization = "Bearer $apiKey",
                request = request
            )

            if (response.isSuccessful && response.body() != null) {
                val content = response.body()!!.choices.firstOrNull()?.message?.content
                if (content != null) {
                    // Cost calculation
                    val responseBody = response.body()!!
                    val inputCost = responseBody.usage.promptTokens * 0.150 / 1_000_000
                    val outputCost = responseBody.usage.completionTokens * 0.600 / 1_000_000
                    updateUsageStats(responseBody.usage.totalTokens, inputCost + outputCost)

                    return parseUnknownWords(content, knownWordsSet)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SentenceGeneratorRepository", "모르는 단어 추출 실패: ${e.message}", e)
        }

        return emptyList()
    }

    /**
     * 모르는 단어 응답 파싱
     */
    private fun parseUnknownWords(content: String, knownWordsSet: Set<String>): List<UnknownWord> {
        return try {
            val cleanJson = content
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val type = object : TypeToken<List<Map<String, String>>>() {}.type
            val parsed: List<Map<String, String>> = gson.fromJson(cleanJson, type)

            parsed.mapNotNull { item ->
                val word = item["word"] ?: return@mapNotNull null
                val reading = item["reading"] ?: return@mapNotNull null
                val meaning = item["meaning"] ?: return@mapNotNull null

                // 이미 알고 있는 단어면 제외
                if (word in knownWordsSet || reading in knownWordsSet) {
                    return@mapNotNull null
                }

                UnknownWord(word, reading, meaning)
            }.distinctBy { it.word } // 중복 제거

        } catch (e: Exception) {
            android.util.Log.e("SentenceGeneratorRepository", "모르는 단어 파싱 실패: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * AI를 사용하여 의미적으로 연관된 단어들끼리 그룹화
     */
    private suspend fun groupWordsBySemantic(words: List<DailyWord>, apiKey: String): List<List<DailyWord>> {
        if (words.size <= GROUP_SIZE) {
            android.util.Log.d("SentenceGeneratorRepository", "단어 수가 적어 그룹화 생략")
            return listOf(words)
        }

        try {
            val wordListForGrouping = words.mapIndexed { index, word ->
                "$index: ${word.word} (${word.meaning})"
            }.joinToString("\n")

            val groupingPrompt = """
아래 일본어 단어들을 의미나 주제가 비슷한 것끼리 그룹으로 묶어주세요.

【단어 목록】
$wordListForGrouping

【그룹화 기준 예시】
- 학교/교육 관련: 학교, 선생님, 공부, 시험, 교실
- 음식 관련: 먹다, 요리, 맛있다, 식당, 밥
- 감정 관련: 기쁘다, 슬프다, 화나다, 즐겁다
- 시간/날씨 관련: 아침, 저녁, 비, 맑다
- 장소/이동 관련: 가다, 오다, 집, 역, 공원
- 동작/행동 관련: 하다, 보다, 듣다, 말하다

【필수 규칙】
✓ 모든 단어를 빠짐없이 그룹에 포함시켜야 합니다 (${words.size}개 전부)
✓ 한 그룹에 6-10개 단어 (너무 작거나 크면 안됨)
✓ 연관성이 낮은 단어는 "기타" 그룹으로
✓ 각 단어는 정확히 하나의 그룹에만 속해야 합니다

【응답 형식 - JSON】
{
  "groups": [
    {"theme": "그룹 주제", "word_ids": [0, 3, 5, 8, 12]},
    {"theme": "그룹 주제", "word_ids": [1, 2, 4, 6, 7]},
    ...
  ]
}

반드시 모든 단어 ID(0~${words.size - 1})가 포함되어야 합니다. JSON만 응답하세요.
            """.trimIndent()

            val request = OpenAIRequest(
                model = "gpt-4o-mini",
                messages = listOf(
                    Message(role = "system", content = "You are a helpful assistant that groups Japanese words by semantic similarity."),
                    Message(role = "user", content = groupingPrompt)
                ),
                temperature = 0.3,
                maxCompletionTokens = 1500
            )

            val response = openAIService.createChatCompletion(
                authorization = "Bearer $apiKey",
                request = request
            )

            if (response.isSuccessful && response.body() != null) {
                val content = response.body()!!.choices.firstOrNull()?.message?.content
                if (content != null) {
                    // Cost calculation
                    val responseBody = response.body()!!
                    val inputCost = responseBody.usage.promptTokens * 0.150 / 1_000_000
                    val outputCost = responseBody.usage.completionTokens * 0.600 / 1_000_000
                    updateUsageStats(responseBody.usage.totalTokens, inputCost + outputCost)

                    val groups = parseGroupingResponse(content, words)
                    if (groups.isNotEmpty()) {
                        android.util.Log.d("SentenceGeneratorRepository", "의미 그룹화 성공: ${groups.size}개 그룹")
                        groups.forEachIndexed { index, group ->
                            android.util.Log.d("SentenceGeneratorRepository", "  그룹 ${index + 1}: ${group.map { it.word }.joinToString(", ")}")
                        }
                        return groups
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SentenceGeneratorRepository", "의미 그룹화 실패: ${e.message}", e)
        }

        // 실패시 기본 chunked 분할
        android.util.Log.w("SentenceGeneratorRepository", "의미 그룹화 실패, 순차 분할로 대체")
        return words.chunked(GROUP_SIZE)
    }

    /**
     * 그룹화 응답 파싱
     */
    private fun parseGroupingResponse(content: String, words: List<DailyWord>): List<List<DailyWord>> {
        return try {
            val cleanJson = content
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val parsed = gson.fromJson(cleanJson, Map::class.java)
            val groups = parsed["groups"] as? List<*> ?: return emptyList()

            val usedIds = mutableSetOf<Int>()
            val result = mutableListOf<List<DailyWord>>()

            for (group in groups) {
                if (group is Map<*, *>) {
                    val wordIds = (group["word_ids"] as? List<*>)
                        ?.mapNotNull { id ->
                            when (id) {
                                is Double -> id.toInt()
                                is Int -> id
                                else -> null
                            }
                        }
                        ?.filter { it in words.indices && it !in usedIds }
                        ?: continue

                    if (wordIds.isNotEmpty()) {
                        val groupWords = wordIds.mapNotNull { id ->
                            usedIds.add(id)
                            words.getOrNull(id)
                        }
                        if (groupWords.isNotEmpty()) {
                            result.add(groupWords)
                        }
                    }
                }
            }

            // 누락된 단어가 있으면 추가 그룹으로
            val missingIds = words.indices.filter { it !in usedIds }
            if (missingIds.isNotEmpty()) {
                android.util.Log.w("SentenceGeneratorRepository", "그룹화에서 누락된 단어 ${missingIds.size}개 추가")
                val missingWords = missingIds.mapNotNull { words.getOrNull(it) }
                missingWords.chunked(GROUP_SIZE).forEach { result.add(it) }
            }

            result

        } catch (e: Exception) {
            android.util.Log.e("SentenceGeneratorRepository", "그룹화 응답 파싱 실패: ${e.message}", e)
            emptyList()
        }
    }
}
