package ca.qc.bdeb.c5gm.exerdex.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ca.qc.bdeb.c5gm.exerdex.R
import ca.qc.bdeb.c5gm.exerdex.data.Exercise


class ExercisePopUp : Fragment() {

    private lateinit var popupLayer: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_exercise_pop_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        popupLayer = view
        val closePopUpBtn: ImageView = view.findViewById(R.id.closePopupImg)
        closePopUpBtn.setOnClickListener {
            closePopUp()
        }
    }

    fun setUpPopup(exerciseToDisplay: Exercise){
        Log.d("FRAG", "showExercise reached frag Popup ")

        val titleView: TextView = popupLayer.findViewById(R.id.popupExoTitle)
        val typeView: TextView = popupLayer.findViewById(R.id.popupExoType)
        val descView: TextView = popupLayer.findViewById(R.id.popupExoDesc)
        val setsView: TextView = popupLayer.findViewById(R.id.popupExoSets)
        val exoImage: ImageView = popupLayer.findViewById(R.id.popupExoImg)
        titleView.text = exerciseToDisplay.exerciseRawData.name
        if(exerciseToDisplay.exerciseRawData.description.isNullOrEmpty()){
            descView.visibility = View.GONE
        }else{
            descView.text = exerciseToDisplay.exerciseRawData.description
        }

        typeView.text = exerciseToDisplay.exerciseRawData.category.toString() + " Exercise"


        val setsStringBuilder = StringBuilder()

        exerciseToDisplay.setList.forEachIndexed { index, item ->
            setsStringBuilder.append(item.toString())
            // Pour ne pas avoir de new line au dernier element
            if (index < exerciseToDisplay.setList.size - 1) {
                setsStringBuilder.append("\n")
            }
        }
        setsView.text = setsStringBuilder.toString()

        if(!exerciseToDisplay.exerciseRawData.imageUri.isNullOrEmpty()){
            val uri = Uri.parse(exerciseToDisplay.exerciseRawData.imageUri)
            exoImage.setImageURI(uri)
        }
        popupLayer.visibility = View.VISIBLE
        Log.d("FRAG", "showExercise view state: "+popupLayer.visibility)
    }
    fun closePopUp(){
        popupLayer.visibility = View.GONE
        requireActivity().findViewById<View>(R.id.popupFragment).visibility = View.GONE
    }
}