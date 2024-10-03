package ca.qc.bdeb.c5gm.exerdex

data class Exercise(
    val name: String,
    val description: String = "",
    val category: MuscleCategory,
    val setList: List<Set>
)
enum class MuscleCategory {
    CHEST, BACK, BICEPS, TRICEPS, QUADS, HAMSTRINGS, CALVES, ABS, FOREARMS, SHOULDERS, TRAPS, NECK, CARDIO
}

data class Set(
    val setOrder: Int,
    val weight: Float,
    val reps: Int
)