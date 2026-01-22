package com.example.myapplication.repository

import android.content.Context
import com.example.myapplication.model.KanaCharacter
import com.example.myapplication.model.KanaData
import com.example.myapplication.model.KanaRow
import com.google.gson.Gson
import java.io.InputStreamReader

/**
 * Repository for managing Kana (Hiragana and Katakana) data
 * Optimized with caching - no reflection used
 */
class KanaRepository(private val context: Context) {

    private var kanaData: KanaData? = null

    // Cached results to avoid repeated computation
    private var cachedAllKana: List<KanaCharacter>? = null
    private var cachedGroupedKana: Map<String, List<KanaCharacter>>? = null
    private var cachedHiraganaCharacters: List<KanaCharacter>? = null
    private var cachedKatakanaCharacters: List<KanaCharacter>? = null

    init {
        loadData()
    }

    private fun loadData() {
        try {
            val gson = Gson()
            context.assets.open("data.json").use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    kanaData = gson.fromJson(reader, KanaData::class.java)
                }
            }

            // Pre-build caches after loading
            buildCaches()
        } catch (e: Exception) {
            android.util.Log.e("KanaRepository", "Error loading kana data", e)
        }
    }

    /**
     * Build all caches at once to avoid repeated computation
     */
    private fun buildCaches() {
        // Build grouped map cache (no reflection - direct access)
        val groupedMap = linkedMapOf<String, List<KanaCharacter>>()

        kanaData?.hiragana?.let { hira ->
            hira.a_gyo?.let { groupedMap["히라가나 - 아행"] = it }
            hira.ka_gyo?.let { groupedMap["히라가나 - 카행"] = it }
            hira.sa_gyo?.let { groupedMap["히라가나 - 사행"] = it }
            hira.ta_gyo?.let { groupedMap["히라가나 - 타행"] = it }
            hira.na_gyo?.let { groupedMap["히라가나 - 나행"] = it }
            hira.ha_gyo?.let { groupedMap["히라가나 - 하행"] = it }
            hira.ma_gyo?.let { groupedMap["히라가나 - 마행"] = it }
            hira.ya_gyo?.let { groupedMap["히라가나 - 야행"] = it }
            hira.ra_gyo?.let { groupedMap["히라가나 - 라행"] = it }
            hira.wa_gyo?.let { groupedMap["히라가나 - 와행"] = it }
        }

        kanaData?.katakana?.let { kata ->
            kata.a_gyo?.let { groupedMap["가타카나 - 아행"] = it }
            kata.ka_gyo?.let { groupedMap["가타카나 - 카행"] = it }
            kata.sa_gyo?.let { groupedMap["가타카나 - 사행"] = it }
            kata.ta_gyo?.let { groupedMap["가타카나 - 타행"] = it }
            kata.na_gyo?.let { groupedMap["가타카나 - 나행"] = it }
            kata.ha_gyo?.let { groupedMap["가타카나 - 하행"] = it }
            kata.ma_gyo?.let { groupedMap["가타카나 - 마행"] = it }
            kata.ya_gyo?.let { groupedMap["가타카나 - 야행"] = it }
            kata.ra_gyo?.let { groupedMap["가타카나 - 라행"] = it }
            kata.wa_gyo?.let { groupedMap["가타카나 - 와행"] = it }
        }
        cachedGroupedKana = groupedMap

        // Build hiragana and katakana character lists (no reflection)
        cachedHiraganaCharacters = buildCharacterList(kanaData?.hiragana)
        cachedKatakanaCharacters = buildCharacterList(kanaData?.katakana)

        // Build combined list
        val allKana = mutableListOf<KanaCharacter>()
        cachedHiraganaCharacters?.let { allKana.addAll(it) }
        cachedKatakanaCharacters?.let { allKana.addAll(it) }
        cachedAllKana = allKana
    }

    /**
     * Build character list from KanaRow without reflection
     */
    private fun buildCharacterList(data: KanaRow?): List<KanaCharacter> {
        if (data == null) return emptyList()
        val characters = mutableListOf<KanaCharacter>()
        data.a_gyo?.let { characters.addAll(it) }
        data.ka_gyo?.let { characters.addAll(it) }
        data.sa_gyo?.let { characters.addAll(it) }
        data.ta_gyo?.let { characters.addAll(it) }
        data.na_gyo?.let { characters.addAll(it) }
        data.ha_gyo?.let { characters.addAll(it) }
        data.ma_gyo?.let { characters.addAll(it) }
        data.ya_gyo?.let { characters.addAll(it) }
        data.ra_gyo?.let { characters.addAll(it) }
        data.wa_gyo?.let { characters.addAll(it) }
        return characters
    }

    /**
     * Get all Kana characters (both Hiragana and Katakana) - cached
     */
    fun getAllKanaList(): List<KanaCharacter> {
        return cachedAllKana ?: emptyList()
    }

    /**
     * Get Kana characters grouped by row (a-gyo, ka-gyo, etc.) - cached
     */
    fun getGroupedKanaList(): Map<String, List<KanaCharacter>> {
        return cachedGroupedKana ?: emptyMap()
    }

    /**
     * Get Hiragana data
     */
    fun getHiraganaData(): KanaRow? = kanaData?.hiragana

    /**
     * Get Katakana data
     */
    fun getKatakanaData(): KanaRow? = kanaData?.katakana

    /**
     * Get selected characters from specific rows - optimized without reflection
     */
    fun getSelectedCharacters(kanaType: String, selectedRows: List<String>): List<KanaCharacter> {
        val data = when (kanaType) {
            "hiragana" -> kanaData?.hiragana
            "katakana" -> kanaData?.katakana
            else -> null
        }

        if (data == null || selectedRows.isEmpty()) return emptyList()

        val characters = mutableListOf<KanaCharacter>()

        // Direct property access instead of reflection
        if (selectedRows.contains("아행")) data.a_gyo?.let { characters.addAll(it) }
        if (selectedRows.contains("카행")) data.ka_gyo?.let { characters.addAll(it) }
        if (selectedRows.contains("사행")) data.sa_gyo?.let { characters.addAll(it) }
        if (selectedRows.contains("타행")) data.ta_gyo?.let { characters.addAll(it) }
        if (selectedRows.contains("나행")) data.na_gyo?.let { characters.addAll(it) }
        if (selectedRows.contains("하행")) data.ha_gyo?.let { characters.addAll(it) }
        if (selectedRows.contains("마행")) data.ma_gyo?.let { characters.addAll(it) }
        if (selectedRows.contains("야행")) data.ya_gyo?.let { characters.addAll(it) }
        if (selectedRows.contains("라행")) data.ra_gyo?.let { characters.addAll(it) }
        if (selectedRows.contains("와행")) data.wa_gyo?.let { characters.addAll(it) }

        return characters
    }
}
