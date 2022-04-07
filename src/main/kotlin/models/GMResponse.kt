package models

import java.sql.Timestamp

data class GMResponse(
    override val sendTime: Timestamp,
    val groupId: String
): Message(sendTime, groupId)
