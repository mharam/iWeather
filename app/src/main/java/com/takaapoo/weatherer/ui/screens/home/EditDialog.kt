package com.takaapoo.weatherer.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.ui.theme.WeathererTheme

@Composable
fun EditDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onConfirmation: () -> Unit,
    locationName: String,
    onLocationNameChange: (String) -> Unit,
    nameAlreadyExists: Boolean
){
    Dialog(
        onDismissRequest = onDismiss
    ){
        Card(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ){
                Icon(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    painter = painterResource(R.drawable.edit_icon),
                    contentDescription = null
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Edit Location Name",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Choose a new name for this location",
                    modifier = Modifier.align(Alignment.Start),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Default
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = locationName,
                    onValueChange = {
                        onLocationNameChange(it)
                    },
                    label = { Text(text = "Location Name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameAlreadyExists,
                    supportingText = {
                        if (nameAlreadyExists){
                            Text("Name Already Exists!")
                        }
                    },
                    trailingIcon = {
                        if (nameAlreadyExists){
                            Icon(imageVector = Icons.Filled.Warning, contentDescription = null)
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { if (locationName.isNotEmpty()) onConfirmation() }
                    )
                )
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { onDismiss() },
                    ) {
                        Text(
                            text = "Dismiss",
                            fontFamily = FontFamily.Default
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(
                        onClick = { onConfirmation() },
                        enabled = locationName.isNotEmpty()
                    ) {
                        Text(
                            text = "Change Name",
                            fontFamily = FontFamily.Default
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditDialogPreview() {
    WeathererTheme {
        EditDialog(
            onDismiss = {},
            onConfirmation = {},
            locationName = "Brussels",
            onLocationNameChange = {_ ->},
            nameAlreadyExists = true
        )
    }
}