package ca.qc.bdeb.c5gm.exerdex.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import ca.qc.bdeb.c5gm.exerdex.MainActivity
import ca.qc.bdeb.c5gm.exerdex.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class LoginFragment : Fragment() {

    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonLogin: Button = view.findViewById(R.id.loginButton)
        val email: EditText = view.findViewById(R.id.emailLoginId)
        val password: EditText = view.findViewById(R.id.passwordLoginId)
        val returnImage: ImageView = view.findViewById(R.id.loginReturnHome)
        val linkSignup: TextView = view.findViewById(R.id.SignupLinkId)
        val pwdVisibility: ImageView = view.findViewById(R.id.pwdLoginVisbility)

        var isVisible = false

        pwdVisibility.setOnClickListener {
            isVisible = !isVisible
            if(isVisible){
                password.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                pwdVisibility.setImageResource(R.drawable.baseline_visibility_24)
            }else{
                password.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                pwdVisibility.setImageResource(R.drawable.baseline_visibility_off_24)
            }
        }


        buttonLogin.setOnClickListener {
            if (email.text.isNullOrBlank() || password.text.isNullOrBlank()) {
                Snackbar.make(view, getString(R.string.cases_empty), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userEmail = email.text.toString()
            val userPassword = password.text.toString()

            email.text.clear()
            password.text.clear()

            db.collection("users")
                .whereEqualTo("Email", userEmail)
                .whereEqualTo("Password", userPassword)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        Snackbar.make(view, "Connexion réussie ! Bienvenue ${querySnapshot.documents[0]["Name"]}", Snackbar.LENGTH_SHORT).show()

                        (activity as? MainActivity)?.onLoginSuccessful()

                    } else {
                        Snackbar.make(view, "Email ou mot de passe incorrect", Snackbar.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Error Login", "Erreur lors de la connexion", e)
                    Snackbar.make(view, "Une erreur est survenue. Veuillez réessayer.", Snackbar.LENGTH_LONG).show()
                }
        }

        returnImage.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerAuthentication, HomePageFragment())
                .commit()
        }

        linkSignup.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerAuthentication, SignupFragment())
                .commit()
        }
    }




}