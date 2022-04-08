package pages

import GMReceiverSocket
import GMSenderSocket
import MyServerException
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import models.GMResponse

@Composable
fun GroupMessage(navigation: (HomeWrapperRoutes) -> Unit, listOfMessages: List<GMResponse>, sid:String, user: String, groupId: String) {

    val coroutineScope = rememberCoroutineScope()

    val scaffoldState = rememberScaffoldState()

    var typingMessage by remember {
        mutableStateOf("")
    }

    val gmSocket: GMReceiverSocket by remember {
        mutableStateOf(GMReceiverSocket())
    }

    var messages by remember {
        mutableStateOf(listOfMessages)
    }

    var isConnectionErrorEnabled by remember {
        mutableStateOf(false)
    }

    fun leaveGroup(){
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val senderSocket = GMSenderSocket()
                senderSocket.leaveGroup(groupId, sid)
                navigation(HomeWrapperRoutes.Home)
            } catch (e: MyServerException){
                e.printStackTrace()
            }
        }
    }

    fun getAllMessages(){
        coroutineScope.launch(Dispatchers.IO){
            try {
                val allMessages = gmSocket.getAllMessagesFrom(sid, groupId)
                messages = allMessages.sortedByDescending { it.sendTime }.map { it as GMResponse }
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
                    val message = gmSocket.receive(sid)
                    if (message is GMResponse){
                        if (groupId == message.groupId){
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
                val senderSocket = GMSenderSocket()
                senderSocket.sendGM(groupId, sid, typingMessage)
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
                    title = { Text(groupId) },
                    navigationIcon = {
                        IconButton(
                            onClick = { navigation(HomeWrapperRoutes.Home) }
                        ) {
                            Icon(Icons.Outlined.ArrowBack, contentDescription = "Back Button")
                        }
                    },
                    actions = {
                        Button(
                            onClick = { navigation(HomeWrapperRoutes.GroupUsers(groupId)) },
                            colors = ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text("Users List")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Button(
                            onClick = { leaveGroup() },
                            colors = ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text("Leave Group")
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
                        if (message.senderId != user) {
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
