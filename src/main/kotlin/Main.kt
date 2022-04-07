import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import models.Session
import pages.Auth
import pages.HomeWrapper

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "messenger") {

        var userId: String? by remember {
            mutableStateOf(null)
        }
        var sid: String? by remember {
            mutableStateOf(null)
        }
        val onAuthComplete: (Session) -> Unit = {
            userId = it.id
            sid = it.sid
        }

        val onLogOut: () -> Unit = {
            userId = null
        }

        if (userId == null) {
            Auth(onAuthComplete = onAuthComplete)
        } else {
            sid?.let {
                HomeWrapper(it, userId!!, onLogOut)
            }
        }
    }
}