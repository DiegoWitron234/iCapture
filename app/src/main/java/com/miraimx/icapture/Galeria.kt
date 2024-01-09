package com.miraimx.icapture

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.miraimx.icapture.ui.theme.ICaptureTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Galeria : ComponentActivity() {
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
                    if (uid != null && projectName != null) {
                        Galeria(uid = uid, projectName = projectName)
                    } else {
                        Toast.makeText(baseContext, "Ha ocurrido un error", Toast.LENGTH_SHORT)
                            .show()
                        finish()
                    }
                }
            }
        }
    }

}

@Composable
fun Galeria(uid: String, projectName: String) {
    val images = remember { mutableStateOf(emptyList<String>()) }
    val imagesLoaded = remember { mutableStateOf(false) }
    val context = LocalContext.current

    val activityResultLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->  //  Recibe los datos de la activity invocada
            if (result.resultCode == Activity.RESULT_OK) {
                val arrayList: ArrayList<String>? = result.data?.getStringArrayListExtra("listImagenes")
                if (arrayList != null) {
                    val imageUrls = mutableListOf<String>()
                    val storage = FirebaseStorage.getInstance() // Reutilizar la misma instancia de FirebaseStorage
                    arrayList.forEach { ruta ->
                        storage.reference.child(ruta).downloadUrl.addOnSuccessListener { uri ->
                            imageUrls.add(uri.toString())
                            if (arrayList.size == imageUrls.size) {
                                images.value += imageUrls
                            }
                        }
                    }
                    imagesLoaded.value = true
                }
            }
        }

    // Carga las imagenes de la galería
    LaunchedEffect(uid, projectName) {
        val storageReference = FirebaseStorage.getInstance().reference.child("$uid/$projectName/miniatura")
        storageReference.listAll()
            .addOnSuccessListener { listResult ->
                val imageUrls = mutableListOf<String>()
                listResult.items.forEach { storageReference ->
                    storageReference.downloadUrl.addOnSuccessListener { uri ->
                        imageUrls.add(uri.toString())
                        images.value = imageUrls
                        if (imageUrls.size == listResult.items.size) {
                            imagesLoaded.value = true
                        }
                    }
                }
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (imagesLoaded.value) {
            ImageGrid(images.value)
        } else {
            // Mostrar un indicador de carga mientras las imágenes se están cargando
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }

        FloatingActionButton(
            onClick = {
                // Crear un Intent para la actividad Camara
                val intent = Intent(context, Camara::class.java)

                // Pasar datos extras a la actividad Camara
                intent.putExtra("projectName", projectName)
                intent.putExtra("uid", uid)

                // Lanzar el IntentSenderRequest usando resultLauncher
                activityResultLauncher.launch(intent)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Filled.AddCircle, contentDescription = "Tomar foto")
        }
    }
}


@Composable
fun ImageGrid(images: List<String>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3), // Cambia el número para ajustar la cantidad de columnas
        contentPadding = PaddingValues(8.dp)
    ) {
        items(images) { imageUrl ->
            ImageItem(imageUrl)
        }
    }
}

@Composable
fun ImageItem(imageUrl: String) {
    val image: MutableState<Bitmap?> = remember { mutableStateOf(null) }
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }

    LaunchedEffect(imageUrl) {
        image.value = withContext(Dispatchers.IO) {
            Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .submit()
                .get()
        }
    }

    image.value?.let { bitmap ->
        Box(modifier = Modifier.aspectRatio(1f).clickable { showDialog.value = true }) {
            Image(bitmap = bitmap.asImageBitmap(), contentDescription = null)
        }

        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                title = { Text("Imagen seleccionada") },
                text = {
                    Column {
                        Image(
                            bitmap = bitmap.asImageBitmap(), contentDescription = null,
                            modifier = Modifier.fillMaxWidth()
                            //SIGO MODIFICANDO, NO ESTÁ LISTO
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IconButton(onClick = { /* código para editar la foto */ }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }

                            IconButton(onClick = { /* código para descargar la foto */ }) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Descargar")
                            }

                            IconButton(onClick = { /* código para eliminar la foto */ }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showDialog.value = false }) {
                        Text("Cerrar")
                    }
                }
            )
        }
    }


}