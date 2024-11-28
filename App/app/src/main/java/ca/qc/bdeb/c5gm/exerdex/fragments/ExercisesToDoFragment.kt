package ca.qc.bdeb.c5gm.exerdex.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.qc.bdeb.c5gm.exerdex.R
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.exerdex.adaptors.ExerciseListAdaptor
import ca.qc.bdeb.c5gm.exerdex.data.Exercise
import ca.qc.bdeb.c5gm.exerdex.room.ExerciseDatabase
import ca.qc.bdeb.c5gm.exerdex.viewmodels.ActiveWorkoutViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExercisesToDoFragment : Fragment() {

    private val viewModel: ActiveWorkoutViewModel by activityViewModels()
    private val roomDatabase: ExerciseDatabase by lazy {
        ExerciseDatabase.getExerciseDatabase(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_exercises_to_do, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toDoRecyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        val exerciseListAdaptor = ExerciseListAdaptor(
            view.context,
            emptyList<Exercise>().toMutableList(),
            { item: Exercise -> finishExercise(item) },
            { item: Exercise -> deleteExercise(item) }
        )
        toDoRecyclerView.adapter = exerciseListAdaptor

        initFromDB(exerciseListAdaptor)

        viewModel.toDoExercises.observe(viewLifecycleOwner) { toDoExercisesUpdated ->
            exerciseListAdaptor.exercisesList = toDoExercisesUpdated
            exerciseListAdaptor.notifyDataSetChanged()
        }
    }

    private fun finishExercise(item: Exercise){
        viewModel.changeExerciseDoneState(item, true)
        lifecycleScope.launch(Dispatchers.IO){
            roomDatabase.exerciseDao().updateAll(item)
        }
    }
    private fun deleteExercise(item: Exercise){
        viewModel.deleteExercise(item)
        lifecycleScope.launch(Dispatchers.IO){
            roomDatabase.exerciseDao().delete(item)
        }
    }

    private fun initFromDB(exerciseListAdaptor: ExerciseListAdaptor) {
        lifecycleScope.launch(Dispatchers.IO) {
            val exercisesToDoFromDB = roomDatabase.exerciseDao().loadExerciseByDone(false)
            withContext(Dispatchers.Main) {
                exerciseListAdaptor.exercisesList = exercisesToDoFromDB
                exerciseListAdaptor.notifyDataSetChanged()
            }
        }
    }
}