package pages

import GMSenderSocket
import MyServerException
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
@Preview
fun NewGroup(
    navigation: (HomeWrapperRoutes) -> Unit,
    sid: String
) {
    val coroutineScope = rememberCoroutineScope()

    val senderSocket by remember {
        mutableStateOf(GMSenderSocket())
    }

    var isNewGroup by remember {
        mutableStateOf(false)
    }

    var groupId by remember {
        mutableStateOf("")
    }

    var isGroupIdEnabled by remember {
        mutableStateOf(true)
    }

    var loaded by remember {
        mutableStateOf(false)
    }

    var isErrorEnable by remember {
        mutableStateOf(false)
    }

    var errorText by remember {
        mutableStateOf("")
    }

    if (loaded) {
        navigation(HomeWrapperRoutes.GroupMessage(groupId = groupId))

    } else
        MaterialTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            if (isNewGroup)
                                Text("New Group")
                            else
                                Text("Join A Group")
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { navigation(HomeWrapperRoutes.Home) }
                            ) {
                                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back Button")
                            }
                        },
                        actions = {
                            Button(
                                onClick = { isNewGroup = !isNewGroup },
                                colors = ButtonDefaults.outlinedButtonColors()
                            ) {
                                if (isNewGroup)
                                    Text("Join a group")
                                else
                                    Text("New Group")
                            }
                        }
                    )
                }
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                            .fillMaxHeight()
                            .width(IntrinsicSize.Max)
                    ) {
                        OutlinedTextField(
                            value = groupId,
                            label = { Text("Group ID") },
                            onValueChange = { groupId = it },
                            singleLine = true,
                            enabled = isGroupIdEnabled
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        if (isErrorEnable) Text(errorText, color = Color.Red)
                        else Text("", color = Color.Transparent)
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    if (isNewGroup) {
                                        try {
                                            isGroupIdEnabled = false
                                            senderSocket.createGroup(groupId, sid)
                                            loaded = true
                                            isGroupIdEnabled = true
                                        } catch (e: MyServerException) {
                                            isGroupIdEnabled = true
                                            e.printStackTrace()
                                            isErrorEnable = true
                                            errorText = if (e.message != null) e.message!! else "Something went wrong"
                                        }
                                    } else {
                                        try {
                                            isGroupIdEnabled = false
                                            senderSocket.joinGroup(groupId, sid)
                                            loaded = true
                                            isGroupIdEnabled = true
                                        } catch (e: MyServerException) {
                                            isGroupIdEnabled = true
                                            e.printStackTrace()
                                            isErrorEnable = true
                                            errorText = if (e.message != null) e.message!! else "Something went wrong"
                                        }

                                    }
                                }
                            },
                            modifier = Modifier
                                .wrapContentWidth()
                                .fillMaxWidth()
                        ) {
                            if (isNewGroup)
                                Text("Create")
                            else
                                Text("Join")
                        }
                    }
                }
            }
        }
}