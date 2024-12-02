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

        val name: EditText = view.findViewById(R.id.nameSignUpId)
        val email: EditText = view.findViewById(R.id.emailSignUpId)
        val password: EditText = view.findViewById(R.id.pwdSignUpId)
        val button: Button = view.findViewById(R.id.signUpButtonId)
        val loginLink: TextView = view.findViewById(R.id.loginLinkId)
        val returnRegister: ImageView = view.findViewById(R.id.returnRegisterId)

        button.isEnabled = !(name.text.isNullOrEmpty() || email.text.isNullOrEmpty() || password.text.isNullOrEmpty())

        button.setOnClickListener {
            val user = hashMapOf(
                "Name" to name.text,
                "Email" to email.text,
                "Password" to password
            )

            db.collection("users")
                .add(user)
                .addOnSuccessListener { documentReference ->
                    Log.d("Success SignUp", "\"DocumentSnapshot added with ID: ${documentReference.id}\"")
                    Snackbar.make(view,  R.string.success_signup.toString() + "${user.get("Name")}", Snackbar.LENGTH_SHORT).show()
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