package com.artemchep.keyguard.feature.send.action

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Share
import com.artemchep.keyguard.common.model.DSend
import com.artemchep.keyguard.feature.navigation.NavigationIntent
import com.artemchep.keyguard.feature.navigation.state.TranslatorScope
import com.artemchep.keyguard.feature.send.add.SendAddRoute
import com.artemchep.keyguard.res.Res
import com.artemchep.keyguard.ui.FlatItemAction
import com.artemchep.keyguard.ui.icons.ChevronIcon
import com.artemchep.keyguard.ui.icons.icon

fun createSendActionOrNull(
    translator: TranslatorScope,
    name: String = "",
    text: String,
    navigate: (NavigationIntent) -> Unit,
) = createSendAction(
    translator = translator,
    name = name,
    text = text,
    navigate = navigate,
)

fun createSendAction(
    translator: TranslatorScope,
    name: String,
    text: String,
    navigate: (NavigationIntent) -> Unit,
) = FlatItemAction(
    leading = icon(Icons.AutoMirrored.Outlined.Send),
    title = translator.translate(Res.strings.text_action_send_title),
    onClick = {
        val args = SendAddRoute.Args(
            type = DSend.Type.Text,
            name = name,
            text = text,
        )
        val route = SendAddRoute(args)
        val intent = NavigationIntent.NavigateToRoute(route)
        navigate(intent)
    },
)

fun createShareAction(
    translator: TranslatorScope,
    text: String,
    navigate: (NavigationIntent) -> Unit,
) = FlatItemAction(
    leading = icon(Icons.Outlined.Share),
    trailing = {
        ChevronIcon()
    },
    title = translator.translate(Res.strings.text_action_share_with_title),
    onClick = {
        val intent = NavigationIntent.NavigateToShare(
            text = text,
        )
        navigate(intent)
    },
)
