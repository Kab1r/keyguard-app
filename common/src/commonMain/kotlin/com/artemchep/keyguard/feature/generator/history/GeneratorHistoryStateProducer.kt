package com.artemchep.keyguard.feature.generator.history

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.Composable
import arrow.core.partially1
import com.artemchep.keyguard.common.io.launchIn
import com.artemchep.keyguard.common.model.DGeneratorHistory
import com.artemchep.keyguard.common.model.Loadable
import com.artemchep.keyguard.common.service.clipboard.ClipboardService
import com.artemchep.keyguard.common.usecase.CopyText
import com.artemchep.keyguard.common.usecase.DateFormatter
import com.artemchep.keyguard.common.usecase.GetGeneratorHistory
import com.artemchep.keyguard.common.usecase.RemoveGeneratorHistory
import com.artemchep.keyguard.common.usecase.RemoveGeneratorHistoryById
import com.artemchep.keyguard.common.util.flow.persistingStateIn
import com.artemchep.keyguard.feature.attachments.SelectableItemState
import com.artemchep.keyguard.feature.attachments.SelectableItemStateRaw
import com.artemchep.keyguard.feature.auth.common.util.REGEX_EMAIL
import com.artemchep.keyguard.feature.confirmation.createConfirmationDialogIntent
import com.artemchep.keyguard.feature.decorator.ItemDecoratorDate
import com.artemchep.keyguard.feature.largetype.LargeTypeRoute
import com.artemchep.keyguard.feature.navigation.state.copy
import com.artemchep.keyguard.feature.navigation.state.produceScreenState
import com.artemchep.keyguard.feature.passwordleak.PasswordLeakRoute
import com.artemchep.keyguard.res.Res
import com.artemchep.keyguard.ui.FlatItemAction
import com.artemchep.keyguard.ui.Selection
import com.artemchep.keyguard.ui.buildContextItems
import com.artemchep.keyguard.ui.icons.icon
import com.artemchep.keyguard.ui.selection.selectionHandle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import org.kodein.di.compose.localDI
import org.kodein.di.direct
import org.kodein.di.instance

@Composable
fun produceGeneratorHistoryState() = with(localDI().direct) {
    produceGeneratorHistoryState(
        getGeneratorHistory = instance(),
        removeGeneratorHistory = instance(),
        removeGeneratorHistoryById = instance(),
        dateFormatter = instance(),
        clipboardService = instance(),
    )
}

