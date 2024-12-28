package com.takaapoo.weatherer.ui.screens.settings


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.AppTheme
import com.takaapoo.weatherer.ui.theme.WeathererTheme


@Composable
fun ChooseThemeDialog(
    modifier: Modifier = Modifier,
    appTheme: AppTheme,
    onDismiss: () -> Unit,
    onConfirmation: (selectedTheme: AppTheme) -> Unit,
){
    var selectedTheme by rememberSaveable { mutableStateOf(appTheme) }
    Dialog(
        onDismissRequest = onDismiss
    ){
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ){
                Icon(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    painter = painterResource(R.drawable.theme),
                    contentDescription = null
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Theme",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Choose app theme",
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 24.dp),
                    style = MaterialTheme.typography.bodyMedium,
//                    fontWeight = FontWeight.Normal
//                    fontSize = 16.sp,
//                    fontFamily = FontFamily.Default
                )
                Spacer(Modifier.height(16.dp))
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                ) {
                    ThemeRow(
                        theme = AppTheme.LIGHT,
                        selected = selectedTheme == AppTheme.LIGHT,
                        onSelected = {
                            selectedTheme = AppTheme.LIGHT
                        }
                    )
                    ThemeRow(
                        theme = AppTheme.DARK,
                        selected = selectedTheme == AppTheme.DARK,
                        onSelected = {
                            selectedTheme = AppTheme.DARK
                        }
                    )
                    ThemeRow(
                        theme = AppTheme.SYSTEM,
                        selected = selectedTheme == AppTheme.SYSTEM,
                        onSelected = {
                            selectedTheme = AppTheme.SYSTEM
                        }
                    )
                }
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { onDismiss() },
                    ) {
                        Text(
                            text = "Cancel",
                            fontFamily = FontFamily.Default
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(
                        onClick = { onConfirmation(selectedTheme) },
                    ) {
                        Text(
                            text = "Confirm",
                            fontFamily = FontFamily.Default
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeRow(
    modifier: Modifier = Modifier,
    theme: AppTheme,
    selected: Boolean,
    onSelected: () -> Unit
) {
   Row(
       modifier = modifier
           .fillMaxWidth()
           .clickable(onClick = onSelected)
           .padding(vertical = 4.dp, horizontal = 8.dp),
       verticalAlignment = Alignment.CenterVertically
   ) {
       RadioButton(
           selected = selected,
           onClick = onSelected
       )
       Spacer(modifier = Modifier.width(24.dp))
       Text(
           text = stringResource(theme.description),
           style = TextStyle.Default
       )
   }
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
