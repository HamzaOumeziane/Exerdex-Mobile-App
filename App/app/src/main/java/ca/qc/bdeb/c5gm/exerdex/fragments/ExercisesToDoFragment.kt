package ca.qc.bdeb.c5gm.exerdex.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.qc.bdeb.c5gm.exerdex.R
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.exerdex.MainActivity
import ca.qc.bdeb.c5gm.exerdex.adaptors.ExerciseListAdaptor
import ca.qc.bdeb.c5gm.exerdex.data.Exercise
import ca.qc.bdeb.c5gm.exerdex.room.ExerciseDatabase
import ca.qc.bdeb.c5gm.exerdex.viewmodels.ActiveWorkoutViewModel
import ca.qc.bdeb.c5gm.exerdex.viewmodels.SharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExercisesToDoFragment : Fragment() {
    private val sharedViewModel: SharedViewModel by activityViewModels()
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
        sharedViewModel.currentUserId.observe(viewLifecycleOwner) { userId ->
            Log.d("ExercisesToDoFragment", "User logged in with ID: $userId")
            if (userId == null) {
                Log.e("ExercisesToDoFragment", "currentUserId is null!")
                return@observe
            }

            val toDoRecyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
            val exerciseListAdaptor = ExerciseListAdaptor(
                view.context,
                emptyList<Exercise>().toMutableList(),
                { item: Exercise -> finishExercise(item) },
                { item: Exercise -> deleteExercise(item) },
                { item: Exercise -> editExercise(item) },
                { item: Exercise -> showExercise(item) },
            )
            toDoRecyclerView.adapter = exerciseListAdaptor

            initFromDB(exerciseListAdaptor, userId)

            viewModel.toDoExercises.observe(viewLifecycleOwner) { toDoExercisesUpdated ->
                exerciseListAdaptor.exercisesList = toDoExercisesUpdated
                exerciseListAdaptor.notifyDataSetChanged()
            }
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
    private fun editExercise(item: Exercise){
        val mainActivity = activity as? MainActivity
        mainActivity?.addExercise(true, item, item.exerciseRawData)
    }
    private fun showExercise(item: Exercise){
        Log.d("FRAG", "showExercise clicked from ToDO ")
        val popupFragment = parentFragmentManager.findFragmentById(R.id.popupFragment) as ExercisePopUp
        popupFragment.setUpPopup(item)
        parentFragmentManager.beginTransaction()
            .show(popupFragment)
            .commit()
        requireActivity().findViewById<View>(R.id.popupFragment).visibility = View.VISIBLE
    }

    private fun initFromDB(exerciseListAdaptor: ExerciseListAdaptor, userId: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            Log.d("InitFromDB", "CurrentUser: ${userId}")
            val exercisesToDoFromDB = roomDatabase.exerciseDao().loadExerciseByDone(false, userId)
            withContext(Dispatchers.Main) {
                exerciseListAdaptor.exercisesList = exercisesToDoFromDB
                viewModel.toDoExercises.value = exercisesToDoFromDB
                exerciseListAdaptor.notifyDataSetChanged()
            }
        }
    }
}