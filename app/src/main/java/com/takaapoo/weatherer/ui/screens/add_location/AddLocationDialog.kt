package com.takaapoo.weatherer.ui.screens.add_location

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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.maps.model.LatLng
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.ui.theme.Cmu
import com.takaapoo.weatherer.ui.theme.WeathererTheme
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
fun AddLocationDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    latLng: LatLng,
    locationName: String,
    onUpdateLocationName: (String) -> Unit,
    nameAlreadyExists: Boolean
) {
    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(
            dismissOnClickOutside = true,
            dismissOnBackPress = true
        )
    ){
        Card(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp)
        ){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ){
                Icon(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    painter = painterResource(R.drawable.add_location_24px),
                    contentDescription = null
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Location Name",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Name this location",
                    modifier = Modifier.align(Alignment.Start),
                    fontFamily = FontFamily.Default,
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            append("Latitude: ")
                        }
                        append("${latLng.latitude.roundTo(decimalDigits = 3)}")
                    },
                    modifier = Modifier.align(Alignment.Start),
                    fontFamily = Cmu,
                    fontSize = 16.sp
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            append("Longitude: ")
                        }
                        append("${latLng.longitude.roundTo(decimalDigits = 3)}")
                    },
                    modifier = Modifier.align(Alignment.Start),
                    fontFamily = Cmu,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = locationName,
                    onValueChange = {
                        onUpdateLocationName(it)
                    },
                    label = { Text(
                        text = "Location Name",
                        fontFamily = FontFamily.Default
                    ) },
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
                        onClick = { onDismissRequest() },
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
                            text = "Add Location",
                            fontFamily = FontFamily.Default
                        )
                    }
                }
            }
        }
    }
}

fun Double.roundTo(decimalDigits: Int): Double {
    val multiplier = 10.0.pow(decimalDigits)
    return (this * multiplier).roundToInt() / multiplier
}

@Preview(showBackground = true)
@Composable
fun AddLocationDialogPreview() {
    WeathererTheme {
        AddLocationDialog(
            onDismissRequest = {},
            onConfirmation = {},
            latLng = LatLng(35.44879, 51.4478214),
            locationName = "",
            onUpdateLocationName = {},
            nameAlreadyExists = true
        )
    }
}