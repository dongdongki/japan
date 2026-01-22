package com.example.myapplication.ui

/**
 * Interface for adapters that support toggling Korean meaning visibility
 */
interface MeaningToggleable {
    /**
     * Toggle the visibility of Korean meanings
     * @return true if meanings are now visible, false if hidden
     */
    fun toggleMeaning(): Boolean

    /**
     * Set the visibility of Korean meanings
     * @param show true to show meanings, false to hide
     */
    fun setMeaningVisibility(show: Boolean)
}
