package com.example.projekt2025

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.projekt2025.ml.LiteModelMovenetSingleposeLightningTfliteFloat164
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import kotlin.random.Random

class CameraActivity2 : AppCompatActivity() {

    val paint = Paint()
    val linePaint = Paint()
    lateinit var handler: Handler
    lateinit var handlerThread: HandlerThread
    lateinit var textureView: TextureView
    lateinit var cameraManager: CameraManager
    lateinit var imageView: ImageView
    lateinit var bitmap: Bitmap
    lateinit var model: LiteModelMovenetSingleposeLightningTfliteFloat164
    lateinit var imageProcessor: ImageProcessor

    val colors = listOf(
        Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.CYAN,
        Color.MAGENTA, Color.LTGRAY, Color.DKGRAY, Color.WHITE, Color.BLACK
    )

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_camera2)

        get_permisssion()

        imageProcessor = ImageProcessor.Builder().add(ResizeOp(192, 192, ResizeOp.ResizeMethod.BILINEAR)).build()
        model = LiteModelMovenetSingleposeLightningTfliteFloat164.newInstance(this)
        imageView = findViewById(R.id.imageView)
        textureView = findViewById(R.id.textureView)

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        paint.setColor(Color.GREEN)
        paint.style = Paint.Style.FILL

        linePaint.color = Color.GREEN
        linePaint.strokeWidth = 5f
        linePaint.style = Paint.Style.STROKE

        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                p0: SurfaceTexture,
                p1: Int,
                p2: Int
            ) {
                open_camera()
            }

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {}

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
                bitmap = textureView.bitmap!!

                var tensorImage = TensorImage(DataType.UINT8)

                tensorImage.load(bitmap)
                tensorImage = imageProcessor.process(tensorImage)

                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 192, 192, 3), DataType.UINT8)
                inputFeature0.loadBuffer(tensorImage.buffer)

                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer

                // Dobijanje float array-a sa metodom getFloatArray()
                val outputFloatArray = outputFeature0.floatArray

                var mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)

                val canvas = Canvas(mutable)

                val h = bitmap.height
                val w = bitmap.width
                var x = 0

                Log.d("output__", outputFloatArray.size.toString())

                val keypoints = mutableListOf<Pair<Float, Float>>()

                while (x <= 49) {
                    if (outputFloatArray[x + 2] > 0.45) {
                        val xPos = outputFloatArray[x + 1] * w
                        val yPos = outputFloatArray[x] * h
                        keypoints.add(Pair(xPos, yPos))

                        // Nacrtaj točku
                        canvas.drawCircle(xPos, yPos, 10f, paint)
                    } else {
                        keypoints.add(Pair(-1f, -1f))
                    }
                    x += 3
                }

                // Lista povezanih ključnih točaka
                val bodyJoints = listOf(
                    Pair(0, 1),  // Nos → Desno unutrašnje oko
                    Pair(0, 2),  // Nos → Levo unutrašnje oko
                    Pair(1, 3),  // Desno unutrašnje oko → Desno spoljašnje oko
                    Pair(2, 4),  // Levo unutrašnje oko → Levo spoljašnje oko
                    Pair(0, 5),  // Nos → Desno uho
                    Pair(0, 6),  // Nos → Levo uho
                    Pair(5, 7),  // Desno rame → Desni lakat
                    Pair(7, 9),  // Desni lakat → Desni ručni zglob
                    Pair(6, 8),  // Lijevo rame → Lijevi lakat
                    Pair(8, 10), // Lijevi lakat → Lijevi ručni zglob
                    Pair(5, 6),  // Desno rame → Levo rame (ramena povezana)
                    Pair(5, 11), // Desno rame → Desni kuk
                    Pair(6, 12), // Lijevo rame → Lijevi kuk
                    Pair(11, 12), // Desni kuk → Levi kuk
                    Pair(11, 13), // Desni kuk → Desno koleno
                    Pair(13, 15), // Desno koleno → Desno stopalo
                    Pair(12, 14), // Lijevi kuk → Lijevo koleno
                    Pair(14, 16)  // Lijevo koleno → Lijevo stopalo
                )

                // Iscrtavanje linija između ključnih točaka
                for ((index, pair) in bodyJoints.withIndex()) {
                    val point1 = keypoints[pair.first]
                    val point2 = keypoints[pair.second]

                    if (point1.first != -1f && point2.first != -1f) {
                        linePaint.color = colors[index % colors.size] // Koristi različite boje
                        canvas.drawLine(point1.first, point1.second, point2.first, point2.second, linePaint)
                    }
                }

                imageView.setImageBitmap(mutable)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        model.close()
    }

    @SuppressLint("MissingPermission")
    fun open_camera() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        cameraManager.openCamera(cameraManager.cameraIdList[0], object : CameraDevice.StateCallback() {
            override fun onOpened(p0: CameraDevice) {
                val captureRequest = p0.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                val surface = Surface(textureView.surfaceTexture)
                captureRequest.addTarget(surface)

                p0.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(p0: CameraCaptureSession) {
                        p0.setRepeatingRequest(captureRequest.build(), null, null)
                    }

                    override fun onConfigureFailed(p0: CameraCaptureSession) {}
                }, handler)
            }

            override fun onDisconnected(p0: CameraDevice) {}

            override fun onError(p0: CameraDevice, error: Int) {}
        }, handler)
    }

    fun get_permisssion() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 101)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) get_permisssion()
    }
}
