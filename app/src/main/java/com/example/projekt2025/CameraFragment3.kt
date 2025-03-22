package com.example.projekt2025

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.projekt2025.ml.Model300
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.BufferedReader
import java.io.InputStreamReader

class CameraFragment3 : Fragment() {

    private val paint = Paint()
    private lateinit var textureView: TextureView
    private lateinit var cameraManager: CameraManager
    private lateinit var handler: Handler
    private lateinit var cameraDevice: CameraDevice
    private lateinit var imageView: ImageView
    private lateinit var bitmap: Bitmap
    private lateinit var model: Model300
    private lateinit var resultTextView: TextView
    private lateinit var camera2ViewModel: Camera2ViewModel

    private val emotionColors = mapOf(
        "Happy" to Color.GREEN,
        "Angry" to Color.RED,
        "Sad" to Color.BLUE,
        "Surprise" to Color.YELLOW
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camera3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        camera2ViewModel = ViewModelProvider(requireActivity()).get(Camera2ViewModel::class.java)

        textureView = view.findViewById(R.id.textureView)
        imageView = view.findViewById(R.id.imageView)
        resultTextView = view.findViewById(R.id.resultTextView)

        model = Model300.newInstance(requireContext())
        cameraManager = requireContext().getSystemService(CameraManager::class.java)

        val handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                openCamera()
            }

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {}

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean { return false }

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
                processImage()
            }
        }

        requestPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        model.close()
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        cameraManager.openCamera(cameraManager.cameraIdList[0], object : CameraDevice.StateCallback() {
            override fun onOpened(p0: CameraDevice) {
                cameraDevice = p0
                val surfaceTexture = textureView.surfaceTexture
                val surface = Surface(surfaceTexture)
                val captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                captureRequest.addTarget(surface)
                cameraDevice.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(p0: CameraCaptureSession) {
                        p0.setRepeatingRequest(captureRequest.build(), null, null)
                    }
                    override fun onConfigureFailed(session: CameraCaptureSession) {}
                }, handler)
            }
            override fun onDisconnected(p0: CameraDevice) {}
            override fun onError(p0: CameraDevice, p1: Int) {}
        }, null)
    }

    private fun processImage() {
        val tempBitmap = textureView.bitmap ?: return
        bitmap = tempBitmap
        val grayscaleBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(grayscaleBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(48, 48, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0f, 256f))
            .build()

        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(grayscaleBitmap)
        tensorImage = imageProcessor.process(tensorImage)

        val inputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 48, 48, 3), DataType.FLOAT32)
        inputBuffer.loadBuffer(tensorImage.buffer)

        val outputs = model.process(inputBuffer)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer
        val rawOutput = outputFeature0.floatArray

        if (rawOutput.isNotEmpty()) {
            val emotionIndex = rawOutput[0]
            val emotionText = getEmotionLabel(emotionIndex)
            resultTextView.text = emotionText

            // Pošaljite detektiranu emociju ViewModelu
            camera2ViewModel.updateEmotionCount(emotionIndex)
        }
    }

    private fun drawBoundingBox(bitmap: Bitmap, emotion: String) {
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = emotionColors[emotion] ?: Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }

        //Koordinatne točke za bounding box
        // Ovo je samo primjer, u stvarnosti bi model trebao vratiti stvarne koordinate
        val left = 100f
        val top = 100f
        val right = 100f
        val bottom = 100f

        // Crtanje bounding boxa
        canvas.drawRect(left, top, right, bottom, paint)
    }

    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 101)
        }
    }

    private fun getEmotionLabel(index: Float): String {
        val emotionList = mutableListOf<String>()
        try {
            val inputStream = requireContext().assets.open("labelmap.txt")
            val reader = BufferedReader(InputStreamReader(inputStream))
            reader.forEachLine { line -> emotionList.add(line) }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emotionList.getOrElse(index.toInt()) { "Nepoznato" }
    }
}

