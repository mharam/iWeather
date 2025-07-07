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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.AppTheme
import com.takaapoo.weatherer.ui.screens.detail.DialogMultiChoiceRow
import com.takaapoo.weatherer.ui.theme.WeathererTheme


@Composable
fun ChooseThemeDialog(
    modifier: Modifier = Modifier,
    appTheme: AppTheme,
    onDismiss: () -> Unit,
    onConfirmation: (theme: AppTheme) -> Unit,
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
                painter = painterResource(R.drawable.theme),
                contentDescription = null
            )
        },
        title = {
            Text(text = stringResource(id = R.string.app_theme))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppTheme.entries.forEach { theme ->
                    DialogMultiChoiceRow(
                        text = stringResource(theme.description),
                        isSelected = theme == appTheme,
                        onSelect = { onConfirmation(theme) }
                    )
                }
            }
        }
    )
}



@Preview(showBackground = true)
@Composable
fun ChooseThemeDialogPreview() {
    WeathererTheme {
        ChooseThemeDialog(
            appTheme = AppTheme.SYSTEM,
            onDismiss = {},
            onConfirmation = {}
        )
    }
}
