/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//Modifications copyright (C) 2014-2016 sprylab technologies GmbH
//Modifications copyright (C) 2018 Nomtek

package com.nomtek.libs.centercropvideoview

import android.app.AlertDialog
import android.content.Context
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.opengl.GLES20
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.MediaController
import java.io.IOException
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext

/**
 * Displays a video file.  The CenterCropVideoView class
 * can load images from various sources (such as resources or content
 * providers), takes care of computing its measurement from the video so that
 * it can be used in any layout manager. It scales video so it fits view bounds. It uses
 * center-crop scaling.
 *
 *
 *
 * *Note: VideoView does not retain its full state when going into the
 * background.*  In particular, it does not restore the current play state,
 * play position or selected tracks.  Applications should
 * save and restore these on their own in
 * [android.app.Activity.onSaveInstanceState] and
 * [android.app.Activity.onRestoreInstanceState].
 *
 *
 * Also note that the audio session id (from [.getAudioSessionId]) may
 * change from its previously returned value when the VideoView is restored.
 *
 *
 * This code is based on the official Android sources for 7.1.1_r13 with the following differences:
 *
 *  1. extends [android.view.TextureView] instead of a [android.view.SurfaceView]
 * allowing proper view animations
 *  1. removes code that uses hidden APIs and thus is not available (e.g. subtitle support)
 *  1. various small fixes and improvements
 *
 */
class CenterCropVideoView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0) : TextureView(context, attrs, defStyle), MediaController.MediaPlayerControl {

    // settable by the client
    private var mUri: Uri? = null
    private var mHeaders: Map<String, String>? = null

    // mCurrentState is a CenterCropVideoView object's current state.
    // mTargetState is the state that a method caller intends to reach.
    // For instance, regardless the CenterCropVideoView object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private var mCurrentState = STATE_IDLE
    private var mTargetState = STATE_IDLE

    // All the stuff we need for playing and showing a video
    private var mSurface: Surface? = null
    private var mMediaPlayer: MediaPlayer? = null
    private var mAudioSession: Int = 0
    private var mVideoWidth: Int = 0
    private var mVideoHeight: Int = 0
    private var mMediaController: MediaController? = null
    private var mOnCompletionListener: MediaPlayer.OnCompletionListener? = null
    private var mOnPreparedListener: MediaPlayer.OnPreparedListener? = null
    private var mCurrentBufferPercentage: Int = 0
    private var mOnErrorListener: MediaPlayer.OnErrorListener? = null
    private var mOnInfoListener: MediaPlayer.OnInfoListener? = null
    private var mSeekWhenPrepared: Int = 0  // recording the seek position while preparing
    private var mCanPause: Boolean = false
    private var mCanSeekBack: Boolean = false
    private var mCanSeekForward: Boolean = false
    private var mShouldRequestAudioFocus = true
    private var mVolume = 0f

    private var mSizeChangedListener: MediaPlayer.OnVideoSizeChangedListener = MediaPlayer
            .OnVideoSizeChangedListener { mp, width, height ->
        mVideoWidth = mp.videoWidth
        mVideoHeight = mp.videoHeight
        if (mVideoWidth != 0 && mVideoHeight != 0) {
            surfaceTexture.setDefaultBufferSize(mVideoWidth, mVideoHeight)
            requestLayout()
        }
    }

    private var mPreparedListener: MediaPlayer.OnPreparedListener = MediaPlayer.OnPreparedListener { mp ->
        mCurrentState = STATE_PREPARED

        mCanSeekForward = true
        mCanSeekBack = mCanSeekForward
        mCanPause = mCanSeekBack

        if (mOnPreparedListener != null) {
            mOnPreparedListener!!.onPrepared(mMediaPlayer)
        }
        if (mMediaController != null) {
            mMediaController!!.isEnabled = true
        }
        mVideoWidth = mp.videoWidth
        mVideoHeight = mp.videoHeight

        val seekToPosition = mSeekWhenPrepared  // mSeekWhenPrepared may be changed after seekTo() call
        if (seekToPosition != 0) {
            seekTo(seekToPosition)
        }
        if (mVideoWidth != 0 && mVideoHeight != 0) {
            //Log.i("@@@@", "video size: " + mVideoWidth +"/"+ mVideoHeight);
            surfaceTexture.setDefaultBufferSize(mVideoWidth, mVideoHeight)
            // We won't get a "surface changed" callback if the surface is already the right size, so
            // start the video here instead of in the callback.
            if (mTargetState == STATE_PLAYING) {
                start()
                if (mMediaController != null) {
                    mMediaController!!.show()
                }
            } else if ((!isPlaying && (seekToPosition != 0 || currentPosition > 0))) {
                if (mMediaController != null) {
                    // Show the media controls when we're paused into a video and make 'em stick.
                    mMediaController!!.show(0)
                }
            }
        } else {
            // We don't know the video size yet, but should start anyway.
            // The video size might be reported to us later.
            if (mTargetState == STATE_PLAYING) {
                start()
            }
        }
    }

