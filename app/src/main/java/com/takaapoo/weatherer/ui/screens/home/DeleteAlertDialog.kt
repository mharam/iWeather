package com.takaapoo.weatherer.ui.screens.home

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.ui.theme.WeathererTheme

@Composable
fun DeleteAlertDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onConfirmation: () -> Unit,
    locationName: String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirmation){
                Text(
                    text = "Delete",
                    fontFamily = FontFamily.Default
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss){
                Text(
                    text = "Cancel",
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
            Text(text = locationName)
        },
        text = {
            Text(
                text = "Are you sure you want to omit this location from the list?",
                fontFamily = FontFamily.Default
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DeleteAlertDialogPreview() {
    WeathererTheme {
        DeleteAlertDialog(
            onDismiss = {},
            onConfirmation = {},
            locationName = "Beijing"
        )
    }
}


