package com.takaapoo.weatherer.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.ui.screens.detail.DialogMultiChoiceRow
import com.takaapoo.weatherer.ui.theme.Gray30


@Composable
fun ChooseLocationDialog(
    modifier: Modifier = Modifier,
    locationList: List<Location>,
    locationId: Int,
    onDismiss: () -> Unit,
    onConfirmation: (id: Int) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss){
                Text(
                    text = "Ok",
                    fontFamily = FontFamily.Default
                )
            }
        },
        modifier = modifier,
        icon = {
            Icon(
                painter = painterResource(R.drawable.location_on_24px),
                contentDescription = null
            )
        },
        title = {
            Text(text = "Location")
        },
        text = {
            Column {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(color = Gray30)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(
                        items = locationList,
                        key = { it.id }
                    ) {
                        DialogMultiChoiceRow(
                            text = it.name,
                            isSelected = it.id == locationId,
                            onSelect = { onConfirmation(it.id) }
                        )
                    }
                }
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(color = Gray30)
                )
            }

        }
    )
}
