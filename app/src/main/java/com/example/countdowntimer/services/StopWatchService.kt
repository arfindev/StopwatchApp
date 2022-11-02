package com.example.countdowntimer.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import com.example.countdowntimer.util.Constants.ACTION_SERVICE_CANCEL
import com.example.countdowntimer.util.Constants.ACTION_SERVICE_START
import com.example.countdowntimer.util.Constants.ACTION_SERVICE_STOP
import com.example.countdowntimer.util.Constants.NOTIFICATION_CHANNEL_ID
import com.example.countdowntimer.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.countdowntimer.util.Constants.NOTIFICATION_ID
import com.example.countdowntimer.util.Constants.STOPWATCH_STATE
import com.example.countdowntimer.util.StopWatchState
import com.example.countdowntimer.util.formatTime
import com.example.countdowntimer.util.pad
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class StopWatchService : Service() {

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder

    private lateinit var timer: Timer
    private var duration: Duration = Duration.ZERO


    var seconds = mutableStateOf("00")
        private set
    var minutes = mutableStateOf("00")
        private set
    var hours = mutableStateOf("00")
        private set
    var currentState = mutableStateOf(StopWatchState.Idle)
        private set

    private val binder = StopwatchBinder()

    override fun onBind(p0: Intent?) = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.getStringExtra(STOPWATCH_STATE)) {
            StopWatchState.Started.name -> {
                setStopButton()
                startForegroundService()
                startStopwatch { hours, minutes, seconds ->
                    updateNotification(hours = hours, minutes = minutes, seconds = seconds)
                }
            }
            StopWatchState.Stopped.name -> {
                stopStopwatch()
                setResumeButton()
            }
            StopWatchState.Canceled.name -> {
                stopStopwatch()
                cancelStopwatch()
                stopForeGroundService()
            }
        }
        intent?.action.let {
            when (it) {
                ACTION_SERVICE_START -> {
                    setStopButton()
                    startForegroundService()
                    startStopwatch { hours, minutes, seconds ->
                        updateNotification(hours = hours, minutes = minutes, seconds = seconds)
                    }
                }
                ACTION_SERVICE_STOP -> {
                    stopStopwatch()
                setResumeButton()
                }
                ACTION_SERVICE_CANCEL -> {
                    stopStopwatch()
                    cancelStopwatch()
                    stopForeGroundService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    private fun startStopwatch(onTick: (h: String, m: String, s: String) -> Unit) {
        currentState.value = StopWatchState.Started
        timer = fixedRateTimer(initialDelay = 1000L, period = 1000L) {
            duration = duration.plus(1.seconds)
            updateTimeUnits()
            onTick(hours.value, minutes.value, seconds.value)
        }
    }

    private fun stopStopwatch() {
        if (this::timer.isInitialized) {
            timer.cancel()
        }
        currentState.value = StopWatchState.Stopped
    }


    private fun cancelStopwatch() {
        duration = Duration.ZERO
        currentState.value = StopWatchState.Idle
        updateTimeUnits()
    }

    private fun updateTimeUnits() {
        duration.toComponents { hours, minutes, seconds, nanoseconds ->
            this@StopWatchService.hours.value = hours.toInt().pad()
            this@StopWatchService.minutes.value = minutes.pad()
            this@StopWatchService.seconds.value = seconds.pad()

        }
    }

    private fun startForegroundService() {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }


    private fun stopForeGroundService() {
        notificationManager.cancel(NOTIFICATION_ID)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(hours: String, minutes: String, seconds: String) {
        notificationManager.notify(
            NOTIFICATION_ID,
            notificationBuilder.setContentText(
                formatTime(
                    hours = hours,
                    seconds = seconds,
                    minutes = minutes
                )
            ).build()
        )
    }


    private fun setStopButton() {
        notificationBuilder.mActions.removeAt(0)
        notificationBuilder.mActions.add(
            0,
            NotificationCompat.Action(
                0,
                "Stop",
                ServiceHelper.stopPendingIntent(this)
                )
        )
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }


    private fun setResumeButton() {
        notificationBuilder.mActions.removeAt(0)
        notificationBuilder.mActions.add(
            0,
            NotificationCompat.Action(
                0,
                "Resume",
                ServiceHelper.resumePendingIntent(this)
                )
        )
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }


    inner class StopwatchBinder : Binder() {
        fun getService(): StopWatchService = this@StopWatchService
    }


}