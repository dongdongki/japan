package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentSetupBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SetupFragment : Fragment() {

    private var _binding: FragmentSetupBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuizViewModel by activityViewModels()

    private val allRows = listOf("아행", "카행", "사행", "타행", "나행", "하행", "마행", "야행", "라행", "와행")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeViewModel()

        binding.btnViewKanaList.setOnClickListener {
            findNavController().navigate(R.id.action_setup_to_kana_list)
        }

        binding.btnWritingQuiz.setOnClickListener {
            findNavController().navigate(R.id.action_setup_to_kana_writing_test)
        }

        binding.btnSelectedKana.setOnClickListener {
            findNavController().navigate(R.id.action_setup_to_selected_kana)
        }

        binding.btnStartQuiz.setOnClickListener {
            if (isSelectionValid()) {
                viewModel.startKanaQuiz()
                findNavController().navigate(R.id.action_setup_to_quiz)
            } else {
                showValidationError()
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupListeners() {
        binding.radioGroupKanaType.setOnCheckedChangeListener { _, checkedId ->
            val type = when (checkedId) {
                R.id.radio_hiragana -> "hiragana"
                R.id.radio_katakana -> "katakana"
                R.id.radio_both -> "both"
                R.id.radio_mixed -> "mixed"
                else -> "hiragana"
            }
            viewModel.kanaType.value = type
        }

        binding.radioGroupRangeType.setOnCheckedChangeListener { _, checkedId ->
            val range = if (checkedId == R.id.radio_range_all) "all" else "row"
            viewModel.rangeType.value = range
        }
    }

    private fun observeViewModel() {
        viewModel.rangeType.observe(viewLifecycleOwner) {
            binding.layoutRowSelection.visibility = if (it == "row") View.VISIBLE else View.GONE
        }

        viewModel.kanaType.observe(viewLifecycleOwner) {
            setupRecyclerView(allRows)
        }
    }

    private fun setupRecyclerView(rows: List<String>) {
        val adapter = RowSelectionAdapter(rows) { rowName, isSelected ->
            val currentSelection = viewModel.selectedRows.value.orEmpty().toMutableList()
            if (isSelected) {
                currentSelection.add(rowName)
            } else {
                currentSelection.remove(rowName)
            }
            viewModel.selectedRows.value = currentSelection
        }
        binding.recyclerRows.adapter = adapter
    }

    private fun isSelectionValid(): Boolean {
        if (viewModel.rangeType.value == "row" && viewModel.selectedRows.value.orEmpty().isEmpty()) {
            return false
        }
        return true
    }

    private fun showValidationError() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("오류")
            .setMessage("행별 학습을 선택한 경우, 하나 이상의 행을 선택해야 합니다.")
            .setPositiveButton("확인", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}