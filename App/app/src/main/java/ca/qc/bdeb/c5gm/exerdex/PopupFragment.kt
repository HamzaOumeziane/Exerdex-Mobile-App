package ca.qc.bdeb.c5gm.exerdex

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ca.qc.bdeb.c5gm.exerdex.data.Exercise


class PopupFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_popup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val exercise = arguments?.getParcelable<Exercise>("exercise")
        exercise?.let { setUpPopup(it, view) }
        val closePopupButton = view.findViewById<ImageView>(R.id.closePopupImg)
        closePopupButton.setOnClickListener { dismiss() }
}

    private fun setUpPopup(exerciseToDisplay: Exercise, view: View) {
        val titleView: TextView = view.findViewById(R.id.popupExoTitle)
        val typeView: TextView = view.findViewById(R.id.popupExoType)
        val descView: TextView = view.findViewById(R.id.popupExoDesc)
        val setsView: TextView = view.findViewById(R.id.popupExoSets)
        val exoImage: ImageView = view.findViewById(R.id.popupExoImg)
        titleView.text = exerciseToDisplay.name
        if (exerciseToDisplay.description.isNullOrEmpty()) {
            descView.visibility = View.GONE
        } else {
            descView.text = exerciseToDisplay.description
        }
        typeView.text = exerciseToDisplay.category.toString() + " Exercise"
        val setsStringBuilder = StringBuilder()
        exerciseToDisplay.setList.forEachIndexed { index, item -> setsStringBuilder.append(item.toString())
            if (index < exerciseToDisplay.setList.size - 1) {
                setsStringBuilder.append("\n")
            }
        }
        setsView.text = setsStringBuilder.toString()
        if (!exerciseToDisplay.imageUri.isNullOrEmpty()) {
            val uri = Uri.parse(exerciseToDisplay.imageUri)
            exoImage.setImageURI(uri)
        }
    }
    }
