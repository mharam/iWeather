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
fun DonateDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onConfirmation: () -> Unit,
){
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirmation){
                Text(
                    text = "Donate",
                    fontFamily = FontFamily.Default
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss){
                Text(
                    text = "Later",
                    fontFamily = FontFamily.Default
                )
            }
        },
        modifier = modifier,
        icon = {
            Icon(
                painter = painterResource(R.drawable.donate),
                contentDescription = null
            )
        },
        title = {
            Text(text = "Donate")
        },
        text = {
            Text(
                text = stringResource(R.string.add_location_donate_text),
                fontFamily = FontFamily.Default
            )
        }
    )
}