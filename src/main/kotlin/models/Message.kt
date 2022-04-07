package models

import java.sql.Timestamp

abstract class Message(
    open val sendTime: Timestamp,
    open val messageSourceId: String
)