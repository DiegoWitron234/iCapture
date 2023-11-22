package com.miraimx.icapture

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.tooling.preview.Preview
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
    var showDialog by remember { mutableStateOf(false) }
    var projectName by remember { mutableStateOf(TextFieldValue("")) }
    var selectedProject by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Botón para crear un nuevo proyecto
        IconButton(onClick = { showDialog = true }) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Crear Proyecto")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de proyectos
        ProjectListScreen(onItemClick = { selectedProject = it })

        // Diálogo para crear un nuevo proyecto
        if (showDialog) {
            CreateProjectDialog(
                projectName = projectName,
                onValueChange = { projectName = it },
                onConfirmClick = {
                    if (projectName.text.isNotBlank()) {
                        // Agregar el código para crear un nuevo proyecto
                        createNewProject(projectName.text)
                        showDialog = false
                    } else {
                        // Mostrar un mensaje al usuario indicando que el nombre del proyecto no puede estar en blanco
                        Toast.makeText(context, "El nombre del proyecto no puede estar en blanco", Toast.LENGTH_SHORT).show()
                    }
                },
                onDismissClick = { showDialog = false }
            )
        }
    }
}

@Composable
fun ProjectList(projects: List<String>, onItemClick: (String) -> Unit) {
    // Mostrar la lista de proyectos
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(projects) { project ->
            ProjectListItem(project = project, onItemClick = onItemClick)
        }
    }
}

@Composable
fun ProjectListItem(project: String, onItemClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = project,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        IconButton(onClick = { onItemClick(project) }) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar")
        }
    }
}

@Composable
fun ProjectListScreen(onItemClick: (String) -> Unit) {
    val projectList by getProjectListForUser().observeAsState(initial = emptyList())

    if (projectList.isEmpty()) {
        Text("Aún no se han creado proyectos", style = MaterialTheme.typography.bodyMedium)
    } else {
        ProjectList(projects = projectList, onItemClick = onItemClick)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectDialog(
    projectName: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissClick,
        title = { Text("Nuevo Proyecto") },
        text = {
            OutlinedTextField(
                value = projectName,
                onValueChange = onValueChange,
                label = { Text("Nombre del Proyecto") }
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmClick) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissClick) {
                Text("Cancelar")
            }
        }
    )
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