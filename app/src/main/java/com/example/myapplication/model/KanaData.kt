package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

data class KanaRow(
    @SerializedName("아행") val a_gyo: List<KanaCharacter>?,
    @SerializedName("카행") val ka_gyo: List<KanaCharacter>?,
    @SerializedName("사행") val sa_gyo: List<KanaCharacter>?,
    @SerializedName("타행") val ta_gyo: List<KanaCharacter>?,
    @SerializedName("나행") val na_gyo: List<KanaCharacter>?,
    @SerializedName("하행") val ha_gyo: List<KanaCharacter>?,
    @SerializedName("마행") val ma_gyo: List<KanaCharacter>?,
    @SerializedName("야행") val ya_gyo: List<KanaCharacter>?,
    @SerializedName("라행") val ra_gyo: List<KanaCharacter>?,
    @SerializedName("와행") val wa_gyo: List<KanaCharacter>?
) {
    /**
     * Get all rows as a map (row name -> characters)
     * This avoids reflection and provides O(1) access
     */
    fun getAllRowsAsMap(): LinkedHashMap<String, List<KanaCharacter>> {
        val map = LinkedHashMap<String, List<KanaCharacter>>()
        a_gyo?.takeIf { it.isNotEmpty() }?.let { map["A행"] = it }
        ka_gyo?.takeIf { it.isNotEmpty() }?.let { map["Ka행"] = it }
        sa_gyo?.takeIf { it.isNotEmpty() }?.let { map["Sa행"] = it }
        ta_gyo?.takeIf { it.isNotEmpty() }?.let { map["Ta행"] = it }
        na_gyo?.takeIf { it.isNotEmpty() }?.let { map["Na행"] = it }
        ha_gyo?.takeIf { it.isNotEmpty() }?.let { map["Ha행"] = it }
        ma_gyo?.takeIf { it.isNotEmpty() }?.let { map["Ma행"] = it }
        ya_gyo?.takeIf { it.isNotEmpty() }?.let { map["Ya행"] = it }
        ra_gyo?.takeIf { it.isNotEmpty() }?.let { map["Ra행"] = it }
        wa_gyo?.takeIf { it.isNotEmpty() }?.let { map["Wa행"] = it }
        return map
    }

    /**
     * Get all characters from all rows
     */
    fun getAllCharacters(): List<KanaCharacter> {
        return listOfNotNull(a_gyo, ka_gyo, sa_gyo, ta_gyo, na_gyo, ha_gyo, ma_gyo, ya_gyo, ra_gyo, wa_gyo)
            .flatten()
    }

    /**
     * Get characters from selected rows
     */
    fun getCharactersByRowNames(selectedRows: List<String>): List<KanaCharacter> {
        val characters = mutableListOf<KanaCharacter>()
        if (selectedRows.contains("a행") || selectedRows.contains("A행")) a_gyo?.let { characters.addAll(it) }
        if (selectedRows.contains("ka행") || selectedRows.contains("Ka행")) ka_gyo?.let { characters.addAll(it) }
        if (selectedRows.contains("sa행") || selectedRows.contains("Sa행")) sa_gyo?.let { characters.addAll(it) }
        if (selectedRows.contains("ta행") || selectedRows.contains("Ta행")) ta_gyo?.let { characters.addAll(it) }
        if (selectedRows.contains("na행") || selectedRows.contains("Na행")) na_gyo?.let { characters.addAll(it) }
        if (selectedRows.contains("ha행") || selectedRows.contains("Ha행")) ha_gyo?.let { characters.addAll(it) }
        if (selectedRows.contains("ma행") || selectedRows.contains("Ma행")) ma_gyo?.let { characters.addAll(it) }
        if (selectedRows.contains("ya행") || selectedRows.contains("Ya행")) ya_gyo?.let { characters.addAll(it) }
        if (selectedRows.contains("ra행") || selectedRows.contains("Ra행")) ra_gyo?.let { characters.addAll(it) }
        if (selectedRows.contains("wa행") || selectedRows.contains("Wa행")) wa_gyo?.let { characters.addAll(it) }
        return characters
    }
}

data class KanaData(
    val hiragana: KanaRow,
    val katakana: KanaRow
)
