package com.denicks21.recorder

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RawRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewAssetLoader.ResourcesPathHandler
import androidx.webkit.WebViewClientCompat
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class CardActivity : AppCompatActivity() {
    lateinit var bthCardChildren: TextView
    lateinit var bthCardPopo: TextView
    lateinit var bthCardChildrenPlayback: TextView
    lateinit var bthCardPopoPlayback: TextView
    private var isChildrenRecording: Boolean = true
    private var isPopoRecording: Boolean = true
    var mFileName: File? = null
    private var mRecorder: MediaRecorder? = null

    private var mPlayer = MediaPlayer().apply {
        setOnPreparedListener { start() }
        setOnCompletionListener {
            reset()
        }
    }
    private val mediaPlayer = MediaPlayer().apply {
        setOnPreparedListener {
            start()
            //statusTV.text = "錄音進行中"
        }
        setOnCompletionListener {
            reset()
            //statusTV.text = ""
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_buttons)
        val myWebView = findViewById<View>(R.id.webview) as WebView
        //myWebView.loadUrl("./hosp_eula.html")

        myWebView.settings.javaScriptEnabled = true

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .addPathHandler("/res/", ResourcesPathHandler(this))
            .build()
        myWebView.webViewClient = LocalContentWebViewClient(assetLoader)
        myWebView.loadUrl("https://appassets.androidplatform.net/assets/index.html")

        bthCardChildren = findViewById(R.id.btnCardChildren)

        bthCardChildren.setOnClickListener {
            if (isChildrenRecording) {
                isChildrenRecording = false
                bthCardChildren.setBackgroundResource(R.drawable.btn_rec_stop_play)
                //playSound(R.raw.what)
                startRecording("q.ogg")
            } else {
                isChildrenRecording = true
                bthCardChildren.setBackgroundResource(R.drawable.btn_rec_play)
                pauseRecording()
                playAudio("q.ogg")
            }
        }

        bthCardChildrenPlayback = findViewById(R.id.btnCardChildrenPlayback)

        bthCardChildrenPlayback.setOnClickListener {
            if (!isChildrenRecording) {
                isChildrenRecording = true
                bthCardChildren.setBackgroundResource(R.drawable.btn_rec_play)
                //pauseRecording()
            }
            //playSound(R.raw.what)
            playAudio("q.ogg")
        }

        bthCardPopo = findViewById(R.id.btnCardPopo)

        bthCardPopo.setOnClickListener {
            if (isPopoRecording) {
                isPopoRecording = false
                bthCardPopo.setBackgroundResource(R.drawable.btn_rec_stop_play)
                //playSound(R.raw.what)
                playAudio("q.ogg")
                startRecording("what.ogg")
            } else {
                isPopoRecording = true
                bthCardPopo.setBackgroundResource(R.drawable.btn_rec_play)
                pauseRecording()
                copyRecording()
                playAudio("what.ogg")
            }

            bthCardPopoPlayback = findViewById(R.id.btnCardPopoPlayback)

            bthCardPopoPlayback.setOnClickListener {
                if (!isPopoRecording) {
                    isPopoRecording = true
                    bthCardPopo.setBackgroundResource(R.drawable.btn_rec_play)
                    pauseRecording()
                    copyRecording()
                }
                playAudio("what.ogg")
            }
        }
    }

    fun playSound(@RawRes rawResId: Int) {
        val assetFileDescriptor = applicationContext.resources.openRawResourceFd(rawResId) ?: return
        mediaPlayer.run {
            reset()
            setDataSource(assetFileDescriptor.fileDescriptor, assetFileDescriptor.startOffset, assetFileDescriptor.declaredLength)
            prepareAsync()
        }
    }

    fun playAudio(filename: String) {
        mFileName = File(externalCacheDir?.absolutePath, filename)
        mPlayer.run {
            reset()
            setDataSource(mFileName.toString())
            prepareAsync()
        }
    }

    private fun startRecording(filename: String) {
        // Check permissions
        if (CheckPermissions()) {
            mFileName = File(externalCacheDir?.absolutePath, filename)
            Log.i("POPO", "startRecording:$mFileName")
            mRecorder = MediaRecorder()

            // Set source to get audio
            mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)

            // Set the format of the file
            mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.OGG)

            // Set the audio encoder
            mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)

            // Set the save path
            mRecorder!!.setOutputFile(mFileName)
            //mRecorder!!.setOutputFile(assetFileDescriptor!!.fileDescriptor)
            try {
                // Preparation of the audio file
                mRecorder!!.prepare()
            } catch (e: IOException) {
                Log.e("POPO", "prepare() failed")
            }
            // Start the audio recording
            mRecorder!!.start()

            Log.e("POPO", "錄音進行中")
            //Log.i("POPO", "valid:${assetFileDescriptor!!.fileDescriptor.valid()}")
            //assetFileDescriptor!!.close()
        } else {
            // Request permissions
            RequestPermissions()
        }
    }

    fun pauseRecording() {
        // Stop recording
        //if (mFileDescriptor == null) {
        if (mFileName == null) {
            Log.i("POPO", "pauseRecording mFileDescriptor null")
            // Message
            Toast.makeText(applicationContext, "Registration not started", Toast.LENGTH_LONG).show()

        } else {
            Log.i("POPO", "pauseRecording mRecorder stop $mFileName")
            mRecorder!!.stop()

            // Message to confirm save file
            Log.i("POPO", "save file:$mFileName")

            // Release the class mRecorder
            mRecorder!!.release()
            mRecorder = null
        }
    }

    fun copyRecording() {
        if (mFileName != null) {
            val savedUri = Uri.fromFile(mFileName)
            val msg = "File saved: " + savedUri!!.lastPathSegment
            Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
            copyToPublicDirectory(savedUri!!.lastPathSegment!!)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // If permissions accepted ->
        when (requestCode) {
            MainActivity.REQUEST_AUDIO_PERMISSION_CODE -> if (grantResults.size > 0) {
                val permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (permissionToRecord && permissionToStore) {

                    // Message
                    Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_LONG).show()

                } else {

                    // Message
                    Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun CheckPermissions(): Boolean {
        // Check permissions
        val result =
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val result1 = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

    private fun RequestPermissions() {

        // Request permissions
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            MainActivity.REQUEST_AUDIO_PERMISSION_CODE
        )
    }

    @Throws(IOException::class)
    private fun copyToPublicDirectory(filename: String) {
        val simpleDateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
        val currentDateAndTime: String = simpleDateFormat.format(Date())
        File(externalCacheDir, filename).copyTo(File("/storage/emulated/0/Music/$currentDateAndTime-$filename"));
        val inputStream: InputStream = createInputStream(filename)
        val outputStream = createOutputStream(filename)
        copy(inputStream, outputStream)
    }

    @Throws(IOException::class)
    private fun createInputStream(filename: String): InputStream {
        val cacheDirectory = externalCacheDir
            ?: throw RuntimeException("Cache is not currently available")
        return FileInputStream(File(cacheDirectory, filename))
    }

    fun createOutputStream(fileName: String): OutputStream? {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        val extVolumeUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        // query for the file
        val cursor = contentResolver.query(
            extVolumeUri,
            null,
            MediaStore.MediaColumns.DISPLAY_NAME + " = ?",
            arrayOf(fileName),
            null
        )

        var fileUri: Uri? = null

        // if file found
        if (cursor != null && cursor.count > 0) {
            // get URI
            while (cursor.moveToNext()) {
                val nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                if (nameIndex > -1) {
                    val displayName = cursor.getString(nameIndex)
                    if (displayName == fileName) {
                        val idIndex = cursor.getColumnIndex(MediaStore.MediaColumns._ID)
                        if (idIndex > -1) {
                            val id = cursor.getLong(idIndex)
                            fileUri = ContentUris.withAppendedId(extVolumeUri, id)
                        }
                    }
                }
            }

            cursor.close()
        } else {
            // insert new file otherwise
            fileUri = contentResolver.insert(extVolumeUri, contentValues)
        }

        return contentResolver.openOutputStream(fileUri!!, "wt")
            ?: throw RuntimeException("Cannot open uri: $fileUri")
    }

    @Throws(IOException::class)
    private fun createOutputStreamorig(filename: String): OutputStream? {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        val contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var uri = contentResolver.insert(contentUri, contentValues)
            ?: throw RuntimeException("Cannot insert file: $filename")
        Log.i("POPO", "output file save at $uri")
        return contentResolver.openOutputStream(uri)
            ?: throw RuntimeException("Cannot open uri: $uri")
    }

    @Throws(IOException::class)
    private fun copy(source: InputStream, target: OutputStream?) {
        val buffer = ByteArray(8192)
        var length: Int
        while (source.read(buffer).also { length = it } != -1) {
            target!!.write(buffer, 0, length)
        }
    }

    companion object {
        const val REQUEST_AUDIO_PERMISSION_CODE = 1
    }
}

private class LocalContentWebViewClient(private val assetLoader: WebViewAssetLoader) : WebViewClientCompat() {
    @RequiresApi(21)
    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        return assetLoader.shouldInterceptRequest(request.url)
    }

    // to support API < 21
    override fun shouldInterceptRequest(
        view: WebView,
        url: String
    ): WebResourceResponse? {
        return assetLoader.shouldInterceptRequest(Uri.parse(url))
    }
}

