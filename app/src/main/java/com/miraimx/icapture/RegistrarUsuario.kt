package com.miraimx.icapture

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.miraimx.icapture.ui.theme.ICaptureTheme

class RegistroViewModel : ViewModel() {
    private val auth = Firebase.auth
    val registroExitosoEvent = MutableLiveData<Boolean>()
    var email = mutableStateOf("")
    var password = mutableStateOf("")
    var verificarpassword = mutableStateOf("")
    var mensajeConfirmacion = mutableStateOf("")

    fun registrarse() {
        if (email.value.isNotEmpty() && password.value.isNotEmpty() && password.value == verificarpassword.value) {
            auth.createUserWithEmailAndPassword(email.value, password.value)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        registroExitosoEvent.value = true
                        //mensajeConfirmacion.value = "Registro exitoso"
                    } else {
                        mensajeConfirmacion.value = "No se pudo realizar el registro"
                    }
                }
        } else {
            mensajeConfirmacion.value = "Por favor, verifica tus datos"
        }
    }
}


class RegistrarUsuario : ComponentActivity() {
    private val viewModel by viewModels<RegistroViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ICaptureTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RegistroScreen(viewModel)
                }
            }
        }

        viewModel.registroExitosoEvent.observe(this) { isSuccessful ->
            if (isSuccessful) {
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(viewModel: RegistroViewModel) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Logo
        Image(
            painter = painterResource(id = R.drawable.logoicapture),
            contentDescription = "Logo",
            modifier = Modifier
                .size(120.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.primary)
                .padding(16.dp)
        )

        // Titulo
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Registro de usuario",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp
            )
        }

        // Campo de correo electrónico
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = viewModel.email.value,
            onValueChange = { viewModel.email.value = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = viewModel.email.value.isEmpty()
        )

        // Campo de contraseña
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = viewModel.password.value,
            onValueChange = { viewModel.password.value = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = viewModel.password.value.isEmpty()
        )

        // Campo de Verificar contraseña
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = viewModel.verificarpassword.value,
            onValueChange = { viewModel.verificarpassword.value = it },
            label = { Text("Confirmar contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = viewModel.verificarpassword.value != viewModel.password.value && viewModel.verificarpassword.value.isNotEmpty()
        )

        // Mensaje de verificación
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = viewModel.mensajeConfirmacion.value,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        // Boton de registro
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { viewModel.registrarse() }) {
            Text(
                text = "Registrarse",
                fontSize = 20.sp
            )
        }

    }
}


