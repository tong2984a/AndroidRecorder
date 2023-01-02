package com.denicks21.recorder

import android.Manifest.permission
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RawRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.*


class MainActivity : AppCompatActivity() {
    lateinit var startTV: TextView
    lateinit var stopTV: TextView
    lateinit var playTV: TextView
    lateinit var stopplayTV: TextView
    lateinit var statusTV: TextView
    private var mRecorder: MediaRecorder? = null
    private var mPlayer = MediaPlayer().apply {
        setOnPreparedListener { start() }
        setOnCompletionListener {
            reset()
            statusTV.text = ""
        }
    }
    private val mediaPlayer = MediaPlayer().apply {
        setOnPreparedListener {
            start()
            statusTV.text = "錄音進行中"
        }
        setOnCompletionListener {
            reset()
            //statusTV.text = ""
        }
    }
    lateinit var grandma: ImageView
    var mFileName: File? = null
    var mFileDescriptor: FileDescriptor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusTV = findViewById(R.id.idTVstatus)
        //startTV = findViewById(R.id.btnRecord) //
        stopTV = findViewById(R.id.btnStop) //
        playTV = findViewById(R.id.btnPlay) //
        //stopplayTV = findViewById(R.id.btnStopPlay) //
        grandma = findViewById(R.id.imageView2)
/*
        startTV.setOnClickListener {
            animateGrandma()
            playSound(R.raw.where)
            startRecording("where.ogg")
        }*/

        stopTV.setOnClickListener {
            animateGrandma()
            //pauseRecording()
            //playSound(R.raw.whom)
            //startRecording("whom.ogg")

            val intent = Intent(this, CardBeautifulActivity::class.java)
            startActivity(intent)
        }

