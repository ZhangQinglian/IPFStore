package com.zqlite.android.ipfstore

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.zqlite.android.ipfstore.ipfs.IPFSDaemon
import com.zqlite.android.ipfstore.ipfs.IPFSDaemonService
import com.zqlite.android.ipfstore.ipfs.IPFSState
import io.ipfs.kotlin.IPFS
import io.ipfs.kotlin.model.VersionInfo
import kotlinx.android.synthetic.main.activity_main.*
import org.ligi.kaxt.startActivityFromClass

class MainActivity : AppCompatActivity() {

    var mIPFSDaemon : IPFSDaemon ? = null
    var ipfs = IPFS()
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mIPFSDaemon = IPFSDaemon(this)
        init.setOnClickListener {
            if(!mIPFSDaemon!!.isReady()){
                mIPFSDaemon!!.copyIPFSBin(MainActivity@this){
                    Toast.makeText(MainActivity@this,"init ok",Toast.LENGTH_LONG).show()
                }
            }else{
                startService(Intent(this, IPFSDaemonService::class.java))

                IPFSState.isDaemonRunning = true

                val progressDialog = ProgressDialog(this)
                progressDialog.setMessage("starting daemon")
                progressDialog.show()


                Thread(Runnable {
                    var version: VersionInfo? = null
                    while (version == null) {
                        try {
                            version = ipfs.info.version()
                        } catch (ignored: Exception) {
                        }
                    }

                    runOnUiThread {
                        progressDialog.dismiss()
                        startActivityFromClass(DetailsActivity::class.java)
                    }
                }).start()
            }
        }

        stop.setOnClickListener {
            stopService(Intent(MainActivity@this,IPFSDaemonService::class.java))
        }
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
}
