package ca.qc.bdeb.c5gm.exerdex.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import ca.qc.bdeb.c5gm.exerdex.R
import ca.qc.bdeb.c5gm.exerdex.viewmodels.ActiveWorkoutViewModel

class ActiveWorkoutManagementFragment : Fragment() {

    private val viewModel: ActiveWorkoutViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_active_workout_management, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}