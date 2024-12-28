package com.takaapoo.weatherer.ui.screens.add_location

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreInterceptKeyBeforeSoftKeyboard
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.takaapoo.weatherer.ui.theme.Gray40
import com.takaapoo.weatherer.ui.theme.WeathererTheme
import com.takaapoo.weatherer.ui.theme.customColorScheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchBarAdam(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    active: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    hasResultTable: Boolean = true,
    permanentDrawer: Boolean = false,
    placeholder: @Composable (() -> Unit)?,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    standardHeight: Dp = 56.dp,
    shape: Shape = RoundedCornerShape(standardHeight / 2),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onBackPressed: () -> Unit,
    content: @Composable (ColumnScope.() -> Unit)
){
    val cardElevation by animateDpAsState(
        targetValue = if (active) 0.dp else 4.dp,
        label = "cardElevation"
    )
    val cardHeight by animateDpAsState(
        targetValue = if (active && hasResultTable) 800.dp else standardHeight,
        label = "cardHeight"
    )
    Card(
        modifier = modifier
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .height(cardHeight)
            .shadow(
                shape = shape,
                elevation = cardElevation
            ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.customColorScheme.searchbarSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ){
        MyOutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(standardHeight)
//                .wrapContentHeight(
//                    align = Alignment.CenterVertically,
//                    unbounded = true
//                )
                .onPreInterceptKeyBeforeSoftKeyboard { keyEvent ->
                    if (keyEvent.key.keyCode == 17179869184) {
                        onBackPressed()
                    }
                    true
                },
            enabled = enabled,
            label = null,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            textStyle = MaterialTheme.typography.titleSmall,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { onSearch(query) }
            ),
            singleLine = true,
            maxLines = 1,
            minLines = 1,
            interactionSource = interactionSource,
            shape = shape,
            colors = OutlinedTextFieldDefaults.colors(
//                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = Color.Transparent,
//                focusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            contentPadding = if (!permanentDrawer) 8.dp else 16.dp
        )
        Spacer(Modifier.height(8.dp))
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            content()
        }
    }
}



@Preview(showBackground = true)
@Composable
fun HomeScreenPreview(modifier: Modifier = Modifier) {
    val height = 48.dp
    WeathererTheme {
        SearchBarAdam(
            query = "",
            onQueryChange = {},
            onSearch = {},
            active = false,
            placeholder = {
                Text(
                    modifier = Modifier
                        .wrapContentHeight(
                            align = Alignment.CenterVertically,
                            unbounded = true
                        ),
                    style = MaterialTheme.typography.titleSmall,
                    color = Gray40,
                    text = "Search here!"
                )
            },
            leadingIcon = null,
            trailingIcon = null,
            standardHeight = height,
            shape = RoundedCornerShape(height / 2),
            onBackPressed = {},
            content = {}
        )
    }
}