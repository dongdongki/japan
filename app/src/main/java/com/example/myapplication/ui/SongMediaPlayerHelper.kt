package com.example.myapplication.ui

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import java.io.IOException

/**
 * SongLyricsFragment의 MediaPlayer 로직을 분리한 헬퍼 클래스
 * MediaPlayer 생명주기 관리 및 재생 제어 담당
 */
class SongMediaPlayerHelper(
    private val context: Context,
    private val songDirectory: String
) {
    private var mediaPlayer: MediaPlayer? = null
    private var playbackHandler: Handler? = null
    private var stopPlaybackRunnable: Runnable? = null
    private var currentLyricIndex: Int = -1

    /**
     * MediaPlayer 초기화 결과 콜백
     */
    interface InitCallback {
        fun onInitialized()
        fun onError(message: String)
    }

    /**
     * MediaPlayer 초기화
     */
    fun initialize(callback: InitCallback? = null) {
        try {
            val assetManager = context.assets

            val mp3Path = try {
                assetManager.openFd("$songDirectory/$songDirectory.mp3").also { it.close() }
                "$songDirectory/$songDirectory.mp3"
            } catch (e: IOException) {
                try {
                    assetManager.openFd("$songDirectory/${songDirectory.lowercase()}.mp3").also { it.close() }
                    "$songDirectory/${songDirectory.lowercase()}.mp3"
                } catch (e2: IOException) {
                    throw IOException("MP3 파일을 찾을 수 없습니다: $songDirectory", e)
                }
            }

            val afd: AssetFileDescriptor = assetManager.openFd(mp3Path)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                prepare()
            }
            afd.close()

            android.util.Log.d("SongMediaPlayerHelper", "MediaPlayer initialized: $mp3Path")
            callback?.onInitialized()
        } catch (e: IOException) {
            android.util.Log.e("SongMediaPlayerHelper", "MP3 파일 로드 실패", e)
            mediaPlayer = null
            callback?.onError("MP3 파일 로드 실패: ${e.message}")
        }
    }

    /**
     * 가사 구간 재생
     * @param index 가사 인덱스
     * @param startTimestamp 시작 타임스탬프 (mm:ss.s)
     * @param endTimestamp 끝 타임스탬프 (mm:ss.s)
     * @param lyricView 하이라이트할 뷰
     * @param previousLyricView 이전 하이라이트된 뷰 (null일 경우 무시)
     */
    fun playSection(
        index: Int,
        startTimestamp: String,
        endTimestamp: String,
        lyricView: View,
        previousLyricView: View?
    ) {
        if (mediaPlayer == null) {
            android.util.Log.w("SongMediaPlayerHelper", "MediaPlayer가 초기화되지 않았습니다")
            return
        }

        try {
            // 기존 핸들러 및 리스너 정리 (중복 실행 방지, 메모리 누수 방지)
            stopPlaybackRunnable?.let { playbackHandler?.removeCallbacks(it) }
            mediaPlayer?.setOnSeekCompleteListener(null)

            val startTimeInSeconds = parseTimestamp(startTimestamp)
            val startTimeInMillis = (startTimeInSeconds * 1000).toInt()
            val endTimeInSeconds = parseTimestamp(endTimestamp)
            val endTimeInMillis = (endTimeInSeconds * 1000).toInt()

            // 이전에 재생 중이던 가사 하이라이트 제거
            previousLyricView?.setBackgroundColor(Color.TRANSPARENT)

            // 현재 재생 중인 가사 하이라이트
            currentLyricIndex = index
            lyricView.setBackgroundColor(ContextCompat.getColor(context, R.color.lyric_highlight_background))

            mediaPlayer?.apply {
                if (isPlaying) {
                    pause()
                }

                setOnSeekCompleteListener { mp ->
                    val actualPosition = mp.currentPosition
                    android.util.Log.d(
                        "SongMediaPlayerHelper",
                        "Seek complete: requested=$startTimeInMillis, actual=$actualPosition"
                    )

                    mp.start()

                    if (playbackHandler == null) {
                        playbackHandler = Handler(Looper.getMainLooper())
                    }

                    stopPlaybackRunnable = object : Runnable {
                        override fun run() {
                            try {
                                val currentPos = mediaPlayer?.currentPosition ?: 0

                                if (currentPos >= endTimeInMillis) {
                                    android.util.Log.d(
                                        "SongMediaPlayerHelper",
                                        "Playback stopped: currentPos=$currentPos, endTime=$endTimeInMillis"
                                    )

                                    mediaPlayer?.apply {
                                        if (isPlaying) {
                                            pause()
                                        }
                                        setOnSeekCompleteListener(null)
                                    }

                                    lyricView.setBackgroundColor(Color.TRANSPARENT)
                                } else {
                                    playbackHandler?.postDelayed(this, 100)
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("SongMediaPlayerHelper", "Position check error", e)
                            }
                        }
                    }

                    stopPlaybackRunnable?.let { runnable ->
                        playbackHandler?.postDelayed(runnable, 100)
                    }

                    android.util.Log.d(
                        "SongMediaPlayerHelper",
                        "재생 시작: $startTimestamp ~ $endTimestamp"
                    )
                }

                seekTo(startTimeInMillis)
            }
        } catch (e: Exception) {
            android.util.Log.e("SongMediaPlayerHelper", "재생 오류", e)
        }
    }

    /**
     * 현재 하이라이트된 가사 인덱스 반환
     */
    fun getCurrentLyricIndex(): Int = currentLyricIndex

    /**
     * 일시정지
     */
    fun pause() {
        stopPlaybackRunnable?.let { playbackHandler?.removeCallbacks(it) }
        mediaPlayer?.apply {
            if (isPlaying) {
                pause()
                android.util.Log.d("SongMediaPlayerHelper", "재생 일시정지")
            }
        }
    }

    /**
     * 리소스 해제
     */
    fun release() {
        // 모든 콜백 정리
        stopPlaybackRunnable?.let { playbackHandler?.removeCallbacks(it) }
        playbackHandler?.removeCallbacksAndMessages(null)
        playbackHandler = null
        stopPlaybackRunnable = null

        // 모든 리스너 해제 후 MediaPlayer 릴리즈
        mediaPlayer?.apply {
            setOnSeekCompleteListener(null)
            setOnCompletionListener(null)
            setOnErrorListener(null)
            setOnPreparedListener(null)
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        currentLyricIndex = -1
    }

    /**
     * 타임스탬프를 초 단위로 변환
     */
    private fun parseTimestamp(timestamp: String): Float {
        val parts = timestamp.split(":")
        return when (parts.size) {
            2 -> {
                val minutes = parts[0].toIntOrNull() ?: 0
                val seconds = parts[1].toFloatOrNull() ?: 0f
                (minutes * 60 + seconds)
            }
            3 -> {
                val hours = parts[0].toIntOrNull() ?: 0
                val minutes = parts[1].toIntOrNull() ?: 0
                val seconds = parts[2].toFloatOrNull() ?: 0f
                (hours * 3600 + minutes * 60 + seconds)
            }
            else -> 0f
        }
    }

    companion object {
        /**
         * 타임스탬프 형식으로 변환
         */
        fun formatTimestamp(timeInSeconds: Float): String {
            val minutes = (timeInSeconds / 60).toInt()
            val seconds = timeInSeconds % 60
            return String.format("%02d:%04.1f", minutes, seconds)
        }

        /**
         * 타임스탬프 유효성 검사
         */
        fun isValidTimestamp(timestamp: String): Boolean {
            val pattern = Regex("^\\d{1,2}:\\d{2}(\\.\\d)?$")
            return pattern.matches(timestamp)
        }

        /**
         * 끝 시간 계산 (시작 + 5초)
         */
        fun calculateEndTime(startTime: String?): String {
            if (startTime == null) return "00:05.0"
            val parts = startTime.split(":")
            val startSeconds = when (parts.size) {
                2 -> {
                    val minutes = parts[0].toIntOrNull() ?: 0
                    val seconds = parts[1].toFloatOrNull() ?: 0f
                    (minutes * 60 + seconds)
                }
                else -> 0f
            }
            val endSeconds = startSeconds + 5.0f
            val minutes = (endSeconds / 60).toInt()
            val seconds = endSeconds % 60
            return String.format("%02d:%04.1f", minutes, seconds)
        }
    }
}
