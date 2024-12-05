package ca.qc.bdeb.c5gm.exerdex

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainer
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.exerdex.adaptors.DoneListAdaptor
import ca.qc.bdeb.c5gm.exerdex.adaptors.ExerciseListAdaptor
import ca.qc.bdeb.c5gm.exerdex.adaptors.ExerciseRawListAdaptor
import ca.qc.bdeb.c5gm.exerdex.data.Exercise
import ca.qc.bdeb.c5gm.exerdex.data.Set
import ca.qc.bdeb.c5gm.exerdex.data.ExerciseRaw
import ca.qc.bdeb.c5gm.exerdex.data.Workout
import ca.qc.bdeb.c5gm.exerdex.fragments.ActiveWorkoutManagementFragment
import ca.qc.bdeb.c5gm.exerdex.fragments.ExerciesDoneFragment
import ca.qc.bdeb.c5gm.exerdex.fragments.ExercisePopUp
import ca.qc.bdeb.c5gm.exerdex.fragments.ExercisesToDoFragment
import ca.qc.bdeb.c5gm.exerdex.fragments.ProfileFragment
import ca.qc.bdeb.c5gm.exerdex.room.ExerciseDatabase
import ca.qc.bdeb.c5gm.exerdex.viewmodels.ActiveWorkoutViewModel
import ca.qc.bdeb.c5gm.exerdex.viewmodels.SharedViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig


class MainActivity : AppCompatActivity() {

    private var isAuthenticated = false
    private var currentUserId: String? = null
    private val AWViewModel: ActiveWorkoutViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by viewModels()
    lateinit var roomDatabase: ExerciseDatabase
    private val GOOGLE_API_KEY = "AIzaSyBOnYb38iQBHfTR1boMeE-g8D-HEewteSA"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        roomDatabase = ExerciseDatabase.getExerciseDatabase(applicationContext)

        // source :
        // https://developer.android.com/training/data-storage/shared-preferences?hl=fr
        // https://www.topcoder.com/thrive/articles/shared-preferences-in-android#:~:text=The%20primary%20purpose%20of%20SharedPreference,t%20require%20any%20specific%20structure.
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        isAuthenticated = sharedPref.getBoolean("isAuthenticated", false)
        currentUserId = sharedPref.getString("currentUserId", null)

