package com.takaapoo.weatherer.ui.screens.add_location

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.LatLng
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.MapState
import com.takaapoo.weatherer.ui.theme.Gray40
import com.takaapoo.weatherer.ui.theme.Lato
import com.takaapoo.weatherer.ui.theme.customColorScheme

@Composable
fun MapTopBar(
    mapState: MapState,
    onSearchQueryChange: (query: String, getLocation: Boolean) -> Unit,
    navigateUp: () -> Unit,
    goToLocation: (latLng: LatLng, boundingBox: DoubleArray?) -> Unit
) {
    val searchbarInteractionSource = remember { MutableInteractionSource() }
    val searchbarIsFocused by searchbarInteractionSource.collectIsFocusedAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        val focusManager = LocalFocusManager.current
        SearchBarAdam(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(start = 8.dp, end = 8.dp, top = 4.dp),
            query = mapState.searchQuery,
            onQueryChange = { onSearchQueryChange(it, true) },
            placeholder = { Text(
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.customColorScheme.lowEmphasisText,
                text = "Search here!"
            ) },
            leadingIcon = {
                IconButton(
                    onClick = {
                        if (searchbarIsFocused) {
                            focusManager.clearFocus()
                            onSearchQueryChange("", true)
                        } else {
                            navigateUp()
                        }
                    }
                ){
                    if (searchbarIsFocused)
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    else
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                }
            },
            trailingIcon = { if (mapState.searchQuery.isNotEmpty())
                IconButton(
                    onClick = {
                        onSearchQueryChange("", true)
                    }
                ){
                    Icon(
                        painter = painterResource(R.drawable.clear_24px),
                        contentDescription = "Clear"
                    )
                }
            else
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                ) },
            active = mapState.locations.isNotEmpty(),
            onSearch = {
                if (mapState.locations.isNotEmpty()){
                    val firstLocation = mapState.locations[0]
                    val title = firstLocation.properties.title(mapState.searchQuery)
                    focusManager.clearFocus()
                    onSearchQueryChange(title, false)
                    keyboardController?.hide()
                    goToLocation(
                        LatLng(
                            firstLocation.geometry.coordinates[1],
                            firstLocation.geometry.coordinates[0]
                        ),
                        firstLocation.boundingBox
                    )
                }
            },
            standardHeight = 48.dp,
            onBackPressed = {
                focusManager.clearFocus()
                onSearchQueryChange("", true)
                keyboardController?.hide()
            },
            interactionSource = searchbarInteractionSource
        ){
            mapState.locations.forEach { thisLocation ->
                val title = thisLocation.properties.title(mapState.searchQuery)
                ListItemAdam(
                    headlineContent = {
                        Text(
                            modifier = Modifier.padding(bottom = 4.dp),
                            text = title,
                            fontWeight = FontWeight(500),
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    supportingContent = {
                        Text(
                            text = thisLocation.properties.formatted,
                            color = Gray40,
                            fontWeight = FontWeight(470),
                            fontSize = 14.sp,
                            fontFamily = Lato,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        ) },
                    leadingContent = { Icon(Icons.Outlined.LocationOn, contentDescription = null) },
                    trailingContent = {
                        IconButton(
                            onClick = {onSearchQueryChange(title, true)}
                        ){
                            Icon(
                                painter = painterResource(R.drawable.arrow_insert_24px),
                                contentDescription = "Insert"
                            )
                        } },
                    modifier = Modifier.fillMaxWidth(),
                ){
                    onSearchQueryChange(title, false)
                    goToLocation(
                        LatLng(
                            thisLocation.geometry.coordinates[1],
                            thisLocation.geometry.coordinates[0]
                        ),
                        thisLocation.boundingBox
                    )
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            }
        }
    }
}