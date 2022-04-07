package models

import java.sql.Timestamp

data class PMResponse(
    override val sendTime: Timestamp,
    val senderId: String,
    val receiverId: String,
    val messageLength: Int,
    val message: String
): Message(sendTime, senderId)