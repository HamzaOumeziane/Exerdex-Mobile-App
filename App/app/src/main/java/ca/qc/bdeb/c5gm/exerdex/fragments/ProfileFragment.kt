package ca.qc.bdeb.c5gm.exerdex.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import ca.qc.bdeb.c5gm.exerdex.R
import ca.qc.bdeb.c5gm.exerdex.viewmodels.SharedViewModel


class ProfileFragment : Fragment() {
    var currentUserId: String? = null
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Observer les changements du currentUserId
        sharedViewModel.currentUserId.observe(viewLifecycleOwner, Observer { userId ->
            Log.d("ProfileFragment", "CurrentUserId updated: $userId")

            // Effectuer des actions avec userId, par exemple mettre à jour l'interface utilisateur
        })

    }

}