package com.takaapoo.weatherer.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.HourlyDiagramTime
import com.takaapoo.weatherer.ui.screens.detail.DialogMultiChoiceRow


@Composable
fun ChooseTimeAxisDialog(
    modifier: Modifier = Modifier,
    time: HourlyDiagramTime,
    title: String? = null,
    onDismiss: () -> Unit,
    onConfirmation: (selectedTime: HourlyDiagramTime) -> Unit,
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
                painter = painterResource(R.drawable.clock_icon),
                contentDescription = null
            )
        },
        title = {
            Text(text = title ?: "Time axis clock")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HourlyDiagramTime.entries.forEach { diagramTime ->
                    DialogMultiChoiceRow(
                        text = stringResource(diagramTime.description),
                        isSelected = time == diagramTime,
                        onSelect = { onConfirmation(diagramTime) }
                    )
                }
            }
        }
    )
}
