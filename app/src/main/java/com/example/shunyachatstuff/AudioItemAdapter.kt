package com.example.shunyachatstuff

import android.media.MediaPlayer
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shunyachatstuff.AudioItemAdapter.AudioItemsViewHolder
import java.io.IOException
import java.util.*
import kotlin.math.ceil

class AudioItemAdapter internal constructor(private val audioItems: ArrayList<ChatModel>) :
    RecyclerView.Adapter<AudioItemsViewHolder>(), Handler.Callback {
    private var mediaPlayer: MediaPlayer? = null
    private val uiUpdateHandler: Handler
    private var playingPosition: Int
    private var playingHolder: AudioItemsViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioItemsViewHolder {
        val v = if(viewType ==1){
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_chat_received, parent, false)
        }else{
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_chat_sent, parent, false)
        }
        return AudioItemsViewHolder(v)
    }

    override fun onBindViewHolder(holder: AudioItemsViewHolder, position: Int) {
        if (position == playingPosition) {
            playingHolder = holder
            updatePlayingView()
        } else {
            updateNonPlayingView(holder)
        }
        holder.tvIndex.text = audioItems[position].duration
    }

    override fun getItemCount(): Int {
        return audioItems.size
    }

    override fun getItemViewType(position: Int): Int {
        return if(audioItems[position].viewType==1) {
            1
        }else{
            0
        }
    }

    override fun onViewRecycled(holder: AudioItemsViewHolder) {
        super.onViewRecycled(holder)
        if (playingPosition == holder.adapterPosition) {
            updateNonPlayingView(playingHolder)
            playingHolder = null
        }
    }

    private fun updateNonPlayingView(holder: AudioItemsViewHolder?) {
        if (holder === playingHolder) {
            uiUpdateHandler.removeMessages(MSG_UPDATE_SEEK_BAR)
        }
        holder!!.seekBar.isEnabled = false
        holder.seekBar.progress = 0
        holder.tvIndex.text = audioItems[holder.adapterPosition].duration
        holder.ivPlayPause.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
    }

    private fun getTimeString(millis: Long): String? {
        val buf = StringBuffer()
        val hours = (millis / (1000 * 60 * 60)).toInt()
        val minutes = (millis % (1000 * 60 * 60) / (1000 * 60)).toInt()
        val seconds = (millis % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()
        buf
//            .append(String.format("%02d", hours))
//            .append(":")
            .append(String.format("%02d", minutes))
            .append(":")
            .append(String.format("%02d", seconds))
        return buf.toString()
    }

    private fun updatePlayingView() {
        playingHolder!!.seekBar.max = mediaPlayer!!.duration
        playingHolder!!.seekBar.progress = mediaPlayer!!.currentPosition
        playingHolder!!.tvIndex.text = getTimeString(playingHolder!!.seekBar.progress.toLong())
        //playingHolder!!.tvIndex.text = mediaPlayer!!.currentPosition.toString()
        playingHolder!!.seekBar.isEnabled = true
        if (mediaPlayer!!.isPlaying) {
            uiUpdateHandler.sendEmptyMessageDelayed(
                MSG_UPDATE_SEEK_BAR,
                100
            )
            playingHolder!!.ivPlayPause.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24)
        } else {
            uiUpdateHandler.removeMessages(MSG_UPDATE_SEEK_BAR)
            playingHolder!!.ivPlayPause.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
        }
    }

    fun stopPlayer() {
        if (null != mediaPlayer) {
            releaseMediaPlayer()
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            MSG_UPDATE_SEEK_BAR -> {
                playingHolder!!.seekBar.progress = mediaPlayer!!.currentPosition
                playingHolder!!.tvIndex.text = getTimeString(playingHolder!!.seekBar.progress.toLong())
                uiUpdateHandler.sendEmptyMessageDelayed(
                    MSG_UPDATE_SEEK_BAR,
                    100
                )
                return true
            }
        }
        return false
    }

    inner class AudioItemsViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener,
        OnSeekBarChangeListener {
        var seekBar: SeekBar
        var ivPlayPause: ImageView
        var tvIndex: TextView

        override fun onClick(v: View) {
            if (adapterPosition == playingPosition) {
                // toggle between play/pause of audio
                if (mediaPlayer!!.isPlaying) {
                    mediaPlayer!!.pause()
                } else {
                    mediaPlayer!!.start()
                }
            } else {
                // start another audio playback
                playingPosition = adapterPosition
                if (mediaPlayer != null) {
                    if (null != playingHolder) {
                        updateNonPlayingView(playingHolder)
                    }
                    mediaPlayer!!.release()
                }
                playingHolder = this
                startMediaPlayer(audioItems[playingPosition].file)
            }
            updatePlayingView()
        }

        override fun onProgressChanged(
            seekBar: SeekBar,
            progress: Int,
            fromUser: Boolean
        ) {
            if (fromUser) {
                mediaPlayer!!.seekTo(progress)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {}
        override fun onStopTrackingTouch(seekBar: SeekBar) {}

        init {
            ivPlayPause =
                itemView.findViewById(R.id.ivPlay) as ImageView
            ivPlayPause.setOnClickListener(this)
            seekBar = itemView.findViewById(R.id.seekBar) as SeekBar
            seekBar.setOnSeekBarChangeListener(this)
            tvIndex = itemView.findViewById(R.id.tvDuration) as TextView
        }
    }

    private fun startMediaPlayer(audioFile: String) {
        mediaPlayer = MediaPlayer()
        try {
            mediaPlayer!!.setDataSource(audioFile)
            mediaPlayer!!.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        mediaPlayer!!.setOnCompletionListener { releaseMediaPlayer() }
        mediaPlayer!!.start()
    }

    private fun releaseMediaPlayer() {
        if (null != playingHolder) {
            updateNonPlayingView(playingHolder)
        }
        mediaPlayer!!.release()
        mediaPlayer = null
        playingPosition = -1
    }

    companion object {
        private const val MSG_UPDATE_SEEK_BAR = 1845
    }

    init {
        playingPosition = -1
        uiUpdateHandler = Handler(this)
    }
}