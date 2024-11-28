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
    private lateinit var exerciseImportantView: ImageView
    private lateinit var selectedCategory: MuscleCategory
    private lateinit var pictureTake: ImageView
    private lateinit var manipulatePicture: ImageView
    private lateinit var uriPic: Uri
    private lateinit var picTaken: ActivityResultLauncher<Uri>
    private lateinit var picSelected: ActivityResultLauncher<PickVisualMediaRequest>
    private var pictureSet: Boolean = false
    private var exerciseImportant: Boolean = false
    private var setsList: MutableList<Set> = mutableListOf()
    private var isEditing: Boolean = false
    private var exerciseBeingEditedId: Int? = null




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

        pictureTake = findViewById(R.id.pictureTakeId)
        manipulatePicture = findViewById(R.id.AddRemovePicButton)

        picTaken = registerForActivityResult(ActivityResultContracts.TakePicture()){ success ->
            if(success){
                pictureTake.setImageURI(uriPic)
                manipulatePicture.setImageResource(R.drawable.baseline_cancel_24_wh)
                pictureSet = true
            }else{
                Toast.makeText(this, "Échec de la prise de photo.", Toast.LENGTH_SHORT).show()
            }
        }

        picSelected = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                // Save the selected image to a local file
                val savedUri = saveSelectedImageToLocalFile(uri)
                if (savedUri != null) {
                    pictureTake.setImageURI(savedUri)
                    uriPic = savedUri
                    manipulatePicture.setImageResource(R.drawable.baseline_cancel_24_wh)
                    pictureSet = true
                } else {
                    Toast.makeText(this, "Failed to save selected image.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        manipulatePicture.setOnClickListener {
            if(pictureSet){
                pictureTake.setImageResource(R.drawable.baseline_photo_camera_24)
                manipulatePicture.setImageResource(R.drawable.baseline_add_circle_24)
                pictureSet = false
                uriPic = Uri.EMPTY
            }else{
                showMenuImage()
            }
        }

        repsTextView = findViewById(R.id.newSetReps)
        weightTextView = findViewById(R.id.newSetWeight)
        exerciseTitleView = findViewById(R.id.exerciseNameInput)
        exerciseDescriptionView = findViewById(R.id.exerciseDescInput)
        setsRecyclerView = findViewById(R.id.setsRecyclerView)
        setListAdapter = SetListAdaptor(applicationContext, this, setsList)
        setsRecyclerView.adapter = setListAdapter
        initializeCategorySpinnner()


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

    private fun showMenuImage(){
        /*
        * Source : https://www.digitalocean.com/community/tutorials/android-alert-dialog-using-kotlin
        * */
        val options = arrayOf(getString(R.string.take_a_photo), getString(R.string.choose_from_gallery))
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.add_image))

        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    takePic()
                }
                1 -> {
                    selectPic()
                }
            }
        }

        builder.setNegativeButton(R.string.cancel_word) { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun takePic(){
        uriPic = createUriPic()
        picTaken.launch(uriPic)
    }

    private fun selectPic(){
        picSelected.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }


    private fun createUriPic(): Uri{
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val pictureFile: File = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "IMG_$timeStamp.jpg")
        return FileProvider.getUriForFile(this, "ca.qc.bdeb.c5gm.photoapp", pictureFile)
    }

    // source: ChatGPT 4o
    private fun saveSelectedImageToLocalFile(selectedImageUri: Uri): Uri? {
        try {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val pictureFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "IMG_$timeStamp.jpg")
            val inputStream = contentResolver.openInputStream(selectedImageUri) ?: return null
            val outputStream = pictureFile.outputStream()

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            return FileProvider.getUriForFile(this, "ca.qc.bdeb.c5gm.photoapp", pictureFile)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_edit_exercise_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun handleIncomingIntent(intent: Intent) {
        var exerciseToEdit: Exercise? = Exercise("haha","haha",MuscleCategory.ABS, listOf(), isImportant = exerciseImportant)
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
        if (isEditing && exerciseToEdit != null) {
            exerciseBeingEditedId = exerciseToEdit.exId
            exerciseTitleView.text = exerciseToEdit.name
            exerciseDescriptionView.text = exerciseToEdit.description

            exerciseImportant = exerciseToEdit.isImportant
            if(exerciseImportant){
                exerciseImportantView.setImageResource(R.drawable.baseline_label_important_24)
            }else{
                exerciseImportantView.setImageResource(R.drawable.baseline_label_important_outline_24)
            }

            if (!exerciseToEdit.imageUri.isNullOrEmpty()) {
                uriPic = Uri.parse(exerciseToEdit.imageUri)
                pictureTake.setImageURI(uriPic)
                manipulatePicture.setImageResource(R.drawable.baseline_cancel_24_wh)
                pictureSet = true
            }

            val spinner: Spinner = findViewById(R.id.muscleCategorySpinner)
            val categoryName = exerciseToEdit.category.name.lowercase().replaceFirstChar { it.uppercase() }
            val position = (spinner.adapter as ArrayAdapter<String>).getPosition(categoryName)
            if (position >= 0) {
                spinner.setSelection(position)
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
        if (exerciseTitleView.text.toString().isBlank() or setsList.isEmpty()){
            Toast.makeText(this,getString(R.string.toast_new_exercise_missing_info_error), Toast.LENGTH_SHORT).show()
            return
        }

        val newExercise: Exercise = Exercise(exerciseTitleView.text.toString(),
            exerciseDescriptionView.text.toString(),
            selectedCategory,
            setsList,
            isImportant = exerciseImportant,
            imageUri = if (pictureSet) uriPic.toString() else null,
            exId = exerciseBeingEditedId?: 0
            )
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

    private fun initializeCategorySpinnner(){
        val categoryEnum: MuscleCategory
        val spinner: Spinner = findViewById(R.id.muscleCategorySpinner)
        val categoryValues = MuscleCategory.values()
        val categoryStrings = categoryValues.map { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categoryStrings) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(Color.WHITE)
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(Color.WHITE)
                view.setBackgroundColor(Color.parseColor("#121212"))
                return view
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCategory = MuscleCategory.valueOf(categoryStrings[position].uppercase())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
        spinner.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
    }
}

