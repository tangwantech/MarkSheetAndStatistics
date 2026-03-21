package com.example.testscoreandstatistics

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.testscoreandstatistics.databinding.DialogEditScoreBinding
import com.example.testscoreandstatistics.datamodels.StudentData

class EditScoreDialogFragment : DialogFragment() {

    private var _binding: DialogEditScoreBinding? = null
    private val binding get() = _binding!!
    private var currentIndex: Int = -1
    private var students: List<StudentData> = emptyList()
    private var listener: OnScoreUpdatedListener? = null

    interface OnScoreUpdatedListener {
        fun onScoreUpdated(index: Int, newScore: Double)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogEditScoreBinding.inflate(layoutInflater)
        
        currentIndex = arguments?.getInt(ARG_INDEX) ?: -1
        students = (parentFragment as? TestScoreFragment)?.getCurrentStudents() ?: emptyList()
        listener = parentFragment as? OnScoreUpdatedListener

        updateUI()

        binding.btnPrevious.setOnClickListener {
            if (saveCurrentScore()) {
                if (currentIndex > 0) {
                    currentIndex--
                    updateUI()
                }
            }
        }

        binding.btnNext.setOnClickListener {
            if (saveCurrentScore()) {
                if (currentIndex < students.size - 1) {
                    currentIndex++
                    updateUI()
                }
            }
        }

        binding.btnDone.setOnClickListener {
            if (saveCurrentScore()) {
                dismiss()
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun updateUI() {
        if (currentIndex in students.indices) {
            val student = students[currentIndex]
            binding.studentName.text = student.studentName
            binding.scoreInput.setText(student.score.toString())
            binding.scoreLayout.error = null
            
            binding.btnPrevious.isEnabled = currentIndex > 0
            binding.btnNext.isEnabled = currentIndex < students.size - 1
            
            // Focus and select all text for easier editing
            binding.scoreInput.requestFocus()
            binding.scoreInput.selectAll()
        }
    }

    private fun saveCurrentScore(): Boolean {
        val scoreText = binding.scoreInput.text.toString()
        if (scoreText.isEmpty()) {
            binding.scoreLayout.error = "Score cannot be empty"
            return false
        }
        
        val newScore = scoreText.toDoubleOrNull()
        if (newScore == null || newScore < 0 || newScore > 20) {
            binding.scoreLayout.error = getString(R.string.error_invalid_score)
            return false
        }
        
        binding.scoreLayout.error = null
        listener?.onScoreUpdated(currentIndex, newScore)
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "EditScoreDialogFragment"
        private const val ARG_INDEX = "arg_index"

        fun newInstance(index: Int): EditScoreDialogFragment {
            return EditScoreDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_INDEX, index)
                }
            }
        }
    }
}
