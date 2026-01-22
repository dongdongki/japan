package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentSongLyricsBinding
import com.example.myapplication.model.Song
import com.google.gson.GsonBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class SongLyricsFragment : Fragment() {

    private var _binding: FragmentSongLyricsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuizViewModel by activityViewModels()

    private lateinit var mediaPlayerHelper: SongMediaPlayerHelper
    private var songDirectory: String = "pretender"
    private var lyricsList: MutableList<Song> = mutableListOf()
    private var isEditMode: Boolean = false
    private val lyricViews: MutableList<View> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongLyricsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        songDirectory = arguments?.getString("songDirectory") ?: "pretender"
        val songTitle = arguments?.getString("songTitle") ?: songDirectory.replaceFirstChar { it.uppercase() }

        binding.tvSongTitle.text = songTitle

        mediaPlayerHelper = SongMediaPlayerHelper(requireContext(), songDirectory)
        mediaPlayerHelper.initialize()

        lyricsList = viewModel.getSongVocabulary(songDirectory).toMutableList()
        displayLyrics()
        setupButtons()
    }

    private fun displayLyrics() {
        lyricsList.forEachIndexed { index, song ->
            val lyricView = layoutInflater.inflate(R.layout.item_lyric_line, binding.lyricsContainer, false)

            lyricView.findViewById<TextView>(R.id.tv_kanji).text = song.kanji
            lyricView.findViewById<TextView>(R.id.tv_hiragana).text = song.hiragana
            lyricView.findViewById<TextView>(R.id.tv_meaning).text = song.meaning

            val timestampStartTextView = lyricView.findViewById<TextView>(R.id.tv_timestamp_start)
            val timestampEndTextView = lyricView.findViewById<TextView>(R.id.tv_timestamp_end)

            timestampStartTextView.text = song.time ?: "00:00.0"
            val endTime = song.endTime ?: lyricsList.getOrNull(index + 1)?.time
                ?: SongMediaPlayerHelper.calculateEndTime(song.time)
            timestampEndTextView.text = endTime

            setupTimestampControls(lyricView, index)

            if (!song.time.isNullOrEmpty()) {
                lyricView.setOnClickListener { playLyricSection(index) }
                lyricView.isClickable = true
                lyricView.isFocusable = true
            }

            lyricViews.add(lyricView)
            binding.lyricsContainer.addView(lyricView)
        }
    }

    private fun setupTimestampControls(lyricView: View, index: Int) {
        lyricView.findViewById<TextView>(R.id.tv_timestamp_start).setOnClickListener {
            showTimestampEditDialog(index, isStart = true)
        }

        lyricView.findViewById<TextView>(R.id.tv_timestamp_end).setOnClickListener {
            showTimestampEditDialog(index, isStart = false)
        }

        lyricView.findViewById<Button>(R.id.btn_start_minus_1).setOnClickListener {
            adjustTimestamp(index, -1.0f, isStart = true)
        }
        lyricView.findViewById<Button>(R.id.btn_start_minus_01).setOnClickListener {
            adjustTimestamp(index, -0.1f, isStart = true)
        }
        lyricView.findViewById<Button>(R.id.btn_start_plus_01).setOnClickListener {
            adjustTimestamp(index, 0.1f, isStart = true)
        }
        lyricView.findViewById<Button>(R.id.btn_start_plus_1).setOnClickListener {
            adjustTimestamp(index, 1.0f, isStart = true)
        }

        lyricView.findViewById<Button>(R.id.btn_end_minus_1).setOnClickListener {
            adjustTimestamp(index, -1.0f, isStart = false)
        }
        lyricView.findViewById<Button>(R.id.btn_end_minus_01).setOnClickListener {
            adjustTimestamp(index, -0.1f, isStart = false)
        }
        lyricView.findViewById<Button>(R.id.btn_end_plus_01).setOnClickListener {
            adjustTimestamp(index, 0.1f, isStart = false)
        }
        lyricView.findViewById<Button>(R.id.btn_end_plus_1).setOnClickListener {
            adjustTimestamp(index, 1.0f, isStart = false)
        }
    }

    private fun setupButtons() {
        binding.btnEditMode.setOnClickListener { toggleEditMode() }
        binding.btnSave.setOnClickListener { saveTimestamps() }
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode

        lyricViews.forEach { lyricView ->
            val timestampControls = lyricView.findViewById<View>(R.id.timestamp_controls)
            timestampControls.visibility = if (isEditMode) View.VISIBLE else View.GONE
        }

        binding.btnSave.visibility = if (isEditMode) View.VISIBLE else View.GONE
        binding.btnEditMode.text = if (isEditMode) "닫기" else "편집"
    }

    private fun adjustTimestamp(index: Int, adjustment: Float, isStart: Boolean) {
        val song = lyricsList[index]
        val lyricView = lyricViews[index]

        if (isStart) {
            val currentTime = song.time?.let { parseTimestamp(it) } ?: 0f
            val newTime = (currentTime + adjustment).coerceAtLeast(0f)
            val newTimestamp = SongMediaPlayerHelper.formatTimestamp(newTime)

            lyricsList[index] = song.copy(time = newTimestamp)
            lyricView.findViewById<TextView>(R.id.tv_timestamp_start).text = newTimestamp
        } else {
            val currentEndTime = song.endTime ?: lyricsList.getOrNull(index + 1)?.time
                ?: SongMediaPlayerHelper.calculateEndTime(song.time)
            val currentTime = parseTimestamp(currentEndTime)
            val newTime = (currentTime + adjustment).coerceAtLeast(0f)
            val newTimestamp = SongMediaPlayerHelper.formatTimestamp(newTime)

            lyricsList[index] = song.copy(endTime = newTimestamp)
            lyricView.findViewById<TextView>(R.id.tv_timestamp_end).text = newTimestamp
        }
    }

    private fun showTimestampEditDialog(index: Int, isStart: Boolean) {
        val song = lyricsList[index]
        val currentTimestamp = if (isStart) {
            song.time ?: "00:00.0"
        } else {
            song.endTime ?: lyricsList.getOrNull(index + 1)?.time
                ?: SongMediaPlayerHelper.calculateEndTime(song.time)
        }

        val input = android.widget.EditText(requireContext())
        input.setText(currentTimestamp)
        input.inputType = android.text.InputType.TYPE_CLASS_TEXT
        input.setSelection(currentTimestamp.length)

        android.app.AlertDialog.Builder(requireContext())
            .setTitle(if (isStart) "시작 타임스탬프 입력" else "끝 타임스탬프 입력")
            .setMessage("형식: mm:ss.s (예: 01:23.4)")
            .setView(input)
            .setPositiveButton("확인") { _, _ ->
                val newTimestamp = input.text.toString()
                if (SongMediaPlayerHelper.isValidTimestamp(newTimestamp)) {
                    val lyricView = lyricViews[index]
                    if (isStart) {
                        lyricsList[index] = song.copy(time = newTimestamp)
                        lyricView.findViewById<TextView>(R.id.tv_timestamp_start).text = newTimestamp
                    } else {
                        lyricsList[index] = song.copy(endTime = newTimestamp)
                        lyricView.findViewById<TextView>(R.id.tv_timestamp_end).text = newTimestamp
                    }
                } else {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "잘못된 형식입니다. mm:ss.s 형식으로 입력해주세요",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun saveTimestamps() {
        try {
            val jsonData = lyricsList.map { song ->
                val data = mutableMapOf<String, String?>()
                data["time"] = song.time
                if (song.endTime != null) {
                    data["endTime"] = song.endTime
                }
                data["kanji"] = song.kanji
                data["hiragana"] = song.hiragana
                data["meaning"] = song.meaning
                data
            }

            val gson = GsonBuilder().setPrettyPrinting().create()
            val jsonString = gson.toJson(jsonData)

            val fileName = "${songDirectory}.json"
            val file = File(requireContext().filesDir, fileName)
            file.writeText(jsonString)

            viewModel.clearSongCache(songDirectory)

            android.widget.Toast.makeText(
                requireContext(),
                "타임스탬프가 저장되었습니다",
                android.widget.Toast.LENGTH_SHORT
            ).show()

            if (isEditMode) {
                toggleEditMode()
            }
        } catch (e: Exception) {
            android.util.Log.e("SongLyricsFragment", "Failed to save timestamps", e)
            android.widget.Toast.makeText(
                requireContext(),
                "저장 실패: ${e.message}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun playLyricSection(index: Int) {
        val currentLyric = lyricsList[index]
        val startTimestamp = currentLyric.time ?: return
        val endTimestamp = currentLyric.endTime
            ?: lyricsList.getOrNull(index + 1)?.time
            ?: SongMediaPlayerHelper.calculateEndTime(currentLyric.time)

        val currentLyricIdx = mediaPlayerHelper.getCurrentLyricIndex()
        val previousLyricView = if (currentLyricIdx != -1 && currentLyricIdx < lyricViews.size) {
            lyricViews[currentLyricIdx]
        } else null

        mediaPlayerHelper.playSection(
            index,
            startTimestamp,
            endTimestamp,
            lyricViews[index],
            previousLyricView
        )
    }

    private fun parseTimestamp(timestamp: String): Float {
        val parts = timestamp.split(":")
        return when (parts.size) {
            2 -> {
                val minutes = parts[0].toIntOrNull() ?: 0
                val seconds = parts[1].toFloatOrNull() ?: 0f
                (minutes * 60 + seconds)
            }
            3 -> {
                val hours = parts[0].toIntOrNull() ?: 0
                val minutes = parts[1].toIntOrNull() ?: 0
                val seconds = parts[2].toFloatOrNull() ?: 0f
                (hours * 3600 + minutes * 60 + seconds)
            }
            else -> 0f
        }
    }

    override fun onPause() {
        super.onPause()
        mediaPlayerHelper.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayerHelper.release()
        _binding = null
    }
}
