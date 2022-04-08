package pages

import GMReceiverSocket
import MyServerException
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
@Preview
fun GroupUsers(
    navigation: (HomeWrapperRoutes) -> Unit,
    sid: String,
    groupId: String
) {
    val coroutineScope = rememberCoroutineScope()

    val scaffoldState = rememberScaffoldState()

    val gmSocket by remember {
        mutableStateOf(GMReceiverSocket())
    }

    var users: List<String> by remember {
        mutableStateOf(listOf())
    }

    LaunchedEffect(scaffoldState) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                users = gmSocket.getGroupUsers(groupId)
            } catch (e: MyServerException) {
                e.printStackTrace()
            }
        }
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Users of $groupId") },
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
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(users) { user ->
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { navigation(HomeWrapperRoutes.PrivateMessage(destinationId = user)) },
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Box(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(user)
                        }
                    }
                }
            }
        }
    }
}