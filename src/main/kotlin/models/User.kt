package models

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize

@Parcelize
data class User(
    val id: String? = null,
    val username: String? = null,
    val password: String? = null
) : Parcelable