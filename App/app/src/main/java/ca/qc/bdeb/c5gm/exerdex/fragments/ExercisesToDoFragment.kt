package ca.qc.bdeb.c5gm.exerdex.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.qc.bdeb.c5gm.exerdex.R
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.exerdex.adaptors.ExerciseListAdaptor
import ca.qc.bdeb.c5gm.exerdex.viewmodels.ActiveWorkoutViewModel

class ExercisesToDoFragment : Fragment() {

    private val viewModel: ActiveWorkoutViewModel by activityViewModels()

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
        val exerciseListAdaptor = ExerciseListAdaptor(view.context, viewModel.toDoExercises)
    }
}