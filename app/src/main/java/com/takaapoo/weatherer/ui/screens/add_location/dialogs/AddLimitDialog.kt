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
import androidx.compose.ui.window.DialogProperties
import com.takaapoo.weatherer.R

@Composable
fun AddLimitDialog(
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
                painter = painterResource(R.drawable.delete_24px),
                contentDescription = null
            )
        },
        title = {
            Text(text = "Delete locations")
        },
        text = {
            Text(
                text = stringResource(R.string.remove_locations_dialog_text),
                fontFamily = FontFamily.Default
            )
        },
        properties = DialogProperties(dismissOnClickOutside = false)
    )
}