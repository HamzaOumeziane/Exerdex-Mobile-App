package ca.qc.bdeb.c5gm.exerdex

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.exerdex.adaptors.SetListAdaptor
import ca.qc.bdeb.c5gm.exerdex.data.Exercise
import ca.qc.bdeb.c5gm.exerdex.data.ExerciseRaw
import ca.qc.bdeb.c5gm.exerdex.data.MuscleCategory
import ca.qc.bdeb.c5gm.exerdex.data.Set
import ca.qc.bdeb.c5gm.exerdex.viewholders.ItemSetHolder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.util.Date
import java.util.Locale

class AddEditExerciseActivity : AppCompatActivity() {
    lateinit var repsTextView: TextView
    lateinit var weightTextView: TextView
    private lateinit var setsRecyclerView: RecyclerView
    private lateinit var setListAdapter: SetListAdaptor
    private lateinit var exerciseTitleView: TextView
    private lateinit var exerciseDescriptionView: TextView
    private lateinit var exerciseCategoryView: TextView
    private lateinit var exerciseImportantView: ImageView
    private lateinit var exerciseImg: ImageView
    private lateinit var uriPic: Uri
    private lateinit var picTaken: ActivityResultLauncher<Uri>
    private lateinit var picSelected: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var exerciseRaw: ExerciseRaw
    private var pictureSet: Boolean = false
    private var exerciseImportant: Boolean = false
    private var setsList: MutableList<Set> = mutableListOf()
    private var isEditing: Boolean = false
    private var exerciseBeingEditedId: Int? = null
    private var currentUserId: String? = null




    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_edit_exercise)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_keyboard_return_24_wh)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        currentUserId = intent.getStringExtra("currentUserId")
        Log.d("AddEditExerciseActivity", "User logged in with ID: $currentUserId")

        exerciseImg = findViewById(R.id.exerciseImg)

        repsTextView = findViewById(R.id.newSetReps)
        weightTextView = findViewById(R.id.newSetWeight)
        exerciseTitleView = findViewById(R.id.exerciseNameDecl)
        exerciseDescriptionView = findViewById(R.id.exerciseDescDecl)
        setsRecyclerView = findViewById(R.id.setsRecyclerView)
        exerciseCategoryView = findViewById(R.id.exerciseCategoryDecl)
        setListAdapter = SetListAdaptor(applicationContext, this, setsList)
        setsRecyclerView.adapter = setListAdapter

        val addSetBtn: Button = findViewById(R.id.addSetBtn)
        addSetBtn.setOnClickListener{
            addNewSet()
        }

        exerciseImportantView = findViewById(R.id.importantExercise)
        exerciseImportantView.setOnClickListener {
            exerciseImportant = !exerciseImportant
            if(exerciseImportant){
                exerciseImportantView.setImageResource(R.drawable.baseline_label_important_24)
            }else{
                exerciseImportantView.setImageResource(R.drawable.baseline_label_important_outline_24)
            }
        }
        handleIncomingIntent(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_edit_exercise_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun handleIncomingIntent(intent: Intent) {
        var exerciseToEdit: Exercise? = null
        if (intent.hasExtra("isEdit")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                isEditing = intent.getBooleanExtra("isEdit", false)
                if (isEditing){
                    exerciseToEdit = intent.getParcelableExtra("exerciseToEdit", Exercise::class.java)
                }
            } else {
                @Suppress("DEPRECATION")
                isEditing = intent.getBooleanExtra("isEdit", false)
                if (isEditing){
                    @Suppress("DEPRECATION")
                    exerciseToEdit = intent.getParcelableExtra("exerciseToEdit")
                }
            }
        }
        if (intent.hasExtra("exerciseRaw")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                exerciseRaw = intent.getParcelableExtra("exerciseRaw", ExerciseRaw::class.java)!!
                if (isEditing){
                    exerciseToEdit = intent.getParcelableExtra("exerciseToEdit", Exercise::class.java)
                }
            } else {
                @Suppress("DEPRECATION")
                exerciseToEdit = intent.getParcelableExtra("exerciseToEdit")
            }
            exerciseTitleView.text = exerciseRaw.name
            exerciseDescriptionView.text = exerciseRaw.description
            exerciseTitleView.isEnabled = false
            exerciseDescriptionView.isEnabled = false
            val categoryName = exerciseRaw.category.name.lowercase().replaceFirstChar { it.uppercase() }
            exerciseCategoryView.text = categoryName
            if (!exerciseRaw.imageUri.isNullOrEmpty()) {
                uriPic = Uri.parse(exerciseRaw.imageUri)
                exerciseImg.setImageURI(uriPic)
                pictureSet = true
            }
        }
        if (isEditing && exerciseToEdit != null) {
            exerciseBeingEditedId = exerciseToEdit.exId
            exerciseImportant = exerciseToEdit.isImportant
            if(exerciseImportant){
                exerciseImportantView.setImageResource(R.drawable.baseline_label_important_24)
            }else{
                exerciseImportantView.setImageResource(R.drawable.baseline_label_important_outline_24)
            }
            setsList.clear()
            setsList.addAll(exerciseToEdit.setList)
            Log.d("SETS", setsList.toString())
            setListAdapter.notifyDataSetChanged()
        }
    }

    private fun addNewSet() {
        val repsInput = repsTextView.text.toString()
        val weightInput = weightTextView.text.toString()

        if (repsInput.isBlank() || weightInput.isBlank()) {
            Toast.makeText(this, getString(R.string.toast_new_set_missing_error), Toast.LENGTH_SHORT).show()
            return
        }

        val reps: Int
        val weight: Float

        try {
            reps = repsInput.toInt()
            weight = weightInput.toFloat()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, getString(R.string.toast_new_set_invalid_input_error), Toast.LENGTH_SHORT).show()
            return
        }

        val setOrder = setsList.size + 1

        val newSet = Set(
            setOrder = setOrder,
            weight = weight,
            reps = reps
        )

        setsList.add(newSet)
        setListAdapter.notifyItemInserted(setsList.size - 1)

        repsTextView.text=""
        weightTextView.text=""
    }


    private fun finalizeExercise(){
        if (setsList.isEmpty()){
            Toast.makeText(this,"Make sure to enter at least one set!", Toast.LENGTH_SHORT).show()
            return
        }

        val newExercise: Exercise = Exercise(
            exerciseRawData = exerciseRaw,
            exerciseRawId = exerciseRaw.exRawId,
            setList =  setsList,
            isImportant = exerciseImportant,
            exId = exerciseBeingEditedId?: 0,
            userId = currentUserId ?: ""
            )

        Log.d("New Exercise", "Create new exercise to the UserId : ${newExercise.userId}")
        val intent = Intent(this,MainActivity::class.java)
        intent.putExtra("exercise",newExercise)
        intent.putExtra("isEdit",isEditing)
        startActivity(intent)
    }

    private fun cancelExercise(){
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                cancelExercise()
                true
            }
            R.id.action_finish -> {
                finalizeExercise()
                true
            }
            R.id.action_cancel -> {
                cancelExercise()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

