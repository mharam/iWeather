package com.takaapoo.weatherer.ui.screens.login

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.remote.UserAuthResponse
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.model.AppTheme
import com.takaapoo.weatherer.domain.model.LoginState
import com.takaapoo.weatherer.ui.theme.Gray20
import com.takaapoo.weatherer.ui.theme.WeathererTheme
import com.takaapoo.weatherer.ui.theme.customColorScheme
import com.takaapoo.weatherer.ui.theme.gerdFontFamily
import com.takaapoo.weatherer.utility.User
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Login(
    modifier: Modifier = Modifier,
    loginState: LoginState,
    user: User?,
    theme: AppTheme,

    onNavigateUp: () -> Unit,
    onUpdateLoginState: (MyResult<out UserAuthResponse>) -> Unit,
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer),
                title = {
                    Text(
                        text = "Sign in",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {

                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0x00FFFFFF)
                ),
                windowInsets = WindowInsets(
                    left = 0, top = TopAppBarDefaults.windowInsets.getTop(density), right = 0, bottom = 0
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxHeight().widthIn(max = 500.dp)
                .background(color = MaterialTheme.customColorScheme.detailScreenSurface)
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    start = 16.dp,
                    end = 16.dp
                )
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Image(
                modifier = Modifier.fillMaxWidth().height(300.dp)
                    .align(Alignment.CenterHorizontally),
                painter = painterResource(id = R.drawable.login),
                contentDescription = null,
                contentScale = ContentScale.FillHeight,
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.sign_in_title),
                style = MaterialTheme.typography.titleMedium,
                fontFamily = gerdFontFamily
            )

            Spacer(Modifier.height(48.dp))
            Box(
                Modifier.fillMaxWidth()
                    .height(1.dp)
                    .padding(horizontal = 16.dp)
                    .background(Gray20)
            )
            Spacer(Modifier.height(48.dp))
            val buttonResource = when (theme) {
                AppTheme.LIGHT -> R.drawable.sign_in_button_light
                AppTheme.DARK -> R.drawable.sign_in_button_dark
                AppTheme.SYSTEM -> {
                    if (isSystemInDarkTheme()) R.drawable.sign_in_button_dark
                    else R.drawable.sign_in_button_light
                }
            }
            Image(
                painter = painterResource(id = buttonResource),
                contentDescription = "sign in button",
                contentScale = ContentScale.FillHeight,
                modifier = Modifier
                    .height(40.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(percent = 50))
                    .clickable(
                        onClick = {
                            coroutineScope.launch {
                                val signInResult = user!!.signIn(context)
                                if (signInResult is MyResult.Success){
                                    Toast.makeText(
                                        context,
                                        "Hi ${signInResult.data.name}! \uD83D\uDD90",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    onNavigateUp()
                                } else if (signInResult is MyResult.Error){
                                    Toast.makeText(
                                        context,
                                        "${signInResult.message} \uD83D\uDE22",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
//                                onUpdateLoginState(signInResult)
                            }
                        }
                    )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    WeathererTheme {
        Login(
            modifier = Modifier.fillMaxSize(),
            loginState = LoginState(),
            user = null,
            theme = AppTheme.SYSTEM,
            onNavigateUp = {},
            onUpdateLoginState = {}
        )
    }
}