package com.takaapoo.weatherer.ui.screens.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
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
            TextButton(onClick = onConfirmation){
                Text(
                    text = "Ok",
                    fontFamily = FontFamily.Default
                )
            }
        },
        modifier = modifier,
        icon = {
            Icon(
                painter = painterResource(R.drawable.theme),
                contentDescription = null
            )
        },
        title = {
            Text(text = stringResource(id = R.string.diagram_theme))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChartTheme.entries.forEach { theme ->
                    DialogMultiChoiceRow(
                        text = stringResource(id = theme.nameId),
                        isSelected = theme == chartTheme,
                        onSelect = { onSelectTheme(theme) }
                    )
                }
            }
        }
    )
}

@Composable
fun DialogMultiChoiceRow(
    modifier: Modifier = Modifier,
    text: String? = null,
    annotatedText: AnnotatedString? = null,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(percent = 50))
            .clickable { onSelect() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = isSelected,
            onClick =  onSelect
        )
        Spacer(modifier = Modifier.width(24.dp))
        if (text != null)
            Text(
                text = text,
                fontFamily = FontFamily.Default
            )
        else
            Text(
                text = annotatedText!!,
                fontFamily = FontFamily.Default
            )
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