package com.takaapoo.weatherer.ui.screens.add_location.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import com.takaapoo.weatherer.R

@Composable
fun HelpDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
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
                painter = painterResource(R.drawable.add_location_24px),
                contentDescription = null
            )
        },
        title = {
            Text(text = "Add location")
        },
        text = {
            Text(
                text = stringResource(R.string.add_location_help_text),
                fontFamily = FontFamily.Default
            )
        },
    )
}