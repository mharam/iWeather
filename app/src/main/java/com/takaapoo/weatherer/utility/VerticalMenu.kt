package com.takaapoo.weatherer.utility

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SelectableDropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.takaapoo.weatherer.ui.theme.Transparent


data class VerticalMenuItem<T>(
    val value: T,
    val label: AnnotatedString,
    val leadingIcon: Painter? = null,
    val trailingLabel: String? = null,
    val enabled: Boolean = true,
    val groupId: Int,
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> VerticalMenu(
    items: List<VerticalMenuItem<T>>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
//    label: String? = null,
//    placeholder: String? = null,
    enabled: Boolean = true,
    expanded: Boolean,
    onUpdateExpanded: (Boolean) -> Unit,
    anchor: @Composable ExposedDropdownMenuBoxScope.() -> Unit,
) {
//    var expanded by remember { mutableStateOf(false) }
    if (items.isEmpty()) return
//    val selected = items.firstOrNull { it.value == selectedItem } ?: items.first()

    val itemsGrouped = items.groupBy { it.groupId }
    val groupCount = itemsGrouped.size
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (enabled) {
                onUpdateExpanded(it)
            }
        },
        modifier = modifier
    ) {

        /*OutlinedTextField(
            value = TextFieldValue(
                annotatedString = selected.label
            ),
            textStyle = TextStyle.Default.copy(
                fontFamily = FontFamily.Default
            ),
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = label?.let { { Text(text = it, fontFamily = FontFamily.Default) } },
            placeholder = placeholder?.let { { Text(text = it, fontFamily = FontFamily.Default) } },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            modifier = Modifier.height(0.dp)
                .menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    enabled = enabled
                )
        )*/
        anchor()
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                onUpdateExpanded(false)
            },
            containerColor = Transparent,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
            ) {
                itemsGrouped.entries.forEachIndexed { groupIndex, (_, menuItems) ->
                    if (groupIndex > 0) {
                        Spacer(
                            modifier = Modifier.height(
                                MenuDefaults.GroupSpacing
                            )
                        )
                    }
                    DropdownMenuGroup(
                        shapes = MenuDefaults.groupShape(
                            index = groupIndex,
                            count = groupCount
                        ),
//                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow ,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                    ) {
                        menuItems.forEachIndexed { itemIndex, item ->
                            SelectableDropdownMenuItem(
                                selected = item.value == selectedItem,
                                onClick = {
                                    onItemSelected(item.value)
                                    onUpdateExpanded(false)
                                },
                                text = {
                                    Text(
                                        text = item.label,
                                        fontFamily = FontFamily.Default,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                shapes = MenuDefaults.itemShape(
                                    index = itemIndex,
                                    count = menuItems.size
                                ),
                                colors = MenuDefaults.selectableItemColors(
                                    textColor = MaterialTheme.colorScheme.onSurface,
                                    containerColor = Transparent,
                                    selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                    leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onTertiaryContainer
                                ),
                                leadingIcon = item.leadingIcon?.let { painter ->
                                    {
                                        Icon(
                                            painter = painter,
                                            contentDescription = null,
                                        )
                                    }
                                },
                                trailingContent = item.trailingLabel?.let { text ->
                                    {
                                        MenuDefaults.DropdownMenuItemTrailingLabel {
                                            Text(text = text, fontFamily = FontFamily.Default)
                                        }
                                    }
                                },
                                enabled = item.enabled
                            )
                        }
                    }
                }
            }
        }
    }
}