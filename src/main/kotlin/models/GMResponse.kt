package models

import java.sql.Timestamp

data class GMResponse(
    val groupId: String,
    val senderId: String,
    val messageLength: Int,
    override val message: String,
    override val sendTime: Timestamp
): Message(sendTime, groupId, message)
