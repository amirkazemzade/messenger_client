package pages

import MyServerException
import PMReceiverSocket
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import models.Message
import models.PMResponse
import java.time.format.DateTimeFormatter

@Composable
@Preview
fun Home(navigation: (HomeWrapperRoutes) -> Unit, sid: String, username: String, onLogOut: () -> Unit) {

    val scaffoldState = rememberScaffoldState()

    val coroutineScope = rememberCoroutineScope()

    val pmSocket: PMReceiverSocket by remember {
        mutableStateOf(PMReceiverSocket())
    }

    var isConnectionErrorEnabled by remember {
        mutableStateOf(false)
    }

    var messages: Map<String, MutableList<Message>> by rememberSaveable {
        mutableStateOf(hashMapOf())
    }

    fun getAllMessages() {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val allMessages = pmSocket.getAllMessages(sid)
                val map = hashMapOf<String, MutableList<Message>>()
                allMessages.forEach { message ->
                    if (message is PMResponse) {
                        if (message.senderId == username && message.receiverId == username) {
                            if (!map.containsKey(message.senderId)) {
                                map[message.senderId] = mutableListOf(message)
                            } else {
                                map[message.senderId]?.apply {
                                    add(message)
                                    sortByDescending { it.sendTime }
                                }
                            }
                        } else if (message.senderId == username) {
                            if (!map.containsKey(message.receiverId)) {
                                map[message.receiverId] = mutableListOf(message)
                            } else {
                                map[message.receiverId]?.apply {
                                    add(message)
                                    sortByDescending { it.sendTime }
                                }
                            }
                        } else if (message.receiverId == username) {
                            if (!map.containsKey(message.senderId)) {
                                map[message.senderId] = mutableListOf(message)
                            } else {
                                map[message.senderId]?.apply {
                                    add(message)
                                    sortByDescending { it.sendTime }
                                }
                            }
                        }
                    } else {
                        // todo implement
                    }
                }
                messages = map
                isConnectionErrorEnabled = false
            } catch (e: MyServerException) {
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
                    val map = HashMap(messages)
                    if (!messages.containsKey(message.messageSourceId)) {
                        map[message.messageSourceId] = mutableListOf(message)
                    } else {
                        val list = map[message.messageSourceId]
                        list?.apply {
                            add(message)
                            sortByDescending { it.sendTime }
                        }
                        map[message.messageSourceId] = list
                        messages = HashMap()
                    }
                    messages = map
                    isConnectionErrorEnabled = false
                } catch (e: MyServerException) {
                    e.printStackTrace()
                    isConnectionErrorEnabled = true
                }
            }
        }
    }

    suspend fun connect() {
        getAllMessages()
        delay(500)
        waitForMessages()
        delay(500)
    }

    LaunchedEffect(scaffoldState) {
        connect()
    }

    if (isConnectionErrorEnabled) {
        MaterialTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Connection Error!") }
                    )
                }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                connect()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Refresh, "Reconnect")
                    }
                }
            }
        }
    } else {
        MaterialTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Hey, $username") },
                        actions = {
                            Button(
                                onClick = { navigation(HomeWrapperRoutes.NewPrivateMessage) },
                                colors = ButtonDefaults.outlinedButtonColors()
                            ) {
                                Text("New Private Message")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {},
                                colors = ButtonDefaults.outlinedButtonColors()
                            ) {
                                Text("New Group")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = onLogOut,
                                colors = ButtonDefaults.outlinedButtonColors()
                            ) {
                                Text("Log Out")
                            }
                        }
                    )
                }
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    var list = messages.entries.toList()
                    list = list.sortedByDescending { it.value.first().sendTime }
                    items(list) { messageEntry ->
                        Button(
                            modifier = Modifier,
                            onClick = {
                                val messagesFromThisUser = messageEntry.value.map {
                                    it as PMResponse
                                }
                                navigation(
                                    HomeWrapperRoutes.PrivateMessage(
                                        messagesFromThisUser.toList(),
                                        messageEntry.key
                                    )
                                )
                            },
                            colors = ButtonDefaults.outlinedButtonColors()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    horizontalArrangement = Arrangement.Start,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 16.dp)
                                ) {
                                    val message = messageEntry.value.first()
                                    if (message is PMResponse)
                                        Text(
                                            "PM",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    else {
                                        Text(
                                            "GM",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(32.dp).height(0.dp))
                                    Box(
                                        contentAlignment = Alignment.CenterStart,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        if (message is PMResponse) {
                                            if (message.senderId == username) {
                                                Text(message.receiverId)
                                            } else {
                                                Text(message.senderId)
                                            }
                                        } else {
                                            Text(message.messageSourceId)
                                        }
                                    }
                                    Box(
                                        contentAlignment = Alignment.CenterEnd,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            message.sendTime.toLocalDateTime()
                                                .format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}