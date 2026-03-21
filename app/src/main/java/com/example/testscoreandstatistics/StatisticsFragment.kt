package com.example.testscoreandstatistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.testscoreandstatistics.databinding.FragmentStatisticsBinding
import com.example.testscoreandstatistics.databinding.ItemStatisticsCardBinding
import com.example.testscoreandstatistics.datamodels.StatisticsResponse
import com.example.testscoreandstatistics.repositories.StatisticsRepository
import com.example.testscoreandstatistics.viewmodels.MainActivityViewModel
import java.util.Locale

class StatisticsFragment : Fragment(), MarksheetSelectionDialogFragment.OKButtonClick {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainActivityViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupListeners()

        if (viewModel.statistics.value == null) {
            showSelectionDialog()
        }
    }

    private fun setupObservers() {
        viewModel.statistics.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                updateStatisticsUI(response)
            }
        }

        viewModel.selectionParams.observe(viewLifecycleOwner) { params ->
            if (params != null) {
                updateHeader(params)
            }
        }
    }

    private fun setupListeners() {
        binding.btnChangeSelection.setOnClickListener {
            showSelectionDialog()
        }
    }

    private fun updateHeader(params: HashMap<String, String>) {
        binding.headerClass.text = params["mainClass"] ?: ""
        binding.headerSubject.text = params["subject"] ?: ""
    }

    private fun updateStatisticsUI(response: StatisticsResponse) {
        binding.statisticsContainer.removeAllViews()
        val params = viewModel.selectionParams.value ?: return
        val mainClassName = params["mainClass"] ?: return
        val subjectName = params["subject"] ?: return
        val sequenceName = params["sequence"] ?: return

        val mainClassData = response.statistics[mainClassName] ?: return

        // 1. Display Overall Statistics for Main Class
        mainClassData.overallStatistics[subjectName]?.get(sequenceName)?.let { stats ->
            addStatisticsCard("$mainClassName Overall", stats)
        }

        // 2. Display Subclass Specific Statistics
        mainClassData.subclasses.forEach { (subclassName, subjectMap) ->
            subjectMap[subjectName]?.get(sequenceName)?.let { stats ->
                addStatisticsCard(subclassName, stats)
            }
        }

        binding.emptyView.visibility = if (binding.statisticsContainer.childCount == 0) View.VISIBLE else View.GONE
    }

    private fun addStatisticsCard(title: String, stats: com.example.testscoreandstatistics.datamodels.SequenceStats) {
        val cardBinding = ItemStatisticsCardBinding.inflate(layoutInflater, binding.statisticsContainer, false)
        
        cardBinding.cardTitle.text = title
        
        // Male Stats
        cardBinding.maleSat.text = stats.males.numSat.toString()
        cardBinding.malePassed.text = stats.males.numPassed.toString()
        cardBinding.malePercent.text = String.format(Locale.getDefault(), "%.1f%%", stats.males.percentagePassed)

        // Female Stats
        cardBinding.femaleSat.text = stats.females.numSat.toString()
        cardBinding.femalePassed.text = stats.females.numPassed.toString()
        cardBinding.femalePercent.text = String.format(Locale.getDefault(), "%.1f%%", stats.females.percentagePassed)

        // Overall Stats
        cardBinding.totalSat.text = stats.overall.numSat.toString()
        cardBinding.totalPassed.text = stats.overall.numPassed.toString()
        cardBinding.totalPercent.text = String.format(Locale.getDefault(), "%.1f%%", stats.overall.percentagePassed)

        binding.statisticsContainer.addView(cardBinding.root)
    }

    private fun showSelectionDialog() {
        if (childFragmentManager.findFragmentByTag(StatisticsSelectionDialogFragment.TAG) == null) {
            StatisticsSelectionDialogFragment().show(childFragmentManager, StatisticsSelectionDialogFragment.TAG)
        }
    }

    override fun onOkButtonClick(params: HashMap<String, String>) {
        (activity as? MainActivity)?.toggleProgress(true)
        viewModel.fetchStatistics(params, object : StatisticsRepository.GetStatisticsListener {
            override fun onSuccess(result: StatisticsResponse) {
                activity?.runOnUiThread {
                    (activity as? MainActivity)?.toggleProgress(false)
                }
            }

            override fun onError(error: String?) {
                activity?.runOnUiThread {
                    (activity as? MainActivity)?.toggleProgress(false)
                    binding.emptyView.visibility = View.VISIBLE
                    binding.emptyView.text = error ?: "Error fetching statistics"
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
