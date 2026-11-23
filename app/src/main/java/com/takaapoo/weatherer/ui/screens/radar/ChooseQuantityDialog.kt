package com.takaapoo.weatherer.ui.screens.radar

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.remote.RadarSpecs
import com.takaapoo.weatherer.ui.screens.detail.DialogMultiChoiceRow
import com.takaapoo.weatherer.ui.theme.WeathererTheme
import com.takaapoo.weatherer.utility.toPx

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChooseQuantityDialog(
    modifier: Modifier = Modifier,
    selectedQuantity: RadarWeatherQuantity,
    selectedModel: WeatherModels,
    quantityWeatherModels: Map<RadarWeatherQuantity, WeatherModels>,
    radarSpecs: Map<WeatherModels, RadarSpecs>,

    onDismissRequest: () -> Unit,
    onConfirmation: (quantity: RadarWeatherQuantity, model: WeatherModels) -> Unit,
) {
    val density = LocalDensity.current
    val savedSelectedQuantity = rememberSaveable { selectedQuantity }
    val savedSelectedWeatherModel = rememberSaveable { selectedModel }

    var chosenQuantity by rememberSaveable { mutableStateOf(selectedQuantity) }
    var chosenModel by rememberSaveable { mutableStateOf(selectedModel) }

    val availableWeatherModels = radarSpecs.filter {
        it.value.variables.contains(chosenQuantity.serverName) ||
                it.value.variables.contains(chosenQuantity.serverName2)
    }.map {
        it.key
    }
    val availableWeatherQuantities = RadarWeatherQuantity.entries
        .filter { radarWeatherQuantity->
            radarSpecs.filter {
                it.value.variables.contains(radarWeatherQuantity.serverName) ||
                        it.value.variables.contains(radarWeatherQuantity.serverName2)
            }.isNotEmpty()
        }
    val quantityScrollState = rememberScrollState(
        initial = (availableWeatherQuantities.indexOf(selectedQuantity).coerceAtLeast(0) *
                dimensionResource(R.dimen.dialog_multi_choice_row_height).toPx(density)).toInt()
    )

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
                    .padding(vertical = 24.dp)
            ){
                Text(
                    text = stringResource(R.string.quantity),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(220.dp)
                        .verticalScroll(quantityScrollState)
                ) {
                    availableWeatherQuantities.forEach { quantity ->
                        DialogMultiChoiceRow(
                            modifier = Modifier.fillMaxWidth(),
                            text = quantity.nameId?.let { stringResource(it) },
                            annotatedText = quantity.title,
                            isSelected = quantity == chosenQuantity,
                            onSelect = {
                                chosenQuantity = quantity
                                chosenModel = if (quantity == selectedQuantity) selectedModel
                                else quantityWeatherModels[chosenQuantity]!!
                            }
                        )
                    }
                }
                HorizontalDivider()
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.weather_provider),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(state = rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(
                        ButtonGroupDefaults.ConnectedSpaceBetween
                    ),
                ) {
                    Spacer(Modifier.width(16.dp))
                    availableWeatherModels.forEachIndexed { index, weatherModel ->
                        val shapes = when (index) {
                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            availableWeatherModels.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        }
                        val isSelected = chosenModel == weatherModel
                        ToggleButton(
                            checked = isSelected,
                            onCheckedChange = {
                                if (it) {
                                    chosenModel = weatherModel
                                }
                            },
                            shapes = if (availableWeatherModels.size == 1) ButtonGroupDefaults.connectedMiddleButtonShapes()
                                    else shapes,
                        ) {
                            Text(
                                text = weatherModel.title,
                                fontFamily = FontFamily.Default
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                }



                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { onDismissRequest() },
                    ) {
                        Text(
                            text = "Cancel",
                            fontFamily = FontFamily.Default
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            onConfirmation(chosenQuantity, chosenModel)
                            onDismissRequest()
                        }
                    ) {
                        Text(
                            text = if (chosenQuantity == savedSelectedQuantity &&
                                chosenModel == savedSelectedWeatherModel) "Reload"
                            else "Load",
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
private fun ChooseQuantityDialogPreview() {
    WeathererTheme {
        ChooseQuantityDialog(
            modifier = Modifier.fillMaxSize(),
            selectedQuantity = RadarWeatherQuantity.TEMPERATURE,
            selectedModel = WeatherModels.NOAA_GFS_013,
            onDismissRequest = {},
            onConfirmation = {_, _ ->},
            quantityWeatherModels = emptyMap(),
            radarSpecs = emptyMap()
        )
    }
}

