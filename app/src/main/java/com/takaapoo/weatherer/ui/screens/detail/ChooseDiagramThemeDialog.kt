package com.takaapoo.weatherer.ui.screens.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartTheme
import com.takaapoo.weatherer.ui.theme.WeathererTheme


@Composable
fun ChooseDiagramThemeDialog(
    modifier: Modifier = Modifier,
    onConfirmation: () -> Unit,
    chartTheme: ChartTheme,
    onSelectTheme: (theme: ChartTheme) -> Unit
) {
    AlertDialog(
        onDismissRequest = onConfirmation,
        confirmButton = {
            TextButton(onClick = onConfirmation){ Text("Ok") }
        },
        modifier = modifier,
        icon = {
            Icon(
                painter = painterResource(R.drawable.theme),
                contentDescription = null
            )
        },
        title = {
            Text(text = stringResource(id = R.string.choose_diagram_theme))
        },
        text = {
            Column(
                modifier = Modifier
            ) {
                ChartTheme.entries.forEach { theme ->
                    ThemeDialogRow(
                        theme = theme,
                        isSelected = theme == chartTheme,
                        onSelectRow = onSelectTheme
                    )
                }
            }
        }
    )
}

@Composable
fun ThemeDialogRow(
    modifier: Modifier = Modifier,
    theme: ChartTheme,
    isSelected: Boolean,
    onSelectRow: (theme: ChartTheme) -> Unit
) {
    Row(
        modifier = modifier
            .clickable { onSelectRow(theme) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { onSelectRow(theme) }
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = stringResource(id = theme.nameId), modifier = Modifier.padding(end = 16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun ChooseDiagramThemeDialogPreview() {
    WeathererTheme {
        ChooseDiagramThemeDialog(
            onConfirmation = {},
            chartTheme = ChartTheme.APPTHEME,
            onSelectTheme = {}
        )
    }
}