@Composable
fun produceGeneratorHistoryState(
    getGeneratorHistory: GetGeneratorHistory,
    removeGeneratorHistory: RemoveGeneratorHistory,
    removeGeneratorHistoryById: RemoveGeneratorHistoryById,
    dateFormatter: DateFormatter,
    clipboardService: ClipboardService,
): Loadable<GeneratorHistoryState> = produceScreenState(
    initial = Loadable.Loading,
    key = "generator_history",
    args = arrayOf(
        getGeneratorHistory,
        removeGeneratorHistory,
        removeGeneratorHistoryById,
        dateFormatter,
        clipboardService,
    ),
) {
    val selectionHandle = selectionHandle("selection")
    val copyFactory = copy(clipboardService)

    val itemsRawFlow = getGeneratorHistory()
        .shareInScreenScope()
    // Automatically de-select items
    // that do not exist.
    combine(
        itemsRawFlow,
        selectionHandle.idsFlow,
    ) { items, selectedItemIds ->
        val newSelectedItemIds = selectedItemIds
            .asSequence()
            .filter { itemId ->
                items.any { it.id == itemId }
            }
            .toSet()
        newSelectedItemIds.takeIf { it.size < selectedItemIds.size }
    }
        .filterNotNull()
        .onEach { ids -> selectionHandle.setSelection(ids) }
        .launchIn(screenScope)

    fun onDeleteByItems(
        items: List<DGeneratorHistory>,
    ) {
        val title = if (items.size > 1) {
            translate(Res.strings.generatorhistory_delete_many_confirmation_title)
        } else {
            translate(Res.strings.generatorhistory_delete_one_confirmation_title)
        }
        val message = items
            .joinToString(separator = "\n") { it.value }
        val intent = createConfirmationDialogIntent(
            icon = icon(Icons.Outlined.Delete),
            title = title,
            message = message,
        ) {
            val ids = items
                .mapNotNull { it.id }
                .toSet()
            removeGeneratorHistoryById(ids)
                .launchIn(appScope)
        }
        navigate(intent)
    }

    fun onDeleteAll() {
        val intent = createConfirmationDialogIntent(
            icon = icon(Icons.Outlined.Delete),
            title = translate(Res.strings.generatorhistory_clear_history_confirmation_title),
            message = translate(Res.strings.generatorhistory_clear_history_confirmation_text),
        ) {
            removeGeneratorHistory()
                .launchIn(appScope)
        }
        navigate(intent)
    }

    val selectionFlow = combine(
        itemsRawFlow,
        selectionHandle.idsFlow,
    ) { items, selectedItemIds ->
        val selectedItems = items
            .filter { it.id in selectedItemIds }
        items to selectedItems
    }
        .map { (allItems, selectedItems) ->
            if (selectedItems.isEmpty()) {
                return@map null
            }

            val actions = buildContextItems {
                section {
                    this += FlatItemAction(
                        leading = icon(Icons.Outlined.Delete),
                        title = translate(Res.strings.remove_from_history),
                        onClick = ::onDeleteByItems
                            .partially1(selectedItems),
                    )
                }
            }
            Selection(
                count = selectedItems.size,
                actions = actions,
                onSelectAll = if (selectedItems.size < allItems.size) {
                    val allIds = allItems
                        .asSequence()
                        .mapNotNull { it.id }
                        .toSet()
                    selectionHandle::setSelection
                        .partially1(allIds)
                } else {
                    null
                },
                onClear = selectionHandle::clearSelection,
            )
        }

    val itemsValueFlow = itemsRawFlow
        .mapLatestScoped { items ->
            items.map { item ->
                val type = when {
                    item.isPassword && item.isUsername -> null
                    item.isPassword -> GeneratorHistoryItem.Value.Type.PASSWORD
                    item.isUsername -> kotlin.run {
                        val isEmail = REGEX_EMAIL.matches(item.value)
                        if (isEmail) {
                            if (item.isEmailRelay) {
                                GeneratorHistoryItem.Value.Type.EMAIL_RELAY
                            } else {
                                GeneratorHistoryItem.Value.Type.EMAIL
                            }
                        } else {
                            GeneratorHistoryItem.Value.Type.USERNAME
                        }
                    }

                    else -> null
                }
                val actions = buildContextItems {
                    section {
                        val (copyTitle, copyType) = when (type) {
                            GeneratorHistoryItem.Value.Type.PASSWORD ->
                                translate(Res.strings.copy_password) to CopyText.Type.PASSWORD
                            GeneratorHistoryItem.Value.Type.EMAIL,
                            GeneratorHistoryItem.Value.Type.EMAIL_RELAY ->
                                translate(Res.strings.copy_email) to CopyText.Type.EMAIL
                            GeneratorHistoryItem.Value.Type.USERNAME ->
                                translate(Res.strings.copy_username) to CopyText.Type.USERNAME
                            null -> translate(Res.strings.copy_value) to CopyText.Type.VALUE
                        }
                        this += copyFactory.FlatItemAction(
                            title = copyTitle,
                            value = item.value,
                            hidden = item.isPassword,
                            type = copyType,
                        )
                        val items = listOfNotNull(
                            item,
                        )
                        this += FlatItemAction(
                            leading = icon(Icons.Outlined.Delete),
                            title = translate(Res.strings.remove_from_history),
                            onClick = ::onDeleteByItems
                                .partially1(items),
                        )
                    }
                    section {
                        this += LargeTypeRoute.showInLargeTypeActionOrNull(
                            translator = this@produceScreenState,
                            text = item.value,
                            colorize = item.isPassword,
                            navigate = ::navigate,
                        )
                        this += LargeTypeRoute.showInLargeTypeActionAndLockOrNull(
                            translator = this@produceScreenState,
                            text = item.value,
                            colorize = item.isPassword,
                            navigate = ::navigate,
                        )
                    }
                    section {
                        // If the value type is a password, then offer to
                        // check it in the breaches.
                        if (type == GeneratorHistoryItem.Value.Type.PASSWORD) {
                            this += PasswordLeakRoute.checkBreachesPasswordAction(
                                translator = this@produceScreenState,
                                password = item.value,
                                navigate = ::navigate,
                            )
                        }
                    }
                }
                val selectableFlow = selectionHandle
                    .idsFlow
                    .map { selectedIds ->
                        SelectableItemStateRaw(
                            selecting = selectedIds.isNotEmpty(),
                            selected = item.id in selectedIds,
                        )
                    }
                    .distinctUntilChanged()
                    .map { raw ->
                        val onClick = if (raw.selecting) {
                            // lambda
                            selectionHandle::toggleSelection.partially1(item.id.orEmpty())
                        } else {
                            null
                        }
                        val onLongClick = if (raw.selecting) {
                            null
                        } else {
                            // lambda
                            selectionHandle::toggleSelection.partially1(item.id.orEmpty())
                        }
                        SelectableItemState(
                            selecting = raw.selecting,
                            selected = raw.selected,
                            onClick = onClick,
                            onLongClick = onLongClick,
                        )
                    }
                val selectableStateFlow =
                    if (items.size >= 100) {
                        val sharing = SharingStarted.WhileSubscribed(1000L)
                        selectableFlow.persistingStateIn(this, sharing)
                    } else {
                        selectableFlow.stateIn(this)
                    }
                GeneratorHistoryItem.Value(
                    id = item.id.orEmpty(),
                    title = item.value,
                    text = dateFormatter.formatDateTime(item.createdDate),
                    type = type,
                    createdDate = item.createdDate,
                    dropdown = actions,
                    selectableState = selectableStateFlow,
                )
            }
        }
    val itemsFlow = itemsValueFlow
        .map { items ->
            val decorator = ItemDecoratorDate<GeneratorHistoryItem, GeneratorHistoryItem.Value>(
                dateFormatter = dateFormatter,
                selector = { it.createdDate },
                factory = { id, text ->
                    GeneratorHistoryItem.Section(
                        id = id,
                        text = text,
                    )
                },
            )
            sequence {
                items.forEach { item ->
                    val section = decorator.getOrNull(item)
                    if (section != null) {
                        yield(section)
                    }
                    yield(item)
                }
            }.toPersistentList()
        }
    val optionsFlow = itemsRawFlow
        .map { items ->
            items.isEmpty()
        }
        .distinctUntilChanged()
        .map { isEmpty ->
            if (isEmpty) {
                persistentListOf()
            } else {
                val action = FlatItemAction(
                    leading = icon(Icons.Outlined.Delete),
                    title = translate(Res.strings.generatorhistory_clear_history_title),
                    onClick = ::onDeleteAll,
                )
                persistentListOf(action)
            }
        }
    combine(
        optionsFlow,
        selectionFlow,
        itemsFlow,
    ) { options, selection, items ->
        val state = GeneratorHistoryState(
            options = options,
            selection = selection,
            items = items.toImmutableList(),
        )
        Loadable.Ok(state)
    }
}

fun <T, R> Flow<T>.mapLatestScoped(
    block: suspend CoroutineScope.(T) -> R,
) = this
    .flatMapLatest { value ->
        flow<R> {
            coroutineScope {
                val result = block(value)
                emit(result)
                awaitCancellation()
            }
        }
    }
