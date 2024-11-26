package ca.qc.bdeb.c5gm.exerdex.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.exerdex.MainActivity
import ca.qc.bdeb.c5gm.exerdex.R
import ca.qc.bdeb.c5gm.exerdex.adaptors.ExerciseListAdaptor
import ca.qc.bdeb.c5gm.exerdex.viewmodels.ActiveWorkoutViewModel
import ca.qc.bdeb.c5gm.exerdex.viewmodels.ExerciseViewModel

class ExerciesDoneFragment : Fragment() {

    private lateinit var viewModel: ExerciseViewModel
    private lateinit var adapter: ExerciseListAdaptor

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_exercies_done, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(ExerciseViewModel::class.java)
        val recyclerView = view.findViewById<RecyclerView>(R.id.doneRecyclerView)
        adapter = ExerciseListAdaptor(requireContext(), requireActivity() as MainActivity, viewModel)
        recyclerView.adapter = adapter
        viewModel.exercisesToDo.observe(viewLifecycleOwner, Observer { it?.let { adapter.submitList(it) } })
    }
}