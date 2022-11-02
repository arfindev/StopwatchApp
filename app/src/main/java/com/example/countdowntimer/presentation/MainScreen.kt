package com.example.countdowntimer.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.countdowntimer.services.ServiceHelper
import com.example.countdowntimer.services.StopWatchService
import com.example.countdowntimer.ui.theme.LightGreen
import com.example.countdowntimer.ui.theme.LightOrange
import com.example.countdowntimer.util.Constants.ACTION_SERVICE_CANCEL
import com.example.countdowntimer.util.Constants.ACTION_SERVICE_START
import com.example.countdowntimer.util.Constants.ACTION_SERVICE_STOP
import com.example.countdowntimer.util.StopWatchState


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(stopWatchService: StopWatchService) {
    val context = LocalContext.current
    val hours by stopWatchService.hours
    val minutes by stopWatchService.minutes
    val seconds by stopWatchService.seconds
    val currentState by stopWatchService.currentState

    @ExperimentalAnimationApi
    fun addAnimation(duration: Int = 600): ContentTransform {
        return slideInVertically(animationSpec = tween(durationMillis = duration)) { height -> height } + fadeIn(
            animationSpec = tween(durationMillis = duration)
        ) with slideOutVertically(animationSpec = tween(durationMillis = duration)) { height -> height } + fadeOut(
            animationSpec = tween(durationMillis = duration)
        )
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.weight(weight = 9f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(targetState = hours, transitionSpec = { addAnimation() }) {
                Text(
                    text = hours,
                    style = TextStyle(
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                )
            }
            AnimatedContent(targetState = minutes, transitionSpec = { addAnimation() }) {
                Text(
                    text = minutes, style = TextStyle(
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
            AnimatedContent(targetState = seconds, transitionSpec = { addAnimation() }) {
                Text(
                    text = seconds, style = TextStyle(
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

        }
        Row(modifier = Modifier.weight(weight = 1f)) {
            Button(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(0.8f),
                onClick = {
                    ServiceHelper.triggerForegroundService(
                        context = context,
                        action = if (currentState == StopWatchState.Started) ACTION_SERVICE_STOP else ACTION_SERVICE_START
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightOrange, contentColor = Color.White
                )
            ) {
                Text(
                    text = if (currentState == StopWatchState.Started) "Stop"
                    else if ((currentState == StopWatchState.Stopped)) "Resume"
                    else "Start"
                )
            }
            Spacer(modifier = Modifier.width(30.dp))
            Button(modifier = Modifier
                .weight(1f)
                .fillMaxHeight(0.8f),
                onClick = {
                    ServiceHelper.triggerForegroundService(
                        context = context,
                        action = ACTION_SERVICE_CANCEL
                    )
                },
                enabled = seconds != "00" && currentState != StopWatchState.Started,
                colors = ButtonDefaults.buttonColors(containerColor = LightGreen, contentColor = Color.White)
            ) {
                Text(text = "Cancel")

            }

        }


    }


}