    private val mCompletionListener = MediaPlayer.OnCompletionListener {
        mCurrentState = STATE_PLAYBACK_COMPLETED
        mTargetState = STATE_PLAYBACK_COMPLETED
        if (mMediaController != null) {
            mMediaController!!.hide()
        }
        if (mOnCompletionListener != null) {
            mOnCompletionListener!!.onCompletion(mMediaPlayer)
        }
    }

    private val mInfoListener = MediaPlayer.OnInfoListener { mp, arg1, arg2 ->
        if (mOnInfoListener != null) {
            mOnInfoListener!!.onInfo(mp, arg1, arg2)
        }
        true
    }

    private val mErrorListener = object : MediaPlayer.OnErrorListener {
        override fun onError(mp: MediaPlayer, framework_err: Int, impl_err: Int): Boolean {
            mCurrentState = STATE_ERROR
            mTargetState = STATE_ERROR
            if (mMediaController != null) {
                mMediaController!!.hide()
            }

            /* If an error handler has been supplied, use it and finish. */
            if (mOnErrorListener != null) {
                if (mOnErrorListener!!.onError(mMediaPlayer, framework_err, impl_err)) {
                    return true
                }
            }

            /* Otherwise, pop up an error dialog so the user knows that
                        * something bad has happened. Only try and pop up the dialog
                        * if we're attached to a window. When we're going away and no
                        * longer have a window, don't bother showing the user an error.
                        */
            if (windowToken != null) {
                val r = getContext().resources
                val messageId: Int

                if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
                    messageId = android.R.string.VideoView_error_text_invalid_progressive_playback
                } else {
                    messageId = android.R.string.VideoView_error_text_unknown
                }

                AlertDialog.Builder(getContext())
                        .setMessage(messageId)
                        .setPositiveButton(android.R.string.VideoView_error_button
                        ) { dialog, whichButton ->
                            /* If we get here, there is no onError listener, so
                                                         * at least inform them that the video is over.
                                                         */
                            if (mOnCompletionListener != null) {
                                mOnCompletionListener!!.onCompletion(mMediaPlayer)
                            }
                        }
                        .setCancelable(false)
                        .show()
            }
            return true
        }
    }

    private val mBufferingUpdateListener = MediaPlayer.OnBufferingUpdateListener { _, percent -> mCurrentBufferPercentage = percent }

    private var mSurfaceTextureListener: TextureView.SurfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            val isValidState = (mTargetState == STATE_PLAYING)
            val hasValidSize = (width > 0 && height > 0)
            if (mMediaPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared)
                }
                start()
            }
        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            mSurface = Surface(surface)
            openVideo()
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            // after we return from this we can't use the surface any more
            if (mSurface != null) {
                mSurface!!.release()
                mSurface = null
            }
            if (mMediaController != null) mMediaController!!.hide()
            release(true)
            return true
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            // do nothing
        }
    }

    private val isInPlaybackState: Boolean
        get() = ((mMediaPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING))

    init {
        mVideoWidth = 0
        mVideoHeight = 0

        surfaceTextureListener = mSurfaceTextureListener

        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()

        mCurrentState = STATE_IDLE
        mTargetState = STATE_IDLE
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        var width = View.getDefaultSize(mVideoWidth, widthMeasureSpec)
        var height = View.getDefaultSize(mVideoHeight, heightMeasureSpec)
        if (mVideoWidth > 0 && mVideoHeight > 0) {

            val widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec)
            val widthSpecSize = View.MeasureSpec.getSize(widthMeasureSpec)
            val heightSpecMode = View.MeasureSpec.getMode(heightMeasureSpec)
            val heightSpecSize = View.MeasureSpec.getSize(heightMeasureSpec)

            if (widthSpecMode == View.MeasureSpec.EXACTLY && heightSpecMode == View.MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize
                height = heightSpecSize
            } else if (widthSpecMode == View.MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize
                height = width * mVideoHeight / mVideoWidth
                if (heightSpecMode == View.MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize
                }
            } else if (heightSpecMode == View.MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize
                width = height * mVideoWidth / mVideoHeight
                if (widthSpecMode == View.MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = mVideoWidth
                height = mVideoHeight
                if (heightSpecMode == View.MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize
                    width = height * mVideoWidth / mVideoHeight
                }
                if (widthSpecMode == View.MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize
                    height = width * mVideoHeight / mVideoWidth
                }
            }
        } else {
            // no size yet, just adopt the given spec sizes
        }
        setMeasuredDimension(width, height)
        if (mVideoWidth != 0 && mVideoHeight != 0) {
            adjustVideoScale()
        }
    }

    private fun adjustVideoScale() {
        val matrix = Matrix()

        val viewWidth = measuredWidth
        val viewHeight = measuredHeight

        var scaleX = mVideoWidth.toFloat() / viewWidth
        var scaleY = mVideoHeight.toFloat() / viewHeight

        var boundX = viewWidth - mVideoWidth / scaleY
        var boundY = viewHeight - mVideoHeight / scaleX

        if (scaleX < scaleY) {
            scaleY *= (1.0f / scaleX)
            scaleX = 1.0f
            boundX = 0f
        } else {
            scaleX *= (1.0f / scaleY)
            scaleY = 1.0f
            boundY = 0f
        }

        matrix.setScale(scaleX, scaleY)
        matrix.postTranslate(boundX / 2, boundY / 2)

        setTransform(matrix)
    }

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        event.className = CenterCropVideoView::class.java!!.name
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.className = CenterCropVideoView::class.java!!.name
    }

    fun resolveAdjustedSize(desiredSize: Int, measureSpec: Int): Int {
        return View.getDefaultSize(desiredSize, measureSpec)
    }

    /**
     * Sets video path.
     *
     * @param path the path of the video.
     */
    fun setVideoPath(path: String) {
        setVideoURI(Uri.parse(path))
    }

    /**
     * Sets video URI using specific headers.
     *
     * @param uri     the URI of the video.
     * @param headers the headers for the URI request.
     * Note that the cross domain redirection is allowed by default, but that can be
     * changed with key/value pairs through the headers parameter with
     * "android-allow-cross-domain-redirect" as the key and "0" or "1" as the value
     * to disallow or allow cross domain redirection.
     */
    @JvmOverloads
    fun setVideoURI(uri: Uri, headers: Map<String, String>? = null) {
        mUri = uri
        mHeaders = headers
        mSeekWhenPrepared = 0
        openVideo()
        requestLayout()
        invalidate()
    }

    fun stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.release()
            mMediaPlayer = null
            mCurrentState = STATE_IDLE
            mTargetState = STATE_IDLE
            if (mShouldRequestAudioFocus) {
                val am = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                am!!.abandonAudioFocus(null)
            }
        }
        clearSurface()
    }

    /**
     * Clears the surface texture by attaching a GL context and clearing it.
     * Code taken from [Hugo Gresse's answer on stackoverflow.com](http://stackoverflow.com/a/31582209).
     */
    private fun clearSurface() {
        if (mSurface == null) {
            return
        }

        val egl = EGLContext.getEGL() as EGL10
        val display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
        egl.eglInitialize(display, null)

        val attribList = intArrayOf(EGL10.EGL_RED_SIZE, 8, EGL10.EGL_GREEN_SIZE, 8, EGL10.EGL_BLUE_SIZE, 8, EGL10.EGL_ALPHA_SIZE, 8, EGL10.EGL_RENDERABLE_TYPE, EGL10.EGL_WINDOW_BIT, EGL10.EGL_NONE, 0, // placeholder for recordable [@-3]
                EGL10.EGL_NONE)
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        egl.eglChooseConfig(display, attribList, configs, configs.size, numConfigs)
        val config = configs[0]
        val context = egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, intArrayOf(12440, 2, EGL10.EGL_NONE))
        val eglSurface = egl.eglCreateWindowSurface(display, config, mSurface, intArrayOf(EGL10.EGL_NONE))

        egl.eglMakeCurrent(display, eglSurface, eglSurface, context)
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        egl.eglSwapBuffers(display, eglSurface)
        egl.eglDestroySurface(display, eglSurface)
        egl.eglMakeCurrent(display, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT)
        egl.eglDestroyContext(display, context)
        egl.eglTerminate(display)
    }

    private fun openVideo() {
        if (mUri == null || mSurface == null) {
            // not ready for playback just yet, will try again later
            return
        }
        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        release(false)

        if (mShouldRequestAudioFocus) {
            val am = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            am!!.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }

        try {
            mMediaPlayer = MediaPlayer()

            if (mAudioSession != 0) {
                mMediaPlayer!!.audioSessionId = mAudioSession
            } else {
                mAudioSession = mMediaPlayer!!.audioSessionId
            }
            mMediaPlayer!!.setOnPreparedListener(mPreparedListener)
            mMediaPlayer!!.setOnVideoSizeChangedListener(mSizeChangedListener)
            mMediaPlayer!!.setOnCompletionListener(mCompletionListener)
            mMediaPlayer!!.setOnErrorListener(mErrorListener)
            mMediaPlayer!!.setOnInfoListener(mInfoListener)
            mMediaPlayer!!.setOnBufferingUpdateListener(mBufferingUpdateListener)
            mCurrentBufferPercentage = 0
            mMediaPlayer!!.setDataSource(context.applicationContext, mUri!!, mHeaders)
            mMediaPlayer!!.setSurface(mSurface)
            mMediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mMediaPlayer!!.isLooping = true
            mMediaPlayer!!.setVolume(mVolume, mVolume)
            mMediaPlayer!!.setScreenOnWhilePlaying(true)
            mMediaPlayer!!.prepareAsync()

            // we don't set the target state here either, but preserve the
            // target state that was there before.
            mCurrentState = STATE_PREPARING
            attachMediaController()
        } catch (ex: IOException) {
            Log.w(TAG, "Unable to open content: " + mUri!!, ex)
            mCurrentState = STATE_ERROR
            mTargetState = STATE_ERROR
            mErrorListener.onError(mMediaPlayer!!, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0)
            return
        } catch (ex: IllegalArgumentException) {
            Log.w(TAG, "Unable to open content: " + mUri!!, ex)
            mCurrentState = STATE_ERROR
            mTargetState = STATE_ERROR
            mErrorListener.onError(mMediaPlayer!!, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0)
            return
        }

    }

    fun getVolume(): Float = mVolume

    fun setVolume(vol: Float) {
        mVolume = vol
        mMediaPlayer?.let {
            it.setVolume(mVolume, mVolume)
        }
    }

    fun setMediaController(controller: MediaController) {
        if (mMediaController != null) {
            mMediaController!!.hide()
        }
        mMediaController = controller
        attachMediaController()
    }

    private fun attachMediaController() {
        if (mMediaPlayer != null && mMediaController != null) {
            mMediaController!!.setMediaPlayer(this)
            val anchorView = if (this.parent is View)
                this.parent as View
            else
                this
            mMediaController!!.setAnchorView(anchorView)
            mMediaController!!.isEnabled = isInPlaybackState
        }
    }

    /**
     * Register a callback to be invoked when the media file
     * is loaded and ready to go.
     *
     * @param l The callback that will be run
     */
    fun setOnPreparedListener(l: MediaPlayer.OnPreparedListener) {
        mOnPreparedListener = l
    }

    /**
     * Register a callback to be invoked when the end of a media file
     * has been reached during playback.
     *
     * @param l The callback that will be run
     */
    fun setOnCompletionListener(l: MediaPlayer.OnCompletionListener) {
        mOnCompletionListener = l
    }

    /**
     * Register a callback to be invoked when an error occurs
     * during playback or setup.  If no listener is specified,
     * or if the listener returned false, CenterCropVideoView will inform
     * the user of any errors.
     *
     * @param l The callback that will be run
     */
    fun setOnErrorListener(l: MediaPlayer.OnErrorListener) {
        mOnErrorListener = l
    }

    /**
     * Register a callback to be invoked when an informational event
     * occurs during playback or setup.
     *
     * @param l The callback that will be run
     */
    fun setOnInfoListener(l: MediaPlayer.OnInfoListener) {
        mOnInfoListener = l
    }

    /*
        * release the media player in any state
        */
    fun release(cleartargetstate: Boolean) {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.reset()
            mMediaPlayer!!.release()
            mMediaPlayer = null
            mCurrentState = STATE_IDLE
            if (cleartargetstate) {
                mTargetState = STATE_IDLE
            }
            if (mShouldRequestAudioFocus) {
                val am = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                am!!.abandonAudioFocus(null)
            }
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (isInPlaybackState && mMediaController != null) {
            toggleMediaControlsVisiblity()
        }
        return false
    }

    override fun onTrackballEvent(ev: MotionEvent): Boolean {
        if (isInPlaybackState && mMediaController != null) {
            toggleMediaControlsVisiblity()
        }
        return false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val isKeyCodeSupported = (keyCode != KeyEvent.KEYCODE_BACK &&
                keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                keyCode != KeyEvent.KEYCODE_MENU &&
                keyCode != KeyEvent.KEYCODE_CALL &&
                keyCode != KeyEvent.KEYCODE_ENDCALL)
        if (isInPlaybackState && isKeyCodeSupported && mMediaController != null) {
            if ((keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)) {
                if (mMediaPlayer!!.isPlaying) {
                    pause()
                    mMediaController!!.show()
                } else {
                    start()
                    mMediaController!!.hide()
                }
                return true
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mMediaPlayer!!.isPlaying) {
                    start()
                    mMediaController!!.hide()
                }
                return true
            } else if ((keyCode == KeyEvent.KEYCODE_MEDIA_STOP || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE)) {
                if (mMediaPlayer!!.isPlaying) {
                    pause()
                    mMediaController!!.show()
                }
                return true
            } else {
                toggleMediaControlsVisiblity()
            }
        }

        return super.onKeyDown(keyCode, event)
    }

    private fun toggleMediaControlsVisiblity() {
        if (mMediaController!!.isShowing) {
            mMediaController!!.hide()
        } else {
            mMediaController!!.show()
        }
    }

    override fun start() {
        if (isInPlaybackState) {
            mMediaPlayer!!.start()
            mCurrentState = STATE_PLAYING
        }
        mTargetState = STATE_PLAYING
    }

    override fun pause() {
        if (isInPlaybackState) {
            if (mMediaPlayer!!.isPlaying) {
                mMediaPlayer!!.pause()
                mCurrentState = STATE_PAUSED
            }
        }
        mTargetState = STATE_PAUSED
    }

    fun suspend() {
        release(false)
    }

    fun resume() {
        openVideo()
    }

    override fun getDuration(): Int {
        return if (isInPlaybackState) {
            mMediaPlayer!!.duration
        } else -1

    }

    override fun getCurrentPosition(): Int {
        return if (isInPlaybackState) {
            mMediaPlayer!!.currentPosition
        } else 0
    }

    override fun seekTo(msec: Int) {
        if (isInPlaybackState) {
            mMediaPlayer!!.seekTo(msec)
            mSeekWhenPrepared = 0
        } else {
            mSeekWhenPrepared = msec
        }
    }

    override fun isPlaying(): Boolean {
        return isInPlaybackState && mMediaPlayer!!.isPlaying
    }

    override fun getBufferPercentage(): Int {
        return if (mMediaPlayer != null) {
            mCurrentBufferPercentage
        } else 0
    }

    override fun canPause(): Boolean {
        return mCanPause
    }

    override fun canSeekBackward(): Boolean {
        return mCanSeekBack
    }

    override fun canSeekForward(): Boolean {
        return mCanSeekForward
    }

    override fun getAudioSessionId(): Int {
        if (mAudioSession == 0) {
            val foo = MediaPlayer()
            mAudioSession = foo.audioSessionId
            foo.release()
        }
        return mAudioSession
    }

    /**
     * Sets the request audio focus flag. If enabled, [CenterCropVideoView] will request
     * audio focus when opening a video by calling [AudioManager]. This flag
     * should be set before calling [CenterCropVideoView.setVideoPath] or
     * [CenterCropVideoView.setVideoURI]. By default, [CenterCropVideoView] will
     * request audio focus.
     *
     * @param shouldRequestAudioFocus  If `true`, [CenterCropVideoView] will request
     * audio focus before opening a video, else audio focus is not requested
     */
    fun setShouldRequestAudioFocus(shouldRequestAudioFocus: Boolean) {
        mShouldRequestAudioFocus = shouldRequestAudioFocus
    }

    /**
     * Returns the current state of the audio focus request flag.
     *
     * @return `true`, if [CenterCropVideoView] will request
     * audio focus before opening a video, else `false`
     */
    fun shouldRequestAudioFocus(): Boolean {
        return mShouldRequestAudioFocus
    }

    companion object {
        private const val TAG = "CenterCropVideoView"

        // all possible internal states
        private const val STATE_ERROR = -1
        private const val STATE_IDLE = 0
        private const val STATE_PREPARING = 1
        private const val STATE_PREPARED = 2
        private const val STATE_PLAYING = 3
        private const val STATE_PAUSED = 4
        private const val STATE_PLAYBACK_COMPLETED = 5
    }


}
