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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.exerdex.adaptors.SetListAdaptor
import ca.qc.bdeb.c5gm.exerdex.data.Exercise
import ca.qc.bdeb.c5gm.exerdex.data.ExerciseRaw
import ca.qc.bdeb.c5gm.exerdex.data.MuscleCategory
import ca.qc.bdeb.c5gm.exerdex.data.Set
import ca.qc.bdeb.c5gm.exerdex.room.ExerciseDatabase
import ca.qc.bdeb.c5gm.exerdex.viewholders.ItemSetHolder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date
import java.util.Locale

class CreateNewExerciseRaw : AppCompatActivity() {
    private lateinit var exerciseTitleView: TextView
    private lateinit var exerciseDescriptionView: TextView
    private lateinit var selectedCategory: MuscleCategory
    private lateinit var pictureTake: ImageView
    private lateinit var manipulatePicture: ImageView
    private lateinit var uriPic: Uri
    private lateinit var picTaken: ActivityResultLauncher<Uri>
    private lateinit var picSelected: ActivityResultLauncher<PickVisualMediaRequest>
    private var pictureSet: Boolean = false
    lateinit var roomDatabase: ExerciseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_new_exercise_raw)
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

        exerciseTitleView = findViewById(R.id.exerciseNameInput)
        exerciseDescriptionView = findViewById(R.id.exerciseDescInput)
        initializeCategorySpinnner()
        roomDatabase = ExerciseDatabase.getExerciseDatabase(applicationContext)
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


    private fun finalizeExercise(){
        if (exerciseTitleView.text.toString().isBlank()){
            Toast.makeText(this,"Make sure to enter a title.", Toast.LENGTH_SHORT).show()
            return
        }

        val newExercise: ExerciseRaw = ExerciseRaw(
            name = exerciseTitleView.text.toString(),
            description = exerciseDescriptionView.text.toString(),
            category = selectedCategory,
            imageUri = if (pictureSet) uriPic.toString() else null,
        )

        val intent = Intent(this,MainActivity::class.java)
        lifecycleScope.launch(Dispatchers.IO){
            roomDatabase.exerciseDao().insertExerciseRaw(newExercise)
            withContext(Dispatchers.Main){
                startActivity(intent)
            }
        }
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

