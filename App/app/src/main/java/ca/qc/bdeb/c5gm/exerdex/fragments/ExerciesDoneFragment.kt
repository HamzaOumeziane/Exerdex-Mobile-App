package ca.qc.bdeb.c5gm.exerdex.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.exerdex.R
import ca.qc.bdeb.c5gm.exerdex.adaptors.DoneListAdaptor
import ca.qc.bdeb.c5gm.exerdex.adaptors.ExerciseListAdaptor
import ca.qc.bdeb.c5gm.exerdex.data.Exercise
import ca.qc.bdeb.c5gm.exerdex.room.ExerciseDatabase
import ca.qc.bdeb.c5gm.exerdex.viewmodels.ActiveWorkoutViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExerciesDoneFragment : Fragment() {
    private var currentUserId: String? = null
    private val viewModel: ActiveWorkoutViewModel by activityViewModels()
    private val roomDatabase: ExerciseDatabase by lazy {
        ExerciseDatabase.getExerciseDatabase(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_exercies_done, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentUserId = arguments?.getString("currentUserId")
        Log.d("DoneFragment", "User logged in with ID: $currentUserId")
        if (currentUserId == null) {
            Log.e("DoneFragment", "currentUserId is null!")
            // Handle the error appropriately
            return
        }
        val doneRecyclerView: RecyclerView = view.findViewById(R.id.doneRecyclerView)
        val doneListAdaptor: DoneListAdaptor = DoneListAdaptor(view.context, emptyList<Exercise>().toMutableList()
        ) { item: Exercise -> returnExercise(item)}
        doneRecyclerView.adapter = doneListAdaptor

        initFromDB(doneListAdaptor)

        viewModel.doneExercises.observe(viewLifecycleOwner) { doneListUpdated ->
            doneListAdaptor.doneList = doneListUpdated
            doneListAdaptor.notifyDataSetChanged()
        }
    }

    private fun returnExercise(item: Exercise){
        viewModel.changeExerciseDoneState(item, false)
        lifecycleScope.launch(Dispatchers.IO){
            roomDatabase.exerciseDao().updateAll(item)
        }
    }

    private fun initFromDB(doneListAdaptor: DoneListAdaptor) {
        lifecycleScope.launch(Dispatchers.IO) {
            val doneListFromDB = roomDatabase.exerciseDao().loadExerciseByDone(true, currentUserId ?: "")
            withContext(Dispatchers.Main) {
                doneListAdaptor.doneList = doneListFromDB
                viewModel.doneExercises.value = doneListFromDB
                doneListAdaptor.notifyDataSetChanged()
            }
        }
    }
}