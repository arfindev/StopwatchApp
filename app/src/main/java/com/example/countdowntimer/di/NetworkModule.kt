package com.example.countdowntimer.di

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.countdowntimer.R
import com.example.countdowntimer.services.ServiceHelper
import com.example.countdowntimer.util.Constants.NOTIFICATION_CHANNEL_ID
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object NetworkModule {

    @ServiceScoped
    @Provides
    fun provideNotificationBuilder(@ApplicationContext context: Context): NotificationCompat.Builder {
        return NotificationCompat.Builder(
            context,
            NOTIFICATION_CHANNEL_ID
        ).setContentTitle("StopWatch").setContentText("00:00:00")
            .setSmallIcon(R.drawable.stopwatch)
            .setOngoing(true)
            .addAction(R.drawable.ic_stop, "Stop", ServiceHelper.stopPendingIntent(context))
            .addAction(R.drawable.ic_cancel, "Cancel", ServiceHelper.cancelPendingIntent(context))
            .setContentIntent(ServiceHelper.clickPendingIntent(context))
    }


    @ServiceScoped
    @Provides
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }


}