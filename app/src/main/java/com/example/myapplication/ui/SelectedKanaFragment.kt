package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentSelectedKanaBinding
import com.example.myapplication.model.KanaCharacter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectedKanaFragment : Fragment() {

    private var _binding: FragmentSelectedKanaBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuizViewModel by activityViewModels()

    private lateinit var selectedAdapter: SelectedKanaDisplayAdapter
    private lateinit var allKanaAdapter: SelectedKanaAdapter
    private var currentKanaType = "hiragana"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectedKanaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTabs()
        setupRecyclerViews()
        setupButtons()
        observeViewModel()
    }

    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("히라가나"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("카타카나"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        currentKanaType = "hiragana"
                        updateAllKanaList()
                    }
                    1 -> {
                        currentKanaType = "katakana"
                        updateAllKanaList()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerViews() {
        // Selected kana display (top)
        selectedAdapter = SelectedKanaDisplayAdapter(
            kanaList = viewModel.getSelectedKanaList(),
            onRemove = { kana ->
                viewModel.toggleKanaSelection(kana)
            }
        )
        binding.recyclerSelectedKana.adapter = selectedAdapter

        // All kana selection (bottom)
        allKanaAdapter = SelectedKanaAdapter(
            kanaList = getFilteredKanaList(),
            isKanaSelected = { kana -> viewModel.isKanaSelected(kana) },
            onToggle = { kana ->
                viewModel.toggleKanaSelection(kana)
            }
        )
        binding.recyclerAllKana.adapter = allKanaAdapter
    }

    private fun setupButtons() {
        binding.btnClearAll.setOnClickListener {
            if (viewModel.getSelectedKanaList().isNotEmpty()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("전체 삭제")
                    .setMessage("선택한 모든 가나를 삭제하시겠습니까?")
                    .setPositiveButton("삭제") { _, _ ->
                        viewModel.clearSelectedKana()
                    }
                    .setNegativeButton("취소", null)
                    .show()
            }
        }

        binding.btnStartQuiz.setOnClickListener {
            // Navigate to selected kana writing quiz
            findNavController().navigate(R.id.action_selected_kana_to_selected_kana_quiz)
        }
    }

    private fun observeViewModel() {
        viewModel.kanaViewModel.selectedKanaList.observe(viewLifecycleOwner) { kanaList ->
            selectedAdapter.updateList(kanaList)
            allKanaAdapter.updateList(getFilteredKanaList())
            binding.tvSelectedCount.text = "선택된 가나: ${kanaList.size}개"
            binding.btnStartQuiz.isEnabled = kanaList.isNotEmpty()
        }
    }

    private fun updateAllKanaList() {
        allKanaAdapter.updateList(getFilteredKanaList())
    }

    private fun getFilteredKanaList(): List<KanaCharacter> {
        val allKana = viewModel.getAllKanaList()
        return when (currentKanaType) {
            "hiragana" -> allKana.filter { it.kana.isNotEmpty() && it.kana[0] in '\u3040'..'\u309F' }
            "katakana" -> allKana.filter { it.kana.isNotEmpty() && it.kana[0] in '\u30A0'..'\u30FF' }
            else -> allKana
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
