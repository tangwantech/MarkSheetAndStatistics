package com.example.testscoreandstatistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.testscoreandstatistics.databinding.FragmentStatisticsBinding
import com.example.testscoreandstatistics.databinding.ItemStatisticsCardBinding
import com.example.testscoreandstatistics.datamodels.SequenceStats
import com.example.testscoreandstatistics.datamodels.StatisticsResponse
import com.example.testscoreandstatistics.repositories.StatisticsRepository
import com.example.testscoreandstatistics.repositories.UserRepository
import com.example.testscoreandstatistics.viewmodels.MainActivityViewModel
import com.google.gson.Gson
import java.util.Locale

class StatisticsFragment : Fragment(), StatisticsSelectionDialogFragment.OKButtonClick {

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
        setupSwipeRefresh()
        setupObservers()
        setupListeners()

        if (!isHidden && viewModel.statistics.value == null) {
            showSelectionDialog()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && viewModel.statistics.value == null) {
            showSelectionDialog()
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary)
    }

    private fun refreshData() {
        val params = viewModel.statisticsSelectionParams.value
        if (params != null) {
            viewModel.fetchStatistics(params, object : StatisticsRepository.GetStatisticsListener {
                override fun onSuccess(result: StatisticsResponse) {
                    activity?.runOnUiThread {
                        binding.swipeRefreshLayout.isRefreshing = false
                    }
                }

                override fun onError(error: String?) {
                    activity?.runOnUiThread {
                        binding.swipeRefreshLayout.isRefreshing = false
                        Toast.makeText(requireContext(), error ?: "Error refreshing statistics", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        } else {
            binding.swipeRefreshLayout.isRefreshing = false
            showSelectionDialog()
        }
    }

    private fun setupObservers() {
        viewModel.statistics.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                updateStatisticsUI(response)
                binding.statisticsScrollView.post {
                    binding.statisticsScrollView.fullScroll(View.FOCUS_UP)
                }
            }
        }

        viewModel.statisticsSelectionParams.observe(viewLifecycleOwner) { params ->
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
        binding.headerTeacher.text = UserRepository.getUserFullName()
        binding.headerClass.text = params["mainClass"] ?: ""
        binding.headerSubject.text = params["subject"] ?: ""
        binding.headerSequence.text = params["sequence"] ?: ""
    }

    private fun updateStatisticsUI(response: StatisticsResponse) {
        binding.statisticsContainer.removeAllViews()
        val params = viewModel.statisticsSelectionParams.value ?: return
        val mainClassName = params["mainClass"] ?: return
        val subjectName = params["subject"] ?: return
        val sequenceName = params["sequence"] ?: return

        val mainClassData = response.statistics[mainClassName] ?: return

        // 1. Display Overall Statistics for Main Class
        mainClassData.overallStatistics?.get(subjectName)?.get(sequenceName)?.let { stats ->
            addStatisticsCard("$mainClassName Overall", null, stats)
        }

        // 2. Display Subclass Specific Statistics
        mainClassData.subclasses?.forEach { (subclassName, subjectMap) ->
            subjectMap[subjectName]?.let { subclassSubjectData ->
                val teachers = if (subclassSubjectData.containsKey("teachers")) {
                    subclassSubjectData["teachers"]?.asString
                } else null
                
                val sequenceJson = subclassSubjectData[sequenceName]
                
                if (sequenceJson != null && sequenceJson.isJsonObject) {
                    try {
                        val stats = Gson().fromJson(sequenceJson, SequenceStats::class.java)
                        addStatisticsCard(subclassName, teachers, stats)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        binding.emptyView.visibility = if (binding.statisticsContainer.childCount == 0) View.VISIBLE else View.GONE
    }

    private fun addStatisticsCard(title: String, teachers: String?, stats: SequenceStats) {
        val cardBinding = ItemStatisticsCardBinding.inflate(layoutInflater, binding.statisticsContainer, false)
        
        cardBinding.cardTitle.text = title
        
        if (!teachers.isNullOrEmpty()) {
            cardBinding.teachersText.visibility = View.VISIBLE
            cardBinding.teachersText.text = getString(R.string.teachers_format, teachers)
        } else {
            cardBinding.teachersText.visibility = View.GONE
        }
        
        // Male Stats
        cardBinding.maleSat.text = stats.males.numSat.toString()
        cardBinding.malePassed.text = stats.males.numPassed.toString()
        cardBinding.malePercent.text = String.format(Locale.getDefault(), "%.1f%%", stats.males.percentagePassed)
        setPercentageColor(cardBinding.malePercent, stats.males.percentagePassed)

        // Female Stats
        cardBinding.femaleSat.text = stats.females.numSat.toString()
        cardBinding.femalePassed.text = stats.females.numPassed.toString()
        cardBinding.femalePercent.text = String.format(Locale.getDefault(), "%.1f%%", stats.females.percentagePassed)
        setPercentageColor(cardBinding.femalePercent, stats.females.percentagePassed)

        // Overall Stats
        cardBinding.totalSat.text = stats.overall.numSat.toString()
        cardBinding.totalPassed.text = stats.overall.numPassed.toString()
        cardBinding.totalPercent.text = String.format(Locale.getDefault(), "%.1f%%", stats.overall.percentagePassed)
        setPercentageColor(cardBinding.totalPercent, stats.overall.percentagePassed)

        binding.statisticsContainer.addView(cardBinding.root)
    }

    private fun setPercentageColor(textView: TextView, percentage: Double) {
        val colorRes = if (percentage >= 50.0) R.color.score_pass else R.color.score_fail
        textView.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
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
