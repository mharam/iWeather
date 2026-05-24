package com.takaapoo.weatherer.ui.screens.home.dialogs

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import com.takaapoo.weatherer.ui.theme.WeathererTheme
import org.junit.Test

class DeleteAlertDialogTest {


    @OptIn(ExperimentalTestApi::class)
    @Test
    fun deleteAlertDialogTest() = runComposeUiTest {
        setContent {
            WeathererTheme {
                DeleteAlertDialog(
                    modifier = Modifier,
                    onDismiss = {},
                    onConfirmation = {},
                    locationName = "Tehran"
                )
            }
        }
        onNodeWithText("Delete").assertIsDisplayed()
    }

}