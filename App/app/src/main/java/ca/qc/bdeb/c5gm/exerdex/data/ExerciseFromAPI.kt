package ca.qc.bdeb.c5gm.exerdex.data

import com.google.gson.annotations.SerializedName

// de json2kt.com
data class ExerciseFromAPI (

    @SerializedName("name"         ) var name         : String? = null,
    @SerializedName("type"         ) var type         : String? = null,
    @SerializedName("muscle"       ) var muscle       : String? = null,
    @SerializedName("equipment"    ) var equipment    : String? = null,
    @SerializedName("difficulty"   ) var difficulty   : String? = null,
    @SerializedName("instructions" ) var instructions : String? = null

)