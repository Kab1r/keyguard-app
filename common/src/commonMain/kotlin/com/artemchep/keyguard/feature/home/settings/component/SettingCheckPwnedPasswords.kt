package com.artemchep.keyguard.feature.home.settings.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import arrow.core.partially1
import com.artemchep.keyguard.common.io.launchIn
import com.artemchep.keyguard.common.usecase.GetCheckPwnedPasswords
import com.artemchep.keyguard.common.usecase.PutCheckPwnedPasswords
import com.artemchep.keyguard.common.usecase.WindowCoroutineScope
import com.artemchep.keyguard.res.Res
import com.artemchep.keyguard.ui.FlatItem
import com.artemchep.keyguard.ui.poweredby.PoweredByHaveibeenpwned
import com.artemchep.keyguard.ui.theme.Dimens
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.map
import org.kodein.di.DirectDI
import org.kodein.di.instance

fun settingCheckPwnedPasswordsProvider(
    directDI: DirectDI,
) = settingCheckPwnedPasswordsProvider(
    getCheckPwnedPasswords = directDI.instance(),
    putCheckPwnedPasswords = directDI.instance(),
    windowCoroutineScope = directDI.instance(),
)

fun settingCheckPwnedPasswordsProvider(
    getCheckPwnedPasswords: GetCheckPwnedPasswords,
    putCheckPwnedPasswords: PutCheckPwnedPasswords,
    windowCoroutineScope: WindowCoroutineScope,
): SettingComponent = getCheckPwnedPasswords().map { checkPwnedPasswords ->
    val onCheckedChange = { shouldCheckPwnedPasswords: Boolean ->
        putCheckPwnedPasswords(shouldCheckPwnedPasswords)
            .launchIn(windowCoroutineScope)
        Unit
    }

    SettingIi {
        SettingCheckPwnedPasswords(
            checked = checkPwnedPasswords,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun SettingCheckPwnedPasswords(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
) {
    Column {
        FlatItem(
            trailing = {
                CompositionLocalProvider(
                    LocalMinimumInteractiveComponentEnforcement provides false,
                ) {
                    Switch(
                        checked = checked,
                        enabled = onCheckedChange != null,
                        onCheckedChange = onCheckedChange,
                    )
                }
            },
            title = {
                Text(
                    text = stringResource(Res.strings.pref_item_check_pwned_passwords_title),
                )
            },
            text = {
                val text = stringResource(Res.strings.watchtower_item_pwned_passwords_text)
                Text(text)
            },
            onClick = onCheckedChange?.partially1(!checked),
        )
        PoweredByHaveibeenpwned(
            modifier = Modifier
                .padding(horizontal = Dimens.horizontalPadding),
        )
    }
}
