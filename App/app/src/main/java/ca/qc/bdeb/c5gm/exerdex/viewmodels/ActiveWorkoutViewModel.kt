package ca.qc.bdeb.c5gm.exerdex.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ca.qc.bdeb.c5gm.exerdex.data.Exercise
import ca.qc.bdeb.c5gm.exerdex.room.ExerciseDatabase

class ActiveWorkoutViewModel: ViewModel() {
    var toDoExercises = MutableLiveData<MutableList<Exercise>>()
    var doneExercises = MutableLiveData<MutableList<Exercise>>()

    fun changeExerciseDoneState(item: Exercise, newDoneState: Boolean){
        val toDo = toDoExercises.value!!
        val done = doneExercises.value!!

        item.isDone = newDoneState // Pourrait être simplifier à isDone = !item.isDone mais cette
                                   // manière de faire est plus 'safe' :D
        if (newDoneState){
            toDo.remove(item)
            done.add(item)
        } else {
            done.remove(item)
            toDo.add(item)
        }
        updateBothLists(toDo, done)
    }

    fun updateBothLists(toDo: MutableList<Exercise>, done: MutableList<Exercise>){
        toDoExercises.value = toDo
        doneExercises.value = done
    }
    fun deleteExercise(item: Exercise){
        val toDo = toDoExercises.value ?: mutableListOf()
        toDo.remove(item)
        toDoExercises.value = toDo
    }
}