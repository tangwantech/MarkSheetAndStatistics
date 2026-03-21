package com.example.testscoreandstatistics

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.testscoreandstatistics.databinding.ItemStudentScoreBinding
import com.example.testscoreandstatistics.datamodels.StudentData

class StudentScoreAdapter(private val onItemClicked: (Int) -> Unit) : 
    ListAdapter<StudentData, StudentScoreAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStudentScoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onItemClicked)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemStudentScoreBinding,
        private val onItemClicked: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(student: StudentData) {
            binding.studentName.text = student.studentName
            binding.studentRegId.text = student.studentRegID
            binding.studentGender.text = student.gender
            binding.studentScore.text = student.score.toString()
            
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClicked(position)
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