        if (isAuthenticated && currentUserId != null) {
            sharedViewModel.updateUserId(currentUserId!!)
            showMainFragments()
            lifecycleScope.launch(Dispatchers.IO) {
                reloadDataFromDatabase(currentUserId!!)
            }
        } else {
            showAuthenticationFragment()
        }

    }

    fun onLogout(){
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()
        sharedViewModel.updateUserId(null.toString())
        showAuthenticationFragment()
    }

    fun onLoginSuccessful(userId: String) {
        isAuthenticated = true
        currentUserId = userId
        sharedViewModel.updateUserId(userId)

        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("isAuthenticated", true)
            putString("currentUserId", userId)
            apply()
        }

        runOnUiThread {
            showMainFragments()
        }
        lifecycleScope.launch(Dispatchers.IO) {
            reloadDataFromDatabase(userId)
        }
    }

    private fun showAuthenticationFragment() {
        findViewById<FragmentContainerView>(R.id.fragmentContainerAuthentication).visibility = View.VISIBLE

        findViewById<FragmentContainerView>(R.id.fragmentContainerProfile).visibility = View.GONE
        findViewById<FragmentContainerView>(R.id.AWExercisesToDoFragment).visibility = View.GONE
        findViewById<FragmentContainerView>(R.id.AWExercisesDoneFragment).visibility = View.GONE
        findViewById<FragmentContainerView>(R.id.AWManagementFragment).visibility = View.GONE
        findViewById<FragmentContainerView>(R.id.popupFragment).visibility = View.GONE
        findViewById<Toolbar>(R.id.toolbar).visibility = View.GONE
    }

    private fun showMainFragments() {

        findViewById<FragmentContainerView>(R.id.fragmentContainerAuthentication).visibility = View.GONE

        findViewById<FragmentContainerView>(R.id.AWExercisesToDoFragment).visibility = View.VISIBLE
        findViewById<FragmentContainerView>(R.id.AWExercisesDoneFragment).visibility = View.VISIBLE
        findViewById<FragmentContainerView>(R.id.AWManagementFragment).visibility = View.VISIBLE


        findViewById<Toolbar>(R.id.toolbar).visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent) {
        if (intent.hasExtra("currentUserId")) {
            currentUserId = intent.getStringExtra("currentUserId")
            sharedViewModel.updateUserId(currentUserId!!)
        }

        if (intent.hasExtra("exercise")) {
            var newExercise:Exercise?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                newExercise = intent.getParcelableExtra("exercise", Exercise::class.java)
            } else {
                @Suppress("DEPRECATION")
                newExercise = intent.getParcelableExtra("exercise")
            }
            val isEdit: Boolean = intent.getBooleanExtra("isEdit",false)
            newExercise?.let {
                var actionDone: String = "Added"
                if (isEdit){
                    actionDone = "Edited"
                    lifecycleScope.launch(Dispatchers.IO) {
                        roomDatabase.exerciseDao().updateAll(it)
                        // Reload data after update
                        currentUserId?.let { it1 -> reloadDataFromDatabase(it1) }
                    }
                } else {
                    Log.d("MainActivity", "CurrentUserId: $currentUserId")
                    if (currentUserId == null) {
                        Toast.makeText(this, "User ID is null. Cannot add exercise.", Toast.LENGTH_SHORT).show()
                        return
                    }
                    it.userId = currentUserId!!
                    lifecycleScope.launch(Dispatchers.IO) {
                        roomDatabase.exerciseDao().insertAll(it)
                        reloadDataFromDatabase(currentUserId!!)
                    }
                }
                intent.removeExtra("exercise") // Pour pas que les exercises se rajoutent lors
                                                     // du changement d'orientation.
                Toast.makeText(this, "Exercise ${actionDone}: ${it.exerciseRawData.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_history -> {
                val intent = Intent(this, ArchivedWorkouts::class.java)
                intent.putExtra("currentUserId", currentUserId)
                startActivity(intent)
                true
            }
            R.id.action_about -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.info_title)
                    .setMessage(R.string.info_message)
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                builder.create().show()
                true
            }
            R.id.action_settings -> {
                
                findViewById<FragmentContainerView>(R.id.AWExercisesToDoFragment).visibility = View.GONE
                findViewById<FragmentContainerView>(R.id.AWExercisesDoneFragment).visibility = View.GONE
                findViewById<FragmentContainerView>(R.id.AWManagementFragment).visibility = View.GONE
                findViewById<Toolbar>(R.id.toolbar).visibility = View.GONE
                findViewById<FragmentContainerView>(R.id.fragmentContainerProfile).visibility = View.VISIBLE
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


    fun addExercise(isEdit: Boolean, exerciseToEdit: Exercise?, exerciseRaw: ExerciseRaw?) {
        val intent = Intent(this, AddEditExerciseActivity::class.java)
        intent.putExtra("isEdit",isEdit)
        intent.putExtra("currentUserId", currentUserId)
        if (isEdit){
            intent.putExtra("exerciseToEdit",exerciseToEdit)
        }
        if (exerciseRaw != null){
            intent.putExtra("exerciseRaw",exerciseRaw)
        }
        startActivity(intent)
    }

    private fun promptGemini(userRequest: String, clearCurrentToDo: Boolean){
        val model = GenerativeModel(
            "gemini-1.5-pro",
            // Retrieve API key as an environmental variable defined in a Build Configuration
            // see https://github.com/google/secrets-gradle-plugin for further instructions
            GOOGLE_API_KEY,
            generationConfig = generationConfig {
                temperature = 1f
                topK = 40
                topP = 0.95f
                maxOutputTokens = 8192
                responseMimeType = "text/plain"
            },
            systemInstruction = content { text("#Role\nYou are an AI workout creator.\n#Task\nUsers will ask you to create different workouts with different possible exercises as a choice for the workout. You are never to choose an exercise that does was not given to you by the user.\nYou are to create as many sets, reps and weight as you deem necessary according to the user's demand. You are to infer weights from the user's request. If infering is impossible, simply set the weights to 0 (e.g., sets of 0x12, 0x10, etc)\n#Context\nThe application you are working with has the following tables:\ndata class ExerciseRaw (\n    var name: String,\n    val description: String = \"\",\n    val category: MuscleCategory,\n    val imageUri: String? = null,\n    val userId: String,\n    @PrimaryKey(autoGenerate = true) val exRawId: Int = 0\n)\ndata class Exercise(\n    val exerciseRawData: ExerciseRaw,\n    val exerciseRawId: Int,\n    val setList: List<Set>,\n    var isDone: Boolean = false,\n    var isImportant: Boolean,\n    var userId: String,\n    @PrimaryKey(autoGenerate = true) val exId: Int = 0\n)\n#Request Formats\nYou will receive requests in the following format:\n\"user_request: '{REQUEST_HERE}', existing_exercises_raw: '{ALL_EXERCISES_RAW_HERE}'\"\nExample of input:\n\"user_request:'I want you to create a push-day workout for me. High intensity, low volume. Hypertrophy training', existing_exercises_raw: 'index:0, name='barbell bench press', description='Lie flat on the bench, wide grip', category=CHEST ;; index:1, name='dumbbell shoulder press', description='seated', category=SHOULDERS\"\n#Response Format\nYou are to respond in the following format:\nA list of all exercises to be added. Separate each exercise by four semi-colons and each information by two semi-colons. Always include four semicolons at the end;\nsets_list is a list of sets, each set separated by a semicolon and the weight and reps separated by an \"x\" character.\nis_important is a boolean. Either true or false:\n{exercise_raw_index};;{sets_list};;{is_important};;;;{exercise_raw_index};;{sets_list};;{is_important};;;;\nExample of output:\n1;;185x12;185x10;x185x8;;true;;;;4;;65x8;65x8;65x8;;false;;;;\n#Example of full usage\n##Input:\nuser_request: 'I want you to create a pushday low volume high-intensity workout. For reference, I can PR bench 250', existing_exercises_raw: 'index:0, name='barbell bench press', description='Lie flat on the bench, wide grip', category=CHEST ;; index:1, name='dumbbell shoulder press', description='Seated dumbbell press for shoulders', category=SHOULDERS ;; index:2, name='tricep dips', description='Bodyweight dips on parallel bars', category=TRICEPS ;; index:3, name='pull-ups', description='Wide-grip pull-ups for lats and upper back', category=BACK ;; index:4, name='barbell bent-over row', description='Rowing with barbell to target back muscles', category=BACK ;; index:5, name='hammer curls', description='Dumbbell curls to target the biceps', category=BICEPS'\n##Output:\n0;;185x12;205x10;205x10;;true;;;;2;;0x20;0x12;0x10;;false;;;;1;;60x12;75x12;75x12;;true;;;;\n") },
        )
        if (currentUserId == null) {
            Toast.makeText(this, "User ID is null.", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch(Dispatchers.IO){
            val exercisesRaw = roomDatabase.exerciseDao().loadExerciseRawByUser(currentUserId!!)
            if (exercisesRaw.isEmpty()) {
                withContext(Dispatchers.Main){
                    Toast.makeText(this@MainActivity, "No exercises available.", Toast.LENGTH_SHORT).show()
                }
                return@launch
            } else {
                val contextStringBuilder = StringBuilder()
                for ((index, exerciseRaw) in exercisesRaw.withIndex()) {
                    contextStringBuilder.append("index:$index, ${exerciseRaw.toCompactString()} ;; ")
                }
                val contextString = contextStringBuilder.toString()
                val response = model.generateContent("user_request: '$userRequest', existing_exercises_raw: '$contextString'")
                val responseText = response.text
                withContext(Dispatchers.Main){
                    if (responseText.isNullOrEmpty()){
                        Toast.makeText(this@MainActivity, "Something went wrong with gemini...", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("promptGemini", "promptGemini response: $responseText")
                        parseGeminiOutput(responseText, clearCurrentToDo, exercisesRaw)
                    }
                }
            }
        }
    }

    private fun parseGeminiOutput(geminiResponse: String,
                                  clearCurrentToDo: Boolean,
                                  exercisesRaw: List<ExerciseRaw>){
        val exercisesToAddString = geminiResponse.split(";;;;").filter { it.isNotBlank() }
        if (exercisesToAddString.count()>1) {
            var toDoList: MutableList<Exercise> = mutableListOf()
            for (exerciseToAdd in exercisesToAddString) {
                val parametersString = exerciseToAdd.split(";;")
                if (parametersString.size < 3) continue
                Log.d("promptGemini", "parametersString: $parametersString")
                val index = parametersString[0].toIntOrNull() ?: continue
                val setsString = parametersString[1].split(";").filter { it.isNotBlank() }
                val setsMutableList = mutableListOf<Set>()
                for ((order, setString) in setsString.withIndex()) {
                    if (setString.isBlank() || !setString.contains("x")) continue
                    val setValString = setString.split("x")
                    setsMutableList.add(
                        Set(
                            order,
                            setValString[0].toFloatOrNull() ?: 0f,
                            setValString[1].toIntOrNull() ?: 0
                        )
                    )
                }
                val isImportant = parametersString[2].trim().equals("true", ignoreCase = true)
                Log.d("promptGemini", "isImportant value after parsing: $isImportant (raw: '${parametersString[2]}')")
                toDoList.add(Exercise(
                    exerciseRawData = exercisesRaw[index],
                    exerciseRawId = exercisesRaw[index].exRawId,
                    isImportant = isImportant,
                    setList = setsMutableList.toList(),
                    userId = currentUserId!!
                ))
            }
            lifecycleScope.launch(Dispatchers.IO){
                if (clearCurrentToDo){
                    roomDatabase.exerciseDao().deleteAllExercises(currentUserId!!)
                }
                roomDatabase.exerciseDao().insertAll(*toDoList.toTypedArray())
                reloadDataFromDatabase(currentUserId!!)
            }
        } else {
            Toast.makeText(this, "No exercises found to add", Toast.LENGTH_SHORT).show()
        }
    }

    fun showGenerateExercisesDialog(){
        val dialogView = layoutInflater.inflate(R.layout.generate_exercises_dialog, null)

        val userRequestInput = dialogView.findViewById<EditText>(R.id.generateExoPromptInput)
        val generateExercisesBtn = dialogView.findViewById<Button>(R.id.sendPromptBtn)
        val clearCurrentCheckBox = dialogView.findViewById<CheckBox>(R.id.clearCurrentCheckBox)

        generateExercisesBtn.setOnClickListener {
            if (userRequestInput.text.toString().length<4){
                Toast.makeText(this@MainActivity, "Please enter a valid prompt", Toast.LENGTH_LONG).show()
            } else {
                promptGemini(userRequestInput.text.toString(), clearCurrentCheckBox.isChecked)
            }
        }

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    fun showAddExerciseDialog(){
        val dialogView = layoutInflater.inflate(R.layout.add_exercise_dialog, null)

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.existingExoRawsRecycler)
        val createCustomBtn = dialogView.findViewById<Button>(R.id.createNewExoRawBtn)
        lifecycleScope.launch(Dispatchers.IO) {
            val exercisesRaw = roomDatabase.exerciseDao().loadExerciseRawByUser(currentUserId!!)
            withContext(Dispatchers.Main){
                val adaptor = ExerciseRawListAdaptor(
                    applicationContext,
                    this@MainActivity,
                    exercisesRaw
                ) { isEdit: Boolean, exerciseToEdit: Exercise?, exerciseRaw: ExerciseRaw?
                        ->
                        addExercise(isEdit, exerciseToEdit, exerciseRaw)
                }
                recyclerView.adapter = adaptor
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setView(dialogView)
                val dialog = builder.create()
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                dialog.show()
            }
        }
        createCustomBtn.setOnClickListener {
            val intent = Intent(this, CreateNewExerciseRaw::class.java)
            intent.putExtra("currentUserId", currentUserId)
            startActivity(intent)
        }
    }


    suspend fun reloadDataFromDatabase(userId: String) {
        Log.d("Reload", "UserID from loading : ${userId}")
        val exercisesToDoFromDB = roomDatabase.exerciseDao().loadExerciseByDone(false, userId)
        val exercisesDoneFromDB = roomDatabase.exerciseDao().loadExerciseByDone(true, userId)
        Log.d("databaseLOGS","Table, on load undone: $exercisesToDoFromDB")
        Log.d("databaseLOGS","Table, on load done: $exercisesDoneFromDB")
        runOnUiThread {
            AWViewModel.updateBothLists(exercisesToDoFromDB, exercisesDoneFromDB)
        }
    }

}