package ca.qc.bdeb.c5gm.exerdex.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import ca.qc.bdeb.c5gm.exerdex.AddEditExerciseActivity
import ca.qc.bdeb.c5gm.exerdex.MainActivity
import ca.qc.bdeb.c5gm.exerdex.R
import ca.qc.bdeb.c5gm.exerdex.data.Exercise
import ca.qc.bdeb.c5gm.exerdex.data.Workout
import ca.qc.bdeb.c5gm.exerdex.room.ExerciseDatabase
import ca.qc.bdeb.c5gm.exerdex.viewmodels.ActiveWorkoutViewModel
import ca.qc.bdeb.c5gm.exerdex.viewmodels.SharedViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date

class ActiveWorkoutManagementFragment : Fragment() {

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val viewModel: ActiveWorkoutViewModel by activityViewModels()
    private val roomDatabase: ExerciseDatabase by lazy {
        ExerciseDatabase.getExerciseDatabase(requireContext())
    }
    private lateinit var newWorkoutName: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_active_workout_management, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        newWorkoutName = view.findViewById(R.id.newWorkoutName)
        val addExoBtn: FloatingActionButton = view.findViewById(R.id.floatingActionBtn)
        val finalizeWorkoutBtn: Button = view.findViewById(R.id.archiveButton)

        sharedViewModel.currentUserId.observe(viewLifecycleOwner) { userId ->
            Log.d("AWMFragment", "User logged in with ID: $userId")
            if (userId == null) {
                Log.e("AWMFragment", "currentUserId is null!")
                // Handle the error appropriately
                return@observe
            }

            addExoBtn.setOnClickListener {
                addExercise()
            }

            finalizeWorkoutBtn.setOnClickListener {
                finalizeCurrentWorkout(userId)
            }
        }
    }
    private fun addExercise() {
        val mainActivity = activity as? MainActivity
        mainActivity?.showAddExerciseDialog()
    }
    private fun finalizeCurrentWorkout(userId: String){
        if (viewModel.doneExercises.value!!.isEmpty() || newWorkoutName.text.toString().length < 2){
                Toast.makeText(requireContext(),getString(R.string.toast_new_workout_missing_info_error),Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch(Dispatchers.IO){
                    archiveWorkout(userId)
                    roomDatabase.exerciseDao().deleteAllExercisesDone(userId)
                }
            }
    }

    private suspend fun archiveWorkout(userId: String){
        val setList: MutableList<String> = mutableListOf()
        var totalWorkoutVolume: Int = 0
        viewModel.doneExercises.value!!.forEach { exercise: Exercise ->
            Log.d("exerciseAddingLogs","Adding Exercise: ${exercise}")
            val setsStringBuilder = StringBuilder()
            setsStringBuilder.append(exercise.exerciseRawData.name)
            val heaviestSet = exercise.setList.maxBy { it.weight }
            setsStringBuilder.append(", ${getString(R.string.heaviest_set)}: ${heaviestSet.weight}x${heaviestSet.reps}")
            setList.add(setsStringBuilder.toString())
            val totalVolume = exercise.setList.sumOf { (it.weight * it.reps).toInt() }
            totalWorkoutVolume += totalVolume
        }

        val workout: Workout = Workout(newWorkoutName.text.toString(), Date(System.currentTimeMillis()),
            setList, viewModel.doneExercises.value!!.toList()
            ,totalWorkoutVolume, userId)

        Log.d("exerciseAddingLogs","Workout Added: ${workout}")
        roomDatabase.workoutDao().insertAll(workout)
        withContext(Dispatchers.Main) {
            newWorkoutName.text = ""
            viewModel.doneExercises.value = mutableListOf()
        }
    }
}