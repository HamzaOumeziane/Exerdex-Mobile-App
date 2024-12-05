package ca.qc.bdeb.c5gm.exerdex.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.exerdex.MainActivity
import ca.qc.bdeb.c5gm.exerdex.R
import ca.qc.bdeb.c5gm.exerdex.adaptors.ProfileAdapter
import ca.qc.bdeb.c5gm.exerdex.data.Exercise
import ca.qc.bdeb.c5gm.exerdex.data.User
import ca.qc.bdeb.c5gm.exerdex.viewmodels.SharedViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


class ProfileFragment : Fragment() {
    var currentUserId: String? = null
    private val sharedViewModel: SharedViewModel by activityViewModels()
    val db = Firebase.firestore
    private lateinit var profileAdapter: ProfileAdapter
    private lateinit var profileName: TextView


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

         profileName= view.findViewById(R.id.nameProfileId)
        val imageProfile: ImageView = view.findViewById(R.id.profileImageView)
        imageProfile.setImageResource(R.drawable.exerdex)
        val returnProfile: ImageView = view.findViewById(R.id.returnProfileId)
        returnProfile.setOnClickListener {
            requireActivity().findViewById<FragmentContainerView>(R.id.AWExercisesToDoFragment).visibility = View.VISIBLE
            requireActivity().findViewById<FragmentContainerView>(R.id.AWExercisesDoneFragment).visibility = View.VISIBLE
            requireActivity().findViewById<FragmentContainerView>(R.id.AWManagementFragment).visibility = View.VISIBLE
            requireActivity().findViewById<Toolbar>(R.id.toolbar).visibility = View.VISIBLE
            requireActivity().findViewById<FragmentContainerView>(R.id.fragmentContainerProfile).visibility = View.GONE
        }


        val recyclerView: RecyclerView = view.findViewById(R.id.profileRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        profileAdapter = ProfileAdapter(
            view.context,
            mutableListOf(),
            onEditClick = { user ->
                handleEditClick(user)
            },
            onLogoutClick = {
                (activity as? MainActivity)?.onLogout()
            },
            onDeleteClick = {
                handleDeleteClick()
            }
        )

        recyclerView.adapter = profileAdapter

        sharedViewModel.currentUserId.observe(viewLifecycleOwner, Observer { userId ->
            Log.d("ProfileFragment", "CurrentUserId updated: $userId")
            loadUserData(userId)
        })

    }

    private fun handleDeleteClick() {
        val userId = sharedViewModel.currentUserId.value
        if(userId == null){
            Log.w("ProfileFragment", "User ID is null, cannot delete account.")
            Toast.makeText(context, "Erreur: ID utilisateur manquant.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(userId).delete()
            .addOnSuccessListener {
                Log.d("ProfileFragment", "User account deleted successfully.")
                (activity as? MainActivity)?.onLogout()
                Toast.makeText(context, "Compte supprimé.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.w("ProfileFragment", "Error deleting user account", e)
                Toast.makeText(context, "Erreur lors de la suppression du compte.", Toast.LENGTH_SHORT).show()
            }

    }


    private fun handleEditClick(user: User) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.edit) + " " + user.title.split(": ")[0])

        // source : https://www.digitalocean.com/community/tutorials/android-alert-dialog-using-kotlin
        val input = android.widget.EditText(requireContext())
        input.hint = getString(R.string.new_edit)
        builder.setView(input)

        builder.setPositiveButton(getString(R.string.edit)) { dialog, which ->
            val newValue = input.text.toString().trim()
            if(newValue.isNotEmpty()){
                updateUser(user.title.split(": ")[0], newValue)
            } else {
                Toast.makeText(context, getString(R.string.value_empty), Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Annuler") { dialog, which ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun updateUser(updated: String, newValue: String){
        val userId = sharedViewModel.currentUserId.value
        if(userId == null){
            Log.w("ProfileFragment", "User ID is null, cannot update user data.")
            Toast.makeText(context, "Erreur: ID utilisateur manquant.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(userId)
            .update(updated, newValue)
            .addOnSuccessListener {
                Log.d("ProfileFragment", "User $updated updated successfully.")
                // Rafraîchir les données
                loadUserData(userId)
                Toast.makeText(context, "$updated mis à jour.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.w("ProfileFragment", "Error updating $updated", e)
                Toast.makeText(context, "Erreur lors de la mise à jour de $updated.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserData(userId: String?) {
        if(userId == null){
            Log.w("ProfileFragment", "User ID is null, cannot load user data.")
            return
        }

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    Log.d("ProfileFragment", "User data: ${document.data}")

                    profileName.text = document["Name"].toString()

                    val userItems = listOf(
                        User(getString(R.string.name) + ": " + document["Name"].toString() ?: "Nom indisponible", R.drawable.baseline_person_outline_24, true),
                        User(getString(R.string.email) + ": "+ document["Email"].toString() ?: "Email indisponible", R.drawable.baseline_mail_outline_24, false),
                        User(getString(R.string.password) + ": " + document["Password"].toString() ?: "PWD indisponible", R.drawable.baseline_lock_outline_24, true),
                        User(getString(R.string.country) + ": " + document["Country"].toString() ?: "Pays indisponible", R.drawable.baseline_outlined_flag_24, true),
                        User(getString(R.string.delete) + ": " +getString(R.string.delete), R.drawable.baseline_delete_outline_24, false),
                        User(getString(R.string.logout) + ": "+ getString(R.string.logout), R.drawable.baseline_logout_24, false)
                    )

                    profileAdapter.updateItems(userItems)

                } else {
                    Log.d("ProfileFragment", "No such document")
                    profileAdapter.updateItems(emptyList())
                }
            }
            .addOnFailureListener { exception ->
                Log.w("ProfileFragment", "Error getting user data", exception)
                profileAdapter.updateItems(emptyList())

            }
    }
    }

