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
import com.takaapoo.weatherer.ui.screens.detail.DialogMultiChoiceRow
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartGrids

@Composable
fun ChooseDiagramGridDialog(
    modifier: Modifier = Modifier,
    chartGrid: ChartGrids,
    onDismiss: () -> Unit,
    onConfirmation: (grid: ChartGrids) -> Unit,
){
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
                painter = painterResource(R.drawable.grid_show),
                contentDescription = null
            )
        },
        title = {
            Text(text = stringResource(id = R.string.diagram_grid))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChartGrids.entries.forEach { grid ->
                    DialogMultiChoiceRow(
                        text = stringResource(grid.nameId),
                        isSelected = grid == chartGrid,
                        onSelect = { onConfirmation(grid) }
                    )
                }
            }
        }
    )
}