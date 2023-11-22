package com.miraimx.icapture

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.miraimx.icapture.ui.theme.ICaptureTheme


class PanelProyectos : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ICaptureTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PanelProyectosScreen()
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelProyectosScreen() {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var projectName by remember { mutableStateOf(TextFieldValue("")) }
    //var selectedProject by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { showDialog = true }) {
            Text("Crear Proyecto")
        }
        Spacer(modifier = Modifier.height(8.dp))


        ProjectListScreen()


        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Nuevo Proyecto") },
                text = {
                    OutlinedTextField(
                        value = projectName,
                        onValueChange = { projectName = it },
                        label = { Text("Nombre del Proyecto") }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (projectName.text.isNotBlank()) {
                            // Agregar el código para crear un nuevo proyecto
                            createNewProject(projectName.text)
                            showDialog = false
                        } else {
                            // Mostrar un mensaje al usuario indicando que el nombre del proyecto no puede estar en blanco
                            Toast.makeText(context, "El nombre del proyecto no puede estar en blanco", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

/*
@Composable
fun ProjectList(onItemClick: (String) -> Unit) {
    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
    val projectList: MutableList<String> = mutableListOf()

    if (currentUserUid != null) {
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("$currentUserUid")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (projectSnapshot in snapshot.children) {
                    // Agregar el nombre del proyecto a la lista
                    projectList.add(projectSnapshot.key!!)
                    Log.d("Mensaje", "Proyecto encontrado: ${projectSnapshot.key}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar errores, si es necesario
                println("Error al obtener la lista de proyectos: ${error.message}")
            }
        })
    }

    // Mostrar la lista de proyectos
    // onItemClick se llama cuando se hace clic en un elemento
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(projectList) { project ->
            TextButton(
                onClick = { onItemClick(project) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(project)
            }
            Divider()
        }
    }
}
 */

@Composable
fun ProjectList(projects: List<String>, onItemClick: (String) -> Unit) {
    // Mostrar la lista de proyectos
    // onItemClick se llama cuando se hace clic en un elemento
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(projects) { project ->
            TextButton(
                onClick = { onItemClick(project) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(project)
            }
            Divider()
        }
    }
}

@Composable
fun ProjectListScreen() {
    var selectedProject by remember { mutableStateOf("") }
    val projectList by getProjectListForUser().observeAsState(initial = emptyList())

    if (projectList.isEmpty()) {
        Text("Aún no se han creado proyectos")
    } else {
        ProjectList(projectList) { selectedProject = it }
    }
}


fun getProjectListForUser(): LiveData<List<String>> {
    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
    val projectList: MutableLiveData<List<String>> = MutableLiveData()

    if (currentUserUid != null) {
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("$currentUserUid")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<String>()
                for (projectSnapshot in snapshot.children) {
                    // Agregar el nombre del proyecto a la lista
                    list.add(projectSnapshot.key!!)
                }
                projectList.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar errores, si es necesario
                println("Error al obtener la lista de proyectos: ${error.message}")
            }
        })
    }

    return projectList
}


// Agregar el nuevo proyecto a la base de datos
fun createNewProject(projectName: String) {
    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

    if (currentUserUid != null) {
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("$currentUserUid/$projectName")

        // Asignar un valor vacío al proyecto (puedes asignar algún valor específico si es necesario)
        databaseReference.setValue("")
            .addOnSuccessListener {
                // Manejar el éxito de la operación, si es necesario
                println("Nuevo proyecto creado: $projectName")
                //Toast.makeText(it, "Nuevo proyecto creado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Manejar el fallo de la operación, si es necesario
                println("Error al crear el nuevo proyecto: ${e.message}")
            }
    }
}