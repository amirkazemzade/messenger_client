package pages

import MyServerException
import PMReceiverSocket
import PMSenderSocket
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import models.PMResponse
import java.time.format.DateTimeFormatter

@Composable
fun PrivateMessage(navigation: (HomeWrapperRoutes) -> Unit, listOfMessages: List<PMResponse>, sid:String, desUsername: String) {

    val coroutineScope = rememberCoroutineScope()

    val scaffoldState = rememberScaffoldState()

    var typingMessage by remember {
        mutableStateOf("")
    }
    
    val pmSocket: PMReceiverSocket by remember {
        mutableStateOf(PMReceiverSocket())
    }

    var messages by remember {
        mutableStateOf(listOfMessages)
    }

    var isConnectionErrorEnabled by remember {
        mutableStateOf(false)
    }

    fun getAllMessages(){
        coroutineScope.launch(Dispatchers.IO){
            try {
                val allMessages = pmSocket.getAllMessagesFrom(sid, desUsername)
                messages = allMessages.sortedByDescending { it.sendTime }.map { it as PMResponse }
                isConnectionErrorEnabled = false
            } catch (e: MyServerException){
                e.printStackTrace()
                isConnectionErrorEnabled = true
            }
        }
    }

    fun waitForMessages() {
        coroutineScope.launch(Dispatchers.IO) {
            while (!isConnectionErrorEnabled) {
                try {
                    val message = pmSocket.receive(sid)
                    if (message is PMResponse){
                        if (message.senderId == desUsername || message.receiverId == desUsername){
                            val newMessages = messages.toMutableList()
                            newMessages.add(message)
                            messages = newMessages.sortedByDescending { it.sendTime }
                        }
                    }
                    isConnectionErrorEnabled = false
                } catch (e: MyServerException) {
                    e.printStackTrace()
                    isConnectionErrorEnabled = true
                }
            }
        }
    }
    
    LaunchedEffect(scaffoldState){
        delay(500)
        getAllMessages()
        delay(500)
        waitForMessages()
    }

    fun sendPM(){
        if (typingMessage == "") return
        coroutineScope.launch(Dispatchers.IO){
            try {
                val senderSocket = PMSenderSocket()
                senderSocket.sendPM(desUsername, sid, typingMessage)
                isConnectionErrorEnabled = false
                typingMessage = ""
            } catch (e: MyServerException){
                e.printStackTrace()
                isConnectionErrorEnabled = true
            }
        }
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(desUsername) },
                    navigationIcon = {
                        IconButton(
                            onClick = { navigation(HomeWrapperRoutes.Home) }
                        ) {
                            Icon(Icons.Outlined.ArrowBack, contentDescription = "Back Button")
                        }
                    }
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    reverseLayout = true
                ) {
                    messages = messages.sortedByDescending { it.sendTime.time }
                    items(messages) { message ->
                        if (message.messageSourceId == desUsername) {
                            ReceivedMessage(message)
                        } else {
                            SentMessage(message)
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedTextField(
                        value = typingMessage,
                        placeholder = { Text("Write a message...") },
                        onValueChange = { typingMessage = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = { sendPM() },
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send Message")
                    }
                }
            }
        }
    }
}

@Composable
fun SentMessage(message: PMResponse) {
    MessageBox(boxColor = Color(color = 0xFFC5CAE9), alignment = Alignment.CenterEnd, message)
}

@Composable
fun ReceivedMessage(message: PMResponse) {
    MessageBox(boxColor = Color(color = 0xFF8C9EFF), alignment = Alignment.CenterStart, message)
}

@Composable
private fun MessageBox(boxColor: Color, alignment: Alignment, message: PMResponse) {
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
            Text(text = message.senderId, fontWeight = FontWeight.SemiBold)
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