        playTV.setOnClickListener {
            animateGrandma()
            //playAudio()
            //playSound(R.raw.what)
            //startRecording("what.ogg")

            val intent = Intent(this, CardActivity::class.java)
            startActivity(intent)
        }
/*
        stopplayTV.setOnClickListener {
            //pausePlaying()
            pauseRecording()
            playAudio()
        }
        */
    }

    private fun animateGrandma() {
        grandma.animate().apply {
            duration = 2000
            alpha(.5f)
            scaleXBy(.5f)
            scaleYBy(.5f)
            rotationYBy(360f)
            translationYBy(200f)
        }.withEndAction{
            grandma.animate().apply {
                duration = 2000
                alpha(1f)
                scaleXBy(-.5f)
                scaleYBy(-.5f)
                rotationYBy(360f)
                translationYBy(-200f)
            }
        }.start()
    }

    private fun queryAllFiles() {
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME)
        val selection = "${MediaStore.Audio.Media.DISPLAY_NAME} = \"Yahoo - Snip Snap.m4a\""
        //val selectionArgs = arrayOf(fileName)
        applicationContext.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                val displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                Log.i("POPO", "id:$id")
                val contentUri = Uri.withAppendedPath(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, displayName
                )
                Log.i("POPO", "contentUri:$contentUri")
            }
        }
    }

    private fun startRecording(filename: String) {
        // Check permissions
        if (CheckPermissions()) {
            /*
            val contentUri = Uri.withAppendedPath(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, filename
            )
            Log.i("POPO", "contentUri displayname:${contentUri.path}")
            */

            //val assetFileDescriptor = applicationContext.contentResolver.openAssetFileDescriptor(contentUri, "rwt")

            //mFileName = File("/Music/$filename")
            // Initialize the class MediaRecorder
            //Log.i("POPO", "Build.VERSION.SDK_INT ${Build.VERSION.SDK_INT}")
            //Log.i("POPO", "Build.VERSION_CODES.S ${Build.VERSION_CODES.S}")
/*            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
                Log.i("POPO", "MediaRecorder applicationContext")
                mRecorder = MediaRecorder(applicationContext)
            } else {
                Log.i("POPO", "no application context ")
                mRecorder = MediaRecorder()
            }*/

            // /storage/emulated/0/Android/data/com.denicks21.recorder/files/where.m4a
            // save file:/storage/emulated/0/Android/data/com.denicks21.recorder/cache/where.ogg
            //mFileName = File(getExternalFilesDir("")?.absolutePath,filename)
            /*
            mFileName = File("/storage/emulated/0/Android/media/$filename")
            if (mFileName!!.exists()) {
                Log.i("POPO", "$mFileName delete")
                mFileName!!.delete()
            }
            */
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
            statusTV.text = "錄音進行中" //"Recording in progress"

            Log.e("POPO", "錄音進行中")
            //Log.i("POPO", "valid:${assetFileDescriptor!!.fileDescriptor.valid()}")
            //assetFileDescriptor!!.close()
        } else {
            // Request permissions
            RequestPermissions()
        }
    }

    private fun startRecordingold(filename: String) {
        // Check permissions
        if (CheckPermissions()) {
            Log.i("POPO", "filename:$filename")
            val values = ContentValues(1)
            values.put(MediaStore.MediaColumns.TITLE, filename)
            //values.put(MediaStore.MediaColumns.MIME_TYPE, recorder.getMimeContentType());
            val base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val newUri = applicationContext.contentResolver.insert(base, values)

            //val path: String = applicationContext.contentResolver.getDataFilePath(newUri)
            //Log.i("POPO", "uri:"  + uri.toString())
            //val uri2 = getExternalFilesDir("")?.absolutePath
            Log.i("POPO", "newUri:$newUri")

            mFileName = File("${newUri.toString()}/${filename}")
            //mFileName = File(getExternalFilesDir("")?.absolutePath,filename)
            if (mFileName!!.exists()) {
                Log.i("POPO", "mFileName!!.exists() delete")
                mFileName!!.delete()
            }
            Log.i("POPO", "startRecording:$mFileName")

            // If file exists then increment counter
            /*
            var n = 0
            while (mFileName!!.exists()) {
                n++
                mFileName = File(getExternalFilesDir("")?.absolutePath,"Record$n.3gp")
            }

             */

            // Initialize the class MediaRecorder
            Log.i("POPO", "Build.VERSION.SDK_INT ${Build.VERSION.SDK_INT}")
            Log.i("POPO", "Build.VERSION_CODES.S ${Build.VERSION_CODES.S}")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Log.i("POPO", "MediaRecorder applicationContext")
                mRecorder = MediaRecorder(applicationContext)
            } else {
                Log.i("POPO", "no application context ")
                mRecorder = MediaRecorder()
            }

            // Set source to get audio
            mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)

            // Set the format of the file
            mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)

            // Set the audio encoder
            mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            // Set the save path
            mRecorder!!.setOutputFile(mFileName)
            try {
                // Preparation of the audio file
                mRecorder!!.prepare()
            } catch (e: IOException) {
                Log.e("TAG", "prepare() failed")
            }
            // Start the audio recording
            mRecorder!!.start()
            statusTV.text = "錄音進行中" //"Recording in progress"
        } else {
            // Request permissions
            RequestPermissions()
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
            REQUEST_AUDIO_PERMISSION_CODE -> if (grantResults.size > 0) {
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
            ContextCompat.checkSelfPermission(applicationContext, permission.WRITE_EXTERNAL_STORAGE)
        val result1 = ContextCompat.checkSelfPermission(applicationContext, permission.RECORD_AUDIO)
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

    private fun RequestPermissions() {

        // Request permissions
        ActivityCompat.requestPermissions(this,
            arrayOf(permission.RECORD_AUDIO, permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_AUDIO_PERMISSION_CODE)
    }

    fun playSound(@RawRes rawResId: Int) {
        val assetFileDescriptor = applicationContext.resources.openRawResourceFd(rawResId) ?: return
        mediaPlayer.run {
            reset()
            setDataSource(assetFileDescriptor.fileDescriptor, assetFileDescriptor.startOffset, assetFileDescriptor.declaredLength)
            prepareAsync()
        }
    }

    fun playAudio() {
        mPlayer.run {
            reset()
            setDataSource(mFileName.toString())
            //setDataSource(mFileDescriptor)
            prepareAsync()
        }

        // Use the MediaPlayer class to listen to recorded audio files
        /*
        mPlayer = MediaPlayer()
        try {
            // Preleva la fonte del file audio
            mPlayer!!.setDataSource(mFileName.toString())

            // Fetch the source of the mPlayer
            mPlayer!!.prepare()

            // Start the mPlayer
            mPlayer!!.start()
            statusTV.text = "在播放中" //"Listening recording"
        } catch (e: IOException) {
            Log.e("TAG", "prepare() failed")
        }

         */
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

            val savedUri = Uri.fromFile(mFileName)
            val msg = "File saved: " + savedUri!!.lastPathSegment
            Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
            copyToPublicDirectory(savedUri!!.lastPathSegment!!)
            statusTV.text = "在播放中" //"Interrupted recording"
        }
    }

    fun pauseRecordingold() {
        // Stop recording
        if (mFileName == null) {
            Log.i("POPO", "pauseRecording mFileName null")
            // Message
            Toast.makeText(applicationContext, "Registration not started", Toast.LENGTH_LONG).show()

        } else {
            Log.i("POPO", "pauseRecording mRecorder stop $mFileName")
            mRecorder!!.stop()

            // Message to confirm save file
            val savedUri = Uri.fromFile(mFileName)
            val msg = "File saved: " + savedUri!!.lastPathSegment
            Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()

            // Release the class mRecorder
            mRecorder!!.release()
            mRecorder = null
            statusTV.text = "在播放中" //"Interrupted recording"
        }
    }

    fun pausePlaying() {
        // Stop playing the audio file
        statusTV.text = "Listening to interrupted recording"
    }

    @Throws(IOException::class)
    private fun copyToPublicDirectory(filename: String) {
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
