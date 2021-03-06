package com.example.musicplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.musicplayer.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(),TrackPlaybackListener {

    private lateinit var mBinding: ActivityMainBinding

    private val mViewModel: MainViewModel by lazy {
       MainViewModel(this)
    }

    private lateinit var mTrackUtil: TrackUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = DataBindingUtil.setContentView(this,R.layout.activity_main)

        mBinding.viewModel = mViewModel

        initViewModel()

        observeLiveEvents()

        mBinding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser){
                    mViewModel.seekTo(progress * 1000)
                }
                Log.i(TAG, "onProgressChanged: ")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                Log.i(TAG, "onStartTrackingTouch: ")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Log.i(TAG, "onStopTrackingTouch: ")
            }

        })
    }

    /**
     * Initialize the ViewModel
     */
    private fun initViewModel() {
        mTrackUtil = TrackUtil(this) // Instantiate the TrackUtil.
        mViewModel.trackUtil = mTrackUtil // store instance of mTrackUtil to viewModel's trackUtil variable to survive config changes.
        mViewModel.initMediaPlayer() // initialize the media player.
        mTrackUtil.setCallback(this) // set the callback.
    }

    // Observe the live track details
    private fun observeLiveEvents() {
        mViewModel.currentTrackDetail.observe(this, { t ->
            t?.let { track ->
                mBinding.track = track
            }
        })

        mViewModel.isCompleted.observe(this,{ isCompleted  ->
            if(isCompleted){
                mViewModel.clearTimerObservable()
                mViewModel._isCompleted.value = false
            }
        })

    }

    override fun onResume() {
        super.onResume()
        mViewModel.showPlayIcon() // display a play icon.
    }

    override fun onPause() {
        super.onPause()
        mViewModel.pauseMediaPlayer() // Pause the media player.
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.releaseMediaPlayer() // release media player resources.
    }

    override fun onComplete(isComplete: Boolean) {
        if(isComplete){
            mViewModel.onNext() // play the next track
            Toast.makeText(this,"Track Changed",Toast.LENGTH_LONG).show()
        }else{
            mViewModel.showPlayIcon() // display a play icon.
//            Toast.makeText(this,"Tracks completed! Click previous button to play a track",Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}