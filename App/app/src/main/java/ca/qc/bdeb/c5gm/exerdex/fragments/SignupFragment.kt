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
import ca.qc.bdeb.c5gm.exerdex.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


class SignupFragment : Fragment() {

    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val name: EditText = view.findViewById(R.id.nameSignupId)
        val email: EditText = view.findViewById(R.id.emailSignupId)
        val password: EditText = view.findViewById(R.id.pwdSignUpId)
        val button: Button = view.findViewById(R.id.signUpButtonId)
        val loginLink: TextView = view.findViewById(R.id.loginLinkId)
        val returnRegister: ImageView = view.findViewById(R.id.returnRegisterId)
        val pwdVisibility: ImageView = view.findViewById(R.id.pwdSignupVisibility)

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


        button.setOnClickListener {
            if (name.text.isNullOrBlank() || email.text.isNullOrBlank() || password.text.isNullOrBlank()) {
                Snackbar.make(view, "Veuillez remplir tous les champs", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userEmail = email.text.toString()

            val user = hashMapOf(
                "Name" to name.text.toString(),
                "Email" to email.text.toString(),
                "Password" to password.text.toString(),
                "Country" to "Canada"
            )

            name.text.clear()
            email.text.clear()
            password.text.clear()


            db.collection("users")
                .whereEqualTo("Email", userEmail)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if(!querySnapshot.isEmpty){
                        Snackbar.make(view, R.string.user_existed, Snackbar.LENGTH_SHORT).show()
                    }else{
                        db.collection("users")
                        .add(user)
                            .addOnSuccessListener { documentReference ->
                                Log.d("Success SignUp", "\"DocumentSnapshot added with ID: ${documentReference.id}\"")
                                Snackbar.make(view,  getString(R.string.success_signup) + " ${user.get("Name")}", Snackbar.LENGTH_SHORT).show()
                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.fragmentContainerAuthentication, LoginFragment())
                                    .commit()
                            }
                            .addOnFailureListener{ e ->
                                Log.w("Error SignUp", "Error adding document", e)
                                // Show a failure Snackbar
                                Snackbar.make(view, R.string.failed_signup, Snackbar.LENGTH_LONG).show()
                            }
                    }
                }
                .addOnFailureListener{ e ->
                    Log.e("Error Query", "Erreur lors de la vérification de l'email", e)
                    Snackbar.make(view, R.string.error_verification, Snackbar.LENGTH_LONG).show()
                }

        }

        loginLink.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerAuthentication, LoginFragment())
                .commit()
        }

        returnRegister.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerAuthentication, HomePageFragment())
                .commit()
        }
    }

}