package com.artemchep.keyguard.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Attachment
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.CopyAll
import androidx.compose.material.icons.outlined.DataArray
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lens
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.material.icons.outlined.ShortText
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.FeatherIcons
import compose.icons.feathericons.Eye
import compose.icons.feathericons.Globe

val Icons.Outlined.KeyguardWebsite
    get() = FeatherIcons.Globe

val Icons.Outlined.KeyguardView
    get() = FeatherIcons.Eye

val Icons.Outlined.KeyguardTwoFa
    get() = Numbers

val Icons.Outlined.KeyguardPasskey
    get() = Key

val Icons.Outlined.KeyguardNote
    get() = StickyNote2

val Icons.Outlined.KeyguardAttachment
    get() = Attachment

val Icons.Outlined.KeyguardFavourite
    get() = Star

val Icons.Outlined.KeyguardFavouriteOutline
    get() = StarBorder

val Icons.Outlined.KeyguardOrganization
    get() = AccountTree

val Icons.Outlined.KeyguardCollection
    get() = CollectionsBookmark

val Icons.Outlined.KeyguardCipher
    get() = List

val Icons.Outlined.KeyguardLargeType
    get() = Lens

val Icons.Outlined.KeyguardPremium
    get() = Star

val Icons.Outlined.KeyguardWordlist
    get() = LibraryBooks

val Icons.Outlined.KeyguardCipherFilter
    get() = FilterList

val Icons.Outlined.KeyguardPwnedPassword
    get() = DataArray

val Icons.Outlined.KeyguardReusedPassword
    get() = Recycling

val Icons.Outlined.KeyguardPwnedWebsites
    get() = KeyguardWebsite

val Icons.Outlined.KeyguardUnsecureWebsites
    get() = KeyguardWebsite

val Icons.Outlined.KeyguardDuplicateWebsites
    get() = KeyguardWebsite

val Icons.Outlined.KeyguardDuplicateItems
    get() = CopyAll

val Icons.Outlined.KeyguardIncompleteItems
    get() = ShortText

val Icons.Outlined.KeyguardExpiringItems
    get() = Timer

val Icons.Outlined.KeyguardTrashedItems
    get() = Delete

val Icons.Outlined.KeyguardFailedItems
    get() = ErrorOutline

val Icons.Outlined.KeyguardPendingSyncItems
    get() = CloudOff

val Icons.Outlined.KeyguardAuthReprompt
    get() = Lock

val Icons.Outlined.KeyguardIgnoredAlerts
    get() = NotificationsOff

val Icons.Stub: ImageVector
    get() {
        if (_stub != null) {
            return _stub!!
        }
        _stub = materialIcon(name = "Stub") {
            materialPath {
                close()
            }
        }
        return _stub!!
    }

private var _stub: ImageVector? = null
