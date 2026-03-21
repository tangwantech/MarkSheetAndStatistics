package com.example.testscoreandstatistics

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.testscoreandstatistics.databinding.ItemStudentScoreBinding
import com.example.testscoreandstatistics.datamodels.StudentData

class StudentScoreAdapter(
    private val onItemClicked: (Int) -> Unit,
    private val onRegistrationChanged: (Int, Boolean) -> Unit
) : ListAdapter<StudentData, StudentScoreAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStudentScoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onItemClicked, onRegistrationChanged)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemStudentScoreBinding,
        private val onItemClicked: (Int) -> Unit,
        private val onRegistrationChanged: (Int, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(student: StudentData) {
            binding.studentName.text = student.studentName
            binding.studentRegId.text = student.studentRegID
            binding.studentGender.text = student.gender
            binding.studentScore.text = student.score.toString()
            
            // Set score background color based on value
            val scoreColor = if (student.score >= 10) {
                ContextCompat.getColor(itemView.context, R.color.score_pass)
            } else {
                ContextCompat.getColor(itemView.context, R.color.score_fail)
            }
            binding.scoreContainer.backgroundTintList = ColorStateList.valueOf(scoreColor)
            
            // Unset listener before setting state to avoid triggering callback during binding
            binding.switchRegistered.setOnCheckedChangeListener(null)
            binding.switchRegistered.isChecked = student.isRegistered
            
            // Visual feedback for non-registered students
            binding.scoreContainer.alpha = if (student.isRegistered) 1.0f else 0.4f
            binding.root.alpha = if (student.isRegistered) 1.0f else 0.8f

            binding.switchRegistered.setOnCheckedChangeListener { _, isChecked ->
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onRegistrationChanged(position, isChecked)
                }
            }
            
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    if (student.isRegistered) {
                        onItemClicked(position)
                    } else {
                        // Optionally show a message or just ignore the click
                    }
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<StudentData>() {
        override fun areItemsTheSame(oldItem: StudentData, newItem: StudentData): Boolean {
            return oldItem.studentRegID == newItem.studentRegID
        }

        override fun areContentsTheSame(oldItem: StudentData, newItem: StudentData): Boolean {
            return oldItem == newItem
        }
    }
}
