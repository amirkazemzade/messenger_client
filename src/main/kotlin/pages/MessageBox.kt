package pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import models.GMResponse
import models.Message
import models.PMResponse
import java.time.format.DateTimeFormatter


@Composable
fun SentMessage(message: Message) {
    MessageBox(boxColor = Color(color = 0xFFC5CAE9), alignment = Alignment.CenterEnd, message)
}

@Composable
fun ReceivedMessage(message: Message) {
    MessageBox(boxColor = Color(color = 0xFF8C9EFF), alignment = Alignment.CenterStart, message)
}

@Composable
private fun MessageBox(boxColor: Color, alignment: Alignment, message: Message) {
    Box(
        contentAlignment = alignment,
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier
                .background(color = boxColor , shape = RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            if (message is PMResponse) {
                Text(text = message.senderId, fontWeight = FontWeight.SemiBold)
            } else if (message is GMResponse) {
                if (message.senderId != "null")
                Text(text = message.senderId, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = message.message)
                Spacer(modifier = Modifier.width(32.dp))
                Text(
                    text = message.sendTime.toLocalDateTime().format(
                        DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")
                    ),
                    color = Color.DarkGray
                )
            }
        }
    }
}