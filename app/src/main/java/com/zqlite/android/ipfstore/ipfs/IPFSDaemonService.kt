package com.zqlite.android.ipfstore.ipfs

/**
 * Created by scott on 2018/3/10.
 */
import android.app.IntentService
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.zqlite.android.ipfstore.DetailsActivity
import com.zqlite.android.ipfstore.R

class IPFSDaemonService : IntentService("IPFSDaemonService") {

    private var nManager: NotificationManager? = null
    private var daemon: Process? = null
    internal var NOTIFICATION_ID = 2333

    override fun onHandleIntent(intent: Intent) {
        val exitIntent = Intent(this, IPFSDaemonService::class.java)
        exitIntent.action = "STOP"
        val pendingExit = PendingIntent.getService(this, 0, exitIntent, 0)

        val targetIntent = Intent(this, DetailsActivity::class.java)
        val pIntent = PendingIntent.getActivity(this, 0, targetIntent, 0)
        val builder = NotificationCompat.Builder(this).setOngoing(true)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle("IPFS Daemon")
                .setContentText("The daemon is running")
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "exit", pendingExit)

        builder.setContentIntent(pIntent)
        nManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nManager!!.notify(NOTIFICATION_ID, builder.build())

        try {
            daemon = IPFSDaemon(baseContext).run("daemon")
            IPFSState.isDaemonRunning = true
            daemon!!.waitFor()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        daemon!!.destroy()
        super.onDestroy()
        IPFSState.isDaemonRunning = false
        nManager?.cancel(NOTIFICATION_ID)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val action = intent.action
        if (nManager != null && action != null && action == "STOP") {
            stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

}