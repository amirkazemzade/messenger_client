package pages

import GMReceiverSocket
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
import models.GMResponse
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

    val gmSocket: GMReceiverSocket by remember {
        mutableStateOf(GMReceiverSocket())
    }

    var isConnectionErrorEnabled by remember {
        mutableStateOf(false)
    }

    var messages: Map<String, MutableList<Message>> by rememberSaveable {
        mutableStateOf(hashMapOf())
    }


    fun updateMessagesState(message: PMResponse): HashMap<String, MutableList<Message>> {
        val map = HashMap(messages)
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
        return map
    }

    fun getAllPMMessages() {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val allMessages = pmSocket.getAllMessages(sid)
                allMessages.forEach { message ->
                    messages = updateMessagesState(message)
                }
                isConnectionErrorEnabled = false
            } catch (e: MyServerException) {
                e.printStackTrace()
                isConnectionErrorEnabled = true
            }
        }
    }

    fun getAllGMMessages() {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val allMessages = gmSocket.getAllMessages(sid)
                val map = HashMap(messages)
                allMessages.forEach { message ->
                    if (!map.containsKey(message.senderId)) {
                        map[message.groupId] = mutableListOf(message)
                    } else {
                        map[message.groupId] = mutableListOf()
                        map[message.groupId]?.apply {
                            add(message)
                            sortByDescending { it.sendTime }
                        }
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

    fun waitForPMMessages() {
        coroutineScope.launch(Dispatchers.IO) {
            while (!isConnectionErrorEnabled) {
                try {
                    val message = pmSocket.receive(sid)
                    messages = updateMessagesState(message)
                    isConnectionErrorEnabled = false
                } catch (e: MyServerException) {
                    e.printStackTrace()
                    isConnectionErrorEnabled = true
                }
            }
        }
    }

    fun waitForGMMessages() {
        coroutineScope.launch(Dispatchers.IO) {
            while (!isConnectionErrorEnabled) {
                try {
                    val message = gmSocket.receive(sid)
                    val map = HashMap(messages)
                    if (!messages.containsKey(message.groupId)) {
                        map[message.groupId] = mutableListOf(message)
                    } else {
                        val list = map[message.groupId]
                        list?.apply {
                            add(message)
                            sortByDescending { it.sendTime }
                        }
                        map[message.groupId] = list
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
        getAllPMMessages()
        getAllGMMessages()
        delay(500)
        waitForPMMessages()
        waitForGMMessages()
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
                                onClick = { navigation(HomeWrapperRoutes.NewGroup) },
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
                            onClick = {
                                if (messageEntry.value.first() is PMResponse) {
                                    val messagesFromThisUser = messageEntry.value.filterIsInstance<PMResponse>()
                                    navigation(
                                        HomeWrapperRoutes.PrivateMessage(
                                            messagesFromThisUser.toList(),
                                            messageEntry.key
                                        )
                                    )
                                } else {
                                    val messagesFromThisGroup = messageEntry.value.filterIsInstance<GMResponse>()
                                    navigation(
                                        HomeWrapperRoutes.GroupMessage(
                                            messagesFromThisGroup.toList(),
                                            messageEntry.key
                                        )
                                    )
                                }
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