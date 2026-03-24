package com.example.testscoreandstatistics

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.testscoreandstatistics.databinding.DialogStatisticsSelectionBinding
import com.example.testscoreandstatistics.repositories.UserRepository
import com.example.testscoreandstatistics.viewmodels.MainActivityViewModel

class StatisticsSelectionDialogFragment : DialogFragment() {

    private var _binding: DialogStatisticsSelectionBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainActivityViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogStatisticsSelectionBinding.inflate(requireActivity().layoutInflater)
        viewModel = ViewModelProvider(requireActivity())[MainActivityViewModel::class.java]

        setupInitialDropdowns()
        restoreCachedSelections()

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.statistics_selection_title)
            .setView(binding.root)
            .setPositiveButton(R.string.ok) { _, _ ->
                handleOkClick()
            }
            .setNegativeButton(R.string.cancel, null)
            .setCancelable(true) // Changed to true to allow independent navigation
            .create()

        setupListeners(dialog)

        return dialog
    }

    private fun handleOkClick() {
        val sessionToken = UserRepository.getSessionToken() ?: ""
        
        val params = LinkedHashMap<String, String>()
        params["sessionToken"] = sessionToken
        params["mainClass"] = binding.mainClassDropdown.text.toString()
        params["subject"] = binding.subjectDropdown.text.toString()
        params["sequence"] = binding.sequenceDropdown.text.toString()

        val listener = parentFragment as? OKButtonClick
        listener?.onOkButtonClick(params)
    }

    private fun setupInitialDropdowns() {
        val subjects = viewModel.getSubjectsTaught()
        binding.subjectDropdown.setAdapter(createAdapter(subjects))

        val sequences = resources.getStringArray(R.array.sequences).toList()
        binding.sequenceDropdown.setAdapter(createAdapter(sequences))

        binding.mainClassLayout.isEnabled = false
    }

    private fun restoreCachedSelections() {
        val params = viewModel.statisticsSelectionParams.value ?: return
        
        val subject = params["subject"] ?: ""
        if (subject.isNotEmpty()) {
            binding.subjectDropdown.setText(subject, false)
            
            val mainClasses = viewModel.getMainClassesForSubject(subject)
            binding.mainClassDropdown.setAdapter(createAdapter(mainClasses))
            binding.mainClassLayout.isEnabled = true
            
            val mainClass = params["mainClass"] ?: ""
            if (mainClass.isNotEmpty()) {
                binding.mainClassDropdown.setText(mainClass, false)
            }
        }
        
        val sequence = params["sequence"] ?: ""
        if (sequence.isNotEmpty()) {
            binding.sequenceDropdown.setText(sequence, false)
        }
    }

    private fun setupListeners(dialog: AlertDialog) {
        binding.subjectDropdown.setOnItemClickListener { _, _, _, _ ->
            val selectedSubject = binding.subjectDropdown.text.toString()
            val mainClasses = viewModel.getMainClassesForSubject(selectedSubject)
            
            binding.mainClassDropdown.text.clear()
            binding.mainClassDropdown.setAdapter(createAdapter(mainClasses))
            
            binding.mainClassLayout.isEnabled = true
            updateOkButtonState(dialog)
        }

        binding.mainClassDropdown.setOnItemClickListener { _, _, _, _ ->
            updateOkButtonState(dialog)
        }

        binding.sequenceDropdown.setOnItemClickListener { _, _, _, _ ->
            updateOkButtonState(dialog)
        }
    }

    private fun createAdapter(items: List<String>): ArrayAdapter<String> {
        return ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, items)
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? AlertDialog
        updateOkButtonState(dialog)
    }

    private fun updateOkButtonState(dialog: AlertDialog?) {
        val isAllSelected = binding.subjectDropdown.text.isNotEmpty() &&
                binding.mainClassDropdown.text.isNotEmpty() &&
                binding.sequenceDropdown.text.isNotEmpty()
        
        dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = isAllSelected
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface OKButtonClick {
        fun onOkButtonClick(params: HashMap<String, String>)
    }

    companion object {
        const val TAG = "StatisticsSelectionDialogFragment"
    }
}
