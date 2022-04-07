package pages

import MyServerException
import PMSenderSocket
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
fun NewPrivateMessage(
    navigation: (HomeWrapperRoutes) -> Unit,
    sid: String
) {
    val coroutineScope = rememberCoroutineScope()

    val senderSocket by remember {
        mutableStateOf(PMSenderSocket())
    }

    var username by remember {
        mutableStateOf("")
    }

    var isUsernameEnabled by remember {
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
        navigation(HomeWrapperRoutes.PrivateMessage(destinationId = username))
    } else
        MaterialTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("New Private Message") },
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
                            value = username,
                            label = { Text("Username") },
                            onValueChange = { username = it },
                            singleLine = true,
                            enabled = isUsernameEnabled
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        if (isErrorEnable) Text(errorText, color = Color.Red)
                        else Text("", color = Color.Transparent)
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    try {
                                        isUsernameEnabled = false
                                        senderSocket.connectToAnotherUser(username, sid)
                                        loaded = true
                                        isUsernameEnabled = true
                                    } catch (e: MyServerException) {
                                        isUsernameEnabled = true
                                        e.printStackTrace()
                                        isErrorEnable = true
                                        errorText = if (e.message != null) e.message!! else "Something went wrong"
                                    }
                                }
                            },
                            modifier = Modifier
                                .wrapContentWidth()
                                .fillMaxWidth()
                        ) {
                            Text("Start Messaging")
                        }
                    }
                }
            }
        }
}