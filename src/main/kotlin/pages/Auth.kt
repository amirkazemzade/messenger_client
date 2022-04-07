package pages

import MyServerException
import RequestSocket
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import models.Session

@Composable
@Preview
fun Auth(modifier: Modifier = Modifier, onAuthComplete: (Session) -> Unit) {

    val coroutineScope = rememberCoroutineScope()

    var username by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }

    var isPasswordHidden by remember {
        mutableStateOf(true)
    }

    var isLoginState by remember {
        mutableStateOf(true)
    }

    var isErrorEnable by remember {
        mutableStateOf(false)
    }

    var errorText by remember {
        mutableStateOf("")
    }

    fun login(username: String, password: String) {
        coroutineScope.launch(Dispatchers.IO) {
            val socket = RequestSocket()
            try {
                val session = socket.login(username, password)
                onAuthComplete(session)
            } catch (e: MyServerException) {
                isErrorEnable = true
                errorText = if (e.message != null) e.message!! else ""
            }
        }
    }

    fun createUser(username: String, password: String) {
        coroutineScope.launch(Dispatchers.IO) {
            val socket = RequestSocket()
            try {
                socket.createUser(username, password)
                login(username, password)
            } catch (e: MyServerException) {
                isErrorEnable = true
                errorText = if (e.message != null) e.message!! else ""
            }
        }
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        if (isLoginState) Text("Login")
                        else Text("Sign Up")
                    },
                    actions = {
                        Button(
                            onClick = {
                                isLoginState = !isLoginState
                            },
                            colors = ButtonDefaults.outlinedButtonColors()
                        ) {
                            if (isLoginState) Text("Sign Up")
                            else Text("Login")
                        }
                    }
                )
            },
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = modifier.fillMaxWidth()
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
                        label = {
                            Text("Username")
                        },
                        onValueChange = {
                            username = it
                        },
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = password,
                        label = {
                            Text("Password")
                        },
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (isPasswordHidden) PasswordVisualTransformation() else VisualTransformation.None,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(
                                onClick = { isPasswordHidden = !isPasswordHidden },
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = "Show/Hide password toggle")
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (isErrorEnable) Text(errorText, color = Color.Red)
                    else Text("", color = Color.Transparent)
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            if (isLoginState) login(username, password)
                            else {
                                if (password.length < 6) {
                                    isErrorEnable = true
                                    errorText = "Password must be 6 characters or longer!"
                                } else
                                    createUser(username, password)
                            }
                        },
                        modifier = Modifier
                            .wrapContentWidth()
                            .fillMaxWidth()
                    ) {
                        if (isLoginState) Text("Login")
                        else Text("Sign Up")
                    }
                }
            }
        }
    }
}
