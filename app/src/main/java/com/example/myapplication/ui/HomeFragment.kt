package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.repository.SentenceGeneratorRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuizViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnKanaStart.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_setup)
        }

        binding.btnWordStart.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_word_setup)
        }

        binding.btnSentenceStart.setOnClickListener {
            // 새로운 AI 문장 생성 페이지로 이동
            findNavController().navigate(R.id.action_home_to_sentence_generation)
        }

        binding.btnSongStart.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_song_selection)
        }

        binding.btnDailyWord.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_daily_word)
        }

        binding.btnSettings.setOnClickListener {
            showSettingsDialog()
        }

        binding.btnQuit.setOnClickListener {
            showQuitDialog()
        }
    }

    private fun showSettingsDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_settings, null)
        val penSeekBar = dialogView.findViewById<SeekBar>(R.id.pen_width_seekbar)
        val penWidthTextView = dialogView.findViewById<TextView>(R.id.pen_width_value)
        val eraserSeekBar = dialogView.findViewById<SeekBar>(R.id.eraser_width_seekbar)
        val eraserWidthTextView = dialogView.findViewById<TextView>(R.id.eraser_width_value)
        val usageStatsTextView = dialogView.findViewById<TextView>(R.id.tv_usage_stats)

        val sentenceRepo = SentenceGeneratorRepository(requireContext())

        penSeekBar.progress = viewModel.penWidth.value?.toInt() ?: 12
        penWidthTextView.text = penSeekBar.progress.toString()
        eraserSeekBar.progress = viewModel.eraserWidth.value?.toInt() ?: 40
        eraserWidthTextView.text = eraserSeekBar.progress.toString()

        // Load usage statistics
        val totalRequests = sentenceRepo.getTotalRequests()
        val totalTokens = sentenceRepo.getTotalTokens()
        val totalCost = sentenceRepo.getTotalCost()
        usageStatsTextView.text = "총 요청: ${totalRequests}회\n총 토큰: ${totalTokens}개\n총 비용: $${String.format("%.4f", totalCost)}"

        penSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                penWidthTextView.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        eraserSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                eraserWidthTextView.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("설정")
            .setView(dialogView)
            .setPositiveButton("저장") { _, _ ->
                viewModel.savePenWidth(penSeekBar.progress.toFloat())
                viewModel.saveEraserWidth(eraserSeekBar.progress.toFloat())
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showQuitDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("종료")
            .setMessage("앱을 종료하시겠습니까?")
            .setNegativeButton("아니요", null)
            .setPositiveButton("예") { _, _ ->
                requireActivity().finish()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
