package com.zqlite.android.ipfstore.ipfs

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.os.Build
import android.support.v7.app.AlertDialog
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import okio.Okio
import java.io.File
import java.io.FileNotFoundException

/**
 * Created by scott on 2018/3/10.
 */
class IPFSDaemon(val mContext: Context) {


    /**
     * ipfs 是否已经初始化过，即调用了 init
     */
    fun isReady() = File(getIPFSRepoPath(), "version").exists()

    fun copyIPFSBin(activity:Activity,afterCopyCallback:()->Unit) = async(UI) {
        val progressDialog = ProgressDialog(mContext,0)
        progressDialog.setMessage("首次初始化 IPFS，请稍后...")
        progressDialog.setCancelable(false)
        progressDialog.show()


        try {
            async(CommonPool) {
                copyFile(activity)
                getIPFSBinaryFile().setExecutable(true)
            }.await()

            val initInfo = async(CommonPool) {
                val exec = run("init")
                exec.waitFor()
                exec.inputStream.bufferedReader().readText()+ exec.errorStream.bufferedReader().readText()
            }.await()
            progressDialog.dismiss()
            AlertDialog.Builder(mContext)
                    .setMessage(initInfo)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            afterCopyCallback()
        }catch (e:FileNotFoundException){
            progressDialog.dismiss()
            AlertDialog.Builder(mContext)
                    .setMessage("很遗憾，当前手机不支持 " + Build.CPU_ABI)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
        }
    }

    private fun getIPFSBinaryFile() = File(mContext.filesDir, "ipfsbin")
    private fun getIPFSRepoPath() = File(mContext.filesDir, ".ipfs_repo")

    /**
     * 获得平台对应的可执行文件名
     */
    private fun getBinaryFileByABI(abi: String) = when {
        abi.toLowerCase().startsWith("x86") -> "x86"
        abi.toLowerCase().startsWith("arm") -> "arm"
        else -> "unknown"
    }

    /**
     * 使用 ipfs 的 bin 文件执行相关命令
     */
    fun run(cmd: String): Process {
        val env = arrayOf("IPFS_PATH=" +  getIPFSRepoPath().absoluteFile)
        val command = getIPFSBinaryFile().absolutePath + " " + cmd

        return Runtime.getRuntime().exec(command, env)
    }
    /**
     *
     */
    private fun copyFile(activity: Activity) {

        val source = Okio.buffer(Okio.source(activity.assets.open(getBinaryFileByABI(Build.CPU_ABI))))
        val sink = Okio.buffer(Okio.sink(getIPFSBinaryFile()))
        while (!source.exhausted()) {
            source.read(sink.buffer(), 1024)
        }
        source.close()
        sink.close()

    }
}