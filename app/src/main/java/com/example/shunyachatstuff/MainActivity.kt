package com.example.shunyachatstuff

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var recorder: MediaRecorder
    private var audioFileName: String? = null

    lateinit var chatAdapter : AudioItemAdapter
    private var chatList = ArrayList<ChatModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Helper().checkAndRequestPermission(this)

        chatAdapter = AudioItemAdapter(chatList)
        rvChatList.adapter = chatAdapter
        val chatModel = ChatModel(1,"00:10", 0, "", "", false)
        chatList.add(chatModel)
        rvChatList.scrollToPosition(chatList.size-1)

        ivCamera.setOnClickListener(this)
        ivSend.setOnClickListener(this)
        ivMic.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when(event!!.action){
                    MotionEvent.ACTION_DOWN ->{
                        val anim = AnimationUtils.loadAnimation(this@MainActivity, R.anim.zoom_in_anim)
                        ivMic.startAnimation(anim)
                        startRecording()
                    }
                    MotionEvent.ACTION_UP ->{
                        val elapsedMillis: Long =
                            SystemClock.elapsedRealtime() - chronometer.base
                        if (elapsedMillis>1000){
                            val chatModel = ChatModel(0,chronometer.text.toString(), 0, "", audioFileName!!, false)
                            chatList.add(chatModel)
                            chatAdapter.notifyItemInserted(chatList.size-1)
                        }else{
                            Toast.makeText(this@MainActivity, "Too short recording", Toast.LENGTH_SHORT).show()
                            val file = File(audioFileName!!)
                            file.delete()
                            if(file.exists()){
                                file.canonicalFile.delete()
                                if(file.exists()){
                                    applicationContext.deleteFile(file.name);
                                }
                            }
                        }
                        stopRecording()

                    }
                }
                return true
            }
        })
    }

    override fun onPause() {
        super.onPause()
        chatAdapter.stopPlayer()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                val perms: MutableMap<String, Int> =
                    HashMap()
                // Initial
                perms[Manifest.permission.RECORD_AUDIO] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.READ_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] =
                    PackageManager.PERMISSION_GRANTED
                // Fill with results
                var i = 0
                while (i < permissions.size) {
                    perms[permissions[i]] = grantResults[i]
                    i++
                }
                if (perms[Manifest.permission.RECORD_AUDIO] == PackageManager.PERMISSION_GRANTED &&
                    perms[Manifest.permission.READ_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED &&
                    perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                ) {
                    //All permission granted

                } else {
                    // Permission Denied
                    Toast.makeText(this@MainActivity, "Authorization Denied", Toast.LENGTH_SHORT)
                        .show()
                    Helper().checkAndRequestPermission(this)
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }


    private fun stopRecording() {
        chronometer.stop()
        try {
            recorder.stop()
        }catch (r : RuntimeException){
            r.printStackTrace()
        }
        recorder.release()
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.ivCamera -> { }
            R.id.ivSend -> {}
        }
    }

    private fun startRecording() {
        chronometer.visibility = View.VISIBLE
        recorder = MediaRecorder()
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        val path =
            Environment.getExternalStorageDirectory().absolutePath + "/MyFolder/"
        val dir = File(path)
        if (!dir.exists()) dir.mkdirs()
        audioFileName = path + System.currentTimeMillis().toString() + ".mp4"

        recorder.setOutputFile(audioFileName)
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        try {
            recorder.prepare()
            recorder.start()
            chronometer.base = SystemClock.elapsedRealtime();
            chronometer.start() // Recording is now started
            Log.e("filename", audioFileName)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


}