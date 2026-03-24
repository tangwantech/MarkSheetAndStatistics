package com.example.testscoreandstatistics

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testscoreandstatistics.databinding.FragmentTestScoreBinding
import com.example.testscoreandstatistics.datamodels.StudentData
import com.example.testscoreandstatistics.repositories.StudentsRepository
import com.example.testscoreandstatistics.repositories.UserRepository
import com.example.testscoreandstatistics.viewmodels.MainActivityViewModel

class TestScoreFragment : Fragment(), MarksheetSelectionDialogFragment.OKButtonClick, EditScoreDialogFragment.OnStudentUpdateListener {
    
    private var _binding: FragmentTestScoreBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var adapter: StudentScoreAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestScoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSwipeRefresh()
        setupObservers()
        setupListeners()
        
        if (!isHidden && viewModel.students.value == null) {
            showSelectionDialog()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && viewModel.students.value == null) {
            showSelectionDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = StudentScoreAdapter(
            onItemClicked = { position ->
                if (childFragmentManager.findFragmentByTag(EditScoreDialogFragment.TAG) == null) {
                    EditScoreDialogFragment.newInstance(position).show(childFragmentManager, EditScoreDialogFragment.TAG)
                }
            },
            onRegistrationChanged = { position, isRegistered ->
                onStudentRegistrationChanged(position, isRegistered)
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }
        // Customize color
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary)
    }

    private fun refreshData() {
        val params = viewModel.marksheetSelectionParams.value
        if (params != null) {
            viewModel.fetchStudents(params, object : StudentsRepository.FetchStudentsListener {
                override fun onSuccess(result: List<StudentData>) {
                    activity?.runOnUiThread {
                        binding.swipeRefreshLayout.isRefreshing = false
                    }
                }

                override fun onError(error: String?) {
                    activity?.runOnUiThread {
                        binding.swipeRefreshLayout.isRefreshing = false
                        Toast.makeText(requireContext(), error ?: "Error refreshing data", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        } else {
            binding.swipeRefreshLayout.isRefreshing = false
            showSelectionDialog()
        }
    }

    private fun setupObservers() {
        viewModel.students.observe(viewLifecycleOwner) { students ->
            if (students != null) {
                adapter.submitList(students) {
                    binding.recyclerView.scrollToPosition(0)
                }
                adapter.notifyDataSetChanged()
                binding.emptyView.visibility = if (students.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        viewModel.marksheetSelectionParams.observe(viewLifecycleOwner) { params ->
            if (params != null) {
                updateHeader(params)
            }
        }
    }

    private fun setupListeners() {
        binding.btnChangeSelection.setOnClickListener {
            showSelectionDialog()
        }
        
        binding.btnSaveScores.setOnClickListener {
            saveScores()
        }
    }

    private fun saveScores() {
        (activity as? MainActivity)?.toggleProgress(true)
        viewModel.saveStudents(object : StudentsRepository.SaveStudentsListener {
            override fun onSuccess(result: String) {
                activity?.runOnUiThread {
                    (activity as? MainActivity)?.toggleProgress(false)
                    Toast.makeText(requireContext(), "Scores saved successfully", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(error: String?) {
                activity?.runOnUiThread {
                    (activity as? MainActivity)?.toggleProgress(false)
                    Toast.makeText(requireContext(), "Error saving scores: $error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun showSelectionDialog() {
        if (childFragmentManager.findFragmentByTag(MarksheetSelectionDialogFragment.TAG) == null) {
            MarksheetSelectionDialogFragment().show(childFragmentManager, MarksheetSelectionDialogFragment.TAG)
        }
    }

    override fun onOkButtonClick(params: HashMap<String, String>) {
        (activity as? MainActivity)?.toggleProgress(true)
        viewModel.fetchStudents(params, object : StudentsRepository.FetchStudentsListener {
            override fun onSuccess(result: List<StudentData>) {
                activity?.runOnUiThread {
                    (activity as? MainActivity)?.toggleProgress(false)
                }
            }

            override fun onError(error: String?) {
                activity?.runOnUiThread {
                    (activity as? MainActivity)?.toggleProgress(false)
                    binding.emptyView.visibility = View.VISIBLE
                    binding.emptyView.text = error ?: "Error fetching data"
                }
            }
        })
    }

    private fun updateHeader(params: HashMap<String, String>) {
        val subclass = params["subclass"] ?: ""
        val subject = params["subject"] ?: ""
        val sequence = params["sequence"] ?: ""

        binding.headerTeacher.text = UserRepository.getUserFullName()
        binding.headerClass.text = subclass
        binding.headerSubject.text = subject
        binding.headerSequence.text = sequence
    }

    fun getCurrentStudents(): List<StudentData> {
        return viewModel.students.value ?: emptyList()
    }

    override fun onScoreUpdated(index: Int, newScore: Double) {
        val currentList = viewModel.students.value
        if (currentList != null && index in currentList.indices) {
            currentList[index].score = newScore
            adapter.notifyItemChanged(index)
        }
    }

    override fun onRegistrationUpdated(index: Int, isRegistered: Boolean) {
        onStudentRegistrationChanged(index, isRegistered)
    }

    private fun onStudentRegistrationChanged(index: Int, isRegistered: Boolean) {
        val currentList = viewModel.students.value
        if (currentList != null && index in currentList.indices) {
            currentList[index].isRegistered = isRegistered
            adapter.notifyItemChanged(index)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
