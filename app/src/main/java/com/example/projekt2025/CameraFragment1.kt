package com.example.projekt2025

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.projekt2025.ml.SsdMobilenetV11Metadata1
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp



class CameraFragment1 : Fragment() {

    lateinit var labels: List<String>

    var colors = listOf(
        Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.GRAY, Color.BLACK,
        Color.DKGRAY, Color.MAGENTA, Color.YELLOW, Color.RED
    )

    private val paint = Paint()
    private lateinit var textureView: TextureView
    private lateinit var cameraManager: CameraManager
    private lateinit var handler: Handler
    private var cameraDevice: CameraDevice? = null
    private lateinit var imageView: ImageView
    private lateinit var bitmap: Bitmap
    private lateinit var model: SsdMobilenetV11Metadata1
    private lateinit var imageProcessor: ImageProcessor
    private lateinit var camera1ViewModel: Camera1ViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_camera1, container, false)
        textureView = view.findViewById(R.id.textureView)
        imageView = view.findViewById(R.id.imageView)

        camera1ViewModel = ViewModelProvider(requireActivity()).get(Camera1ViewModel::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        model = SsdMobilenetV11Metadata1.newInstance(requireContext())

        val handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        try {
            labels = requireContext().assets.open("labels.txt").bufferedReader().readLines()
        } catch (e: Exception) {
            Log.e("ERROR", "Nije moguće učitati labels.txt", e)
            labels = listOf("Unknown")
        }

        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surfaceTexture: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 101)
                }
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                processFrame()
            }
        }

        cameraManager = requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager
        getPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        model.close()
        cameraDevice?.close()
    }

    @SuppressLint("MissingPermission")
    fun openCamera() {
        if (!this::textureView.isInitialized || textureView.surfaceTexture == null) {
            Log.e("ERROR", "TextureView nije spreman!")
            return
        }

        try {
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    val surfaceTexture = textureView.surfaceTexture ?: return
                    val surface = Surface(surfaceTexture)

                    val captureRequest = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                    captureRequest.addTarget(surface)

                    camera.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            session.setRepeatingRequest(captureRequest.build(), null, handler)
                        }

                        override fun onConfigureFailed(session: CameraCaptureSession) {
                            Log.e("ERROR", "Neuspelo konfiguriranje kamere")
                        }
                    }, handler)
                }

                override fun onDisconnected(camera: CameraDevice) {
                    Log.e("ERROR", "Kamera je prekinuta")
                    camera.close()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    Log.e("ERROR", "Greška pri otvaranju kamere: $error")
                    camera.close()
                }
            }, handler)
        } catch (e: Exception) {
            Log.e("ERROR", "Greška pri otvaranju kamere", e)
        }
    }

    private fun getPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 101)
        }
    }

    private fun processFrame() {
        val tempBitmap = textureView.bitmap ?: return
        bitmap = tempBitmap

        var image = TensorImage.fromBitmap(bitmap)
        image = imageProcessor.process(image)

        val outputs = model.process(image)
        val locations = outputs.locationsAsTensorBuffer.floatArray
        val classes = outputs.classesAsTensorBuffer.floatArray
        val scores = outputs.scoresAsTensorBuffer.floatArray

        val detectedObjects = mutableMapOf<String, Int>()

        val mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutable)

        val h = mutable.height
        val w = mutable.width
        paint.textSize = h / 15f
        paint.strokeWidth = h / 85f



        scores.forEachIndexed { index, score ->
            val x = index * 4
            if (score > 0.5 && x + 3 < locations.size && index < classes.size) {
                paint.color = colors[index % colors.size]
                paint.style = Paint.Style.STROKE
                canvas.drawRect(
                    RectF(
                        locations[x + 1] * w, locations[x] * h,
                        locations[x + 3] * w, locations[x + 2] * h
                    ), paint
                )
                paint.style = Paint.Style.FILL
                val labelIndex = classes[index].toInt()
                val labelText = if (labelIndex < labels.size) labels[labelIndex] else "Unknown"
                canvas.drawText("$labelText ${score.format(2)}", locations[x + 1] * w, locations[x] * h, paint)
                detectedObjects[labelText] = detectedObjects.getOrDefault(labelText, 0) + 1

            }
        }
        imageView.setImageBitmap(mutable)
        camera1ViewModel.updateObjectCounts(detectedObjects)

    }

    private fun Float.format(digits: Int) = "%.${digits}f".format(this)
}