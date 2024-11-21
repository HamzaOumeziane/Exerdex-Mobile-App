package ca.qc.bdeb.c5gm.exerdex.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ca.qc.bdeb.c5gm.exerdex.data.Exercise

class ActiveWorkoutViewModel: ViewModel() {
    var toDoExercises = MutableLiveData<MutableList<Exercise>>()
    var doneExercises = MutableLiveData<MutableList<Exercise>>()
}