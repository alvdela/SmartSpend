package com.alvdela.smartspend.ui.activity

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.exifinterface.media.ExifInterface
import com.alvdela.smartspend.ContextFamily
import com.alvdela.smartspend.R
import com.alvdela.smartspend.databinding.ActivityCameraBinding
import com.alvdela.smartspend.model.Member
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class CameraActivity : AppCompatActivity() {
    companion object{
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
        private const val REQUEST_PHOTO = 12

        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }

    private var FILENAME : String = ""
    private lateinit var binding : ActivityCameraBinding

    private var preview: Preview? = null

    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var cameraProvider: ProcessCameraProvider? = null

    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File

    private lateinit var cameraExecutor: ExecutorService

    private lateinit var user: String
    private var member: Member? = null

    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras
        user = bundle?.getString("USER_NAME").toString()
        member = ContextFamily.family!!.getMember(user)

        cameraExecutor = Executors.newSingleThreadExecutor()

        outputDirectory = getOutputDirectory()
        binding.cameraCaptureButton.setOnClickListener {  takePhoto() }

        binding.cameraSwitchButton.setOnClickListener {
            lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing){
                CameraSelector.LENS_FACING_BACK
            }else{
                CameraSelector.LENS_FACING_FRONT
            }
            bindCamera()
        }

        binding.photoViewButton.setOnClickListener {
            selectPhotoFromGallery(this,REQUEST_PHOTO)
        }

        if (allPermissionsGranted()) startCamera()
        else ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_PHOTO && resultCode == Activity.RESULT_OK){
            if (data != null){
                val uri = data.data
                if (uri != null){
                    val photoFile = File (outputDirectory, "$FILENAME.jpg")

                    val inputStream = this.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val scaledBitmap = cropImage(bitmap)
                    saveBitmapToFile(scaledBitmap, photoFile)

                    showPopUp(R.layout.pop_up_profile_photo)
                    dialog.setCanceledOnTouchOutside(false)
                    val imagePreview = dialog.findViewById<ImageView>(R.id.imagePreview)
                    imagePreview.setImageBitmap(scaledBitmap)

                    val cancelPhotoButton = dialog.findViewById<Button>(R.id.cancelPhotoButton)
                    cancelPhotoButton.setOnClickListener {
                        dialog.dismiss()
                    }

                    val confirmPhotoButton = dialog.findViewById<Button>(R.id.confirmPhotoButton)
                    confirmPhotoButton.setOnClickListener {
                        if (!ContextFamily.isMock){
                            uploadFile(photoFile)
                        }
                        dialog.dismiss()
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<out String>,grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS){
            if (allPermissionsGranted()) startCamera()
            else{
                Toast.makeText(this, "Debes proporcionar permisos si quieres tomar fotos", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all{
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }


    private fun bindCamera(){
        //val metrics = DisplayMetrics().also { binding.viewFinder.display.getRealMetrics(it) }

        val screenAspectRatio = AspectRatio.RATIO_4_3
        val rotation = binding.viewFinder.display.rotation

        val cameraProvider = cameraProvider ?: throw IllegalStateException("Fallo al iniciar la camara")

        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        preview = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        cameraProvider.unbindAll()

        try{
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        }catch(exc: Exception){
            Log.e("SmartSpend", "Fallo al vincular la camara", exc)
        }
    }

    private fun aspectRadio(width: Int, height: Int): Int{
        val previewRatio = max(width, height).toDouble() / min(width, height)

        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)){
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    private fun startCamera(){
        val cameraProviderFinnaly = ProcessCameraProvider.getInstance(this)
        cameraProviderFinnaly.addListener(Runnable {

            cameraProvider = cameraProviderFinnaly.get()

            lensFacing = when{
                hasBackCamera() -> CameraSelector.LENS_FACING_BACK
                hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
                else -> throw IllegalStateException("No existe camara")
            }

            manageSwitchButton()

            bindCamera()

        }, ContextCompat.getMainExecutor(this))


    }

    private fun hasBackCamera(): Boolean{
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }
    private fun hasFrontCamera(): Boolean{
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }
    private fun manageSwitchButton(){
        val switchButton = binding.cameraSwitchButton
        try {
            switchButton.isEnabled = hasBackCamera() && hasFrontCamera()

        }catch (exc: CameraInfoUnavailableException){
            switchButton.isEnabled = false
        }

    }

    private fun getOutputDirectory(): File{
        val mediaDir = externalMediaDirs.firstOrNull()?.let{
            File(it, "SmartSpend").apply {  mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir

    }
    private fun takePhoto(){
        FILENAME = getString(R.string.app_name) + UUID.randomUUID()

        val photoFile = File (outputDirectory, "$FILENAME.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object:ImageCapture.OnImageSavedCallback{
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                    val savedUri = Uri.fromFile(photoFile)

                    setGalleryThumbnail(savedUri)
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    val rotatedBitmap = rotateImageIfRequired(bitmap, photoFile)

                    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(savedUri.toFile().extension)
                    MediaScannerConnection.scanFile(
                        baseContext,
                        arrayOf(savedUri.toFile().absolutePath),
                        arrayOf(mimeType)
                    ){ _, _ ->

                    }

                    val scaledBitmap = cropImage(rotatedBitmap)

                    saveBitmapToFile(scaledBitmap, photoFile)

                    showPopUp(R.layout.pop_up_profile_photo)
                    dialog.setCanceledOnTouchOutside(false)
                    val imagePreview = dialog.findViewById<ImageView>(R.id.imagePreview)
                    imagePreview.setImageBitmap(scaledBitmap)

                    val cancelPhotoButton = dialog.findViewById<Button>(R.id.cancelPhotoButton)
                    cancelPhotoButton.setOnClickListener {
                        dialog.dismiss()
                        val myFile = File(photoFile.absolutePath)
                        myFile.delete()
                    }

                    val confirmPhotoButton = dialog.findViewById<Button>(R.id.confirmPhotoButton)
                    confirmPhotoButton.setOnClickListener {
                        if (!ContextFamily.isMock){
                            uploadFile(photoFile)
                        }
                        dialog.dismiss()
                    }

                }

                override fun onError(exception: ImageCaptureException) {
                    val clMain = findViewById<ConstraintLayout>(R.id.cameraMain)
                    Snackbar.make(clMain, "Error al guardar la imagen", Snackbar.LENGTH_LONG).setAction("OK"){
                        clMain.setBackgroundColor(Color.CYAN)
                    }.show()
                }
            })
    }

    private fun saveBitmapToFile(bitmap: Bitmap, file: File) {
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
    }

    private fun cropImage(bitmap: Bitmap): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height

        val squareSize = Math.min(originalWidth, originalHeight)

        val x = (originalWidth - squareSize) / 2
        val y = (originalHeight - squareSize) / 2

        return Bitmap.createBitmap(bitmap, x, y, squareSize, squareSize)
    }

    private fun rotateImageIfRequired(img: Bitmap, selectedImage: File): Bitmap {
        val ei = ExifInterface(selectedImage.absolutePath)
        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270)
            ExifInterface.ORIENTATION_NORMAL -> img
            else -> img
        }
    }

    private fun rotateImage(img: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        return Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
    }

    private fun uploadFile(image: File) {
        val fileName = member!!.getId()
        val uuid = FirebaseAuth.getInstance().currentUser!!.uid

        val path = "images/$uuid/$fileName"

        val storageReference = FirebaseStorage.getInstance().getReference(path)

        storageReference.putFile(Uri.fromFile(image))
            .addOnSuccessListener {

                val myFile = File(image.absolutePath)
                myFile.delete()

                Toast.makeText(this,"Imagen de perfil actualizada.", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener{
                Toast.makeText(this,"Fallo al cargar la imagen. Imagen guardada en local.", Toast.LENGTH_LONG).show()
            }
    }

    private fun setGalleryThumbnail(uri: Uri){
        val thumbnail = binding.photoViewButton
        thumbnail.post {
            Glide.with (thumbnail)
                .load(uri)
                .apply(RequestOptions.circleCropTransform())
                .into(thumbnail)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun showPopUp(layout: Int) {
        dialog = Dialog(this)
        dialog.setContentView(layout)
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun selectPhotoFromGallery(activity: Activity, code: Int){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        activity.startActivityForResult(intent, code)
    }
}
