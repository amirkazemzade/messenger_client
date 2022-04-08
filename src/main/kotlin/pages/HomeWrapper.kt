package pages

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import models.GMResponse
import models.PMResponse

@Composable
@Preview
fun HomeWrapper(sid: String, username: String, onLogOut: () -> Unit) {

    var currentRoute: HomeWrapperRoutes by rememberSaveable {
        mutableStateOf(HomeWrapperRoutes.Home)
    }

    val navigateTo: (HomeWrapperRoutes) -> Unit = { des ->
        currentRoute = des
    }

    when (currentRoute) {
        is HomeWrapperRoutes.Home -> Home(navigateTo, sid, username, onLogOut)
        is HomeWrapperRoutes.NewPrivateMessage -> NewPrivateMessage(navigateTo, sid)
        is HomeWrapperRoutes.PrivateMessage -> {
            val route = currentRoute as HomeWrapperRoutes.PrivateMessage
            PrivateMessage(navigateTo, route.messages, sid, route.destinationId)
        }
        is HomeWrapperRoutes.GroupMessage -> {
            val route = currentRoute as HomeWrapperRoutes.GroupMessage
            GroupMessage(navigateTo, route.messages, sid, username, route.groupId)
        }
    }
}

@Parcelize
sealed class HomeWrapperRoutes : Parcelable {
    object Home : HomeWrapperRoutes()
    object NewPrivateMessage : HomeWrapperRoutes()
    data class PrivateMessage(
        val messages: List<PMResponse> = listOf(), val destinationId: String
    ) : HomeWrapperRoutes()
    data class GroupMessage(
        val messages: List<GMResponse> = listOf(), val groupId: String
    ) : HomeWrapperRoutes()
}