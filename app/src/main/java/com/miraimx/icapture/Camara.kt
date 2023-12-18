package com.miraimx.icapture

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface

import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.os.BuildCompat
import androidx.lifecycle.LifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.miraimx.icapture.ui.theme.ICaptureTheme
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.Executor

class Camara : ComponentActivity() {
    private var listaImaenes = mutableListOf<String>()
    @androidx.annotation.OptIn(BuildCompat.PrereleaseSdkCheck::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uid = intent.getStringExtra("uid")
        val projectName = intent.getStringExtra("projectName")
        setContent {
            ICaptureTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!uid.isNullOrEmpty() && !projectName.isNullOrEmpty()) {
                        Pantalla(uid, projectName, listaImaenes)
                    }
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (listaImaenes.isNotEmpty()) {
                    val datos = Intent()
                    val arrayList = ArrayList(listaImaenes)
                    datos.putExtra("listImagenes", arrayList)
                    setResult(Activity.RESULT_OK, datos)
                } else {
                    Toast.makeText(this@Camara, "La lista está vacias", Toast.LENGTH_SHORT).show()
                }
                finish()
            }
        })
    }

}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Pantalla(uid: String, projectName: String, listaImaenes: MutableList<String>) {
    val imagenUri = remember { mutableStateOf("") }
    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    LaunchedEffect(Unit) { permissionState.launchPermissionRequest() }

    val context = LocalContext.current
    val cameraController = remember {
        LifecycleCameraController(context)
    }
    val lifecycle = LocalLifecycleOwner.current

    Scaffold(modifier = Modifier.fillMaxSize()) {
        if (permissionState.status.isGranted) {
            Camera(modifier = Modifier.padding(it), cameraController, lifecycle)

            if (imagenUri.value.isNotEmpty()) {
                val file = File(imagenUri.value)
                val fileName = "$uid/$projectName/original/${System.currentTimeMillis()}.jpg"
                val fileNameMiniatura =
                    "$uid/$projectName/miniatura/${System.currentTimeMillis()}.jpg"

                //Se agrega a la lista de imagenes la ruta
                subirFoto(fileName, file).addOnSuccessListener {
                    //listaImaenes.add(fileName)
                    Toast.makeText(context, "Imagen subida", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    //Toast.makeText(context, "No se pudo subir la imagen", Toast.LENGTH_SHORT).show()
                }

                subirMiniatura(fileNameMiniatura, file).addOnSuccessListener {
                    listaImaenes.add(fileNameMiniatura)
                }.addOnFailureListener {
                    Toast.makeText(context, "No se pudo subir la imagen", Toast.LENGTH_SHORT).show()
                }
                file.delete()

            }

            // Coloca el botón en la parte inferior y en el centro de la pantalla
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                val executor = ContextCompat.getMainExecutor(context)
                FloatingActionButton(
                    onClick = {
                        tomarFoto(cameraController, executor, imagenUri)
                    },
                    modifier = Modifier
                        .padding(16.dp),
                ) {
                    Icon(
                        Icons.Filled.AddCircle,
                        contentDescription = "Camara"
                    ) // Agrega un ícono de cámara al botón
                }
            }
        } else {
            Text(text = "Permiso Denegado", modifier = Modifier.padding(it))
        }
    }
}

private fun tomarFoto(
    cameraController: LifecycleCameraController,
    executor: Executor,
    imagenUri: MutableState<String>,
) {
    val file = File.createTempFile("imagen", "jpg")
    val outPutDirectory = ImageCapture.OutputFileOptions.Builder(file).build()
    cameraController.takePicture(
        outPutDirectory,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                imagenUri.value = file.absolutePath
            }

            override fun onError(exception: ImageCaptureException) {
            }

        })
}


private fun subirFoto(fileName: String, file: File): UploadTask {
    val storageReference = FirebaseStorage.getInstance().reference.child(fileName)
    return storageReference.putFile(Uri.fromFile(file))
}

private fun subirMiniatura(fileName: String, file: File): UploadTask {
    val rutaArchivo = file.path
    // Crear un Bitmap a partir de la imagen original
    val bitmap = BitmapFactory.decodeFile(rutaArchivo)

    // Obteniendo orientación de la imagen
    val orientacion = obtenerOrientacion(rutaArchivo)

    // Rotar la imagen
    val imagenRotada = rotateBitmap(bitmap, orientacion)

    // Crear un Bitmap redimensionado
    val imageScaled: Bitmap = Bitmap.createScaledBitmap(
        imagenRotada,
        imagenRotada.width / 4,
        imagenRotada.height / 4,
        false
    )

    // Comprimir el Bitmap redimensionado en un ByteArrayOutputStream
    val baos = ByteArrayOutputStream()
    imageScaled.compress(Bitmap.CompressFormat.JPEG, 50, baos)

    // Convertir el ByteArrayOutputStream en un byte array
    val data = baos.toByteArray()

    // Obtener una referencia al lugar donde se subirá la imagen
    val storageReference = FirebaseStorage.getInstance().reference.child(fileName)

    // Subir la imagen redimensionada
    return storageReference.putBytes(data)
}

private fun obtenerOrientacion(ruta: String): Int {
    val exif = ExifInterface(ruta)
    val orientation =
        exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    var rotationDegrees = 0

    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotationDegrees = 90
        ExifInterface.ORIENTATION_ROTATE_180 -> rotationDegrees = 180
        ExifInterface.ORIENTATION_ROTATE_270 -> rotationDegrees = 270
    }
    return rotationDegrees
}

fun rotateBitmap(source: Bitmap, angle: Int): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(angle.toFloat())
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}

@Composable
fun Camera(
    modifier: Modifier = Modifier,
    cameraController: LifecycleCameraController,
    lifecycle: LifecycleOwner
) {
    cameraController.bindToLifecycle(lifecycle)
    AndroidView(modifier = modifier, factory = { context ->
        val previewView = PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        previewView.controller = cameraController
        previewView
    })
}


