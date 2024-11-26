package ca.qc.bdeb.c5gm.exerdex.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ca.qc.bdeb.c5gm.exerdex.data.Exercise
import ca.qc.bdeb.c5gm.exerdex.room.ExerciseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExerciseViewModel(application: Application): AndroidViewModel(application) {
    private val exerciseDao = ExerciseDatabase.getExerciseDatabase(application).exerciseDao()
    val exercisesToDo: LiveData<List<Exercise>> = exerciseDao.loadExerciseByDone(false)
    val exercisesDone: LiveData<List<Exercise>> = exerciseDao.loadExerciseByDone(true)

    fun insert(exercise: Exercise) = viewModelScope.launch(Dispatchers.IO) {
        exerciseDao.insertAll(exercise)
    }

    fun update(exercise: Exercise) = viewModelScope.launch(Dispatchers.IO) {
        exerciseDao.updateAll(exercise)
    }

    fun deleteDoneExercises() = viewModelScope.launch(Dispatchers.IO) {
        exerciseDao.deleteAllExercisesDone()
    }



}