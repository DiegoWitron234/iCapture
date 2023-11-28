package com.miraimx.icapture

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.miraimx.icapture.ui.theme.ICaptureTheme

class CambiarContraseniaViewModel : ViewModel() {

}

class CambiarContrasenia : ComponentActivity() {
    private val cambiarContraseniaViewModel = CambiarContraseniaViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ICaptureTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Greeting() {
    var isPasswordVisible by remember { mutableStateOf(false) }
    var currentEmail by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    val user = Firebase.auth.currentUser!!
    val context = LocalContext.current
    val activity = LocalContext.current as Activity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = currentEmail,
            onValueChange = { currentEmail = it },
            label = { Text("Correo") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.MailOutline,
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = currentPassword,
            onValueChange = { currentPassword = it },
            label = { Text("Contraseña actual") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = { isPasswordVisible = !isPasswordVisible }
                ) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Outlined.Check else Icons.Outlined.Lock,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("Nueva contraseña") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = { isPasswordVisible = !isPasswordVisible }
                ) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Outlined.Check else Icons.Outlined.Lock,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .fillMaxWidth()
        )

        OutlinedTextField(
            value = confirmarPassword,
            onValueChange = { confirmarPassword = it },
            label = { Text("Confirmar contraseña") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = { isPasswordVisible = !isPasswordVisible }
                ) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Outlined.Check else Icons.Outlined.Lock,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val credential = EmailAuthProvider.getCredential(currentEmail, currentPassword)
                user.reauthenticate(credential).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("Mensaje", "Usuario reautenticado.")
                            if (newPassword == confirmarPassword){
                                user.updatePassword(newPassword)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Log.d("Mensaje", "Contraseña del usuario cambiada.")
                                            Toast.makeText(
                                                context,
                                                "Contraseña cambiada",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            activity.finish()
                                        } else {
                                            Log.w(
                                                "Mensaje",
                                                "Cambio de contraseña fallido.",
                                                task.exception
                                            )
                                            Toast.makeText(
                                                context,
                                                "Ha ocurrido un error",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            }else{
                                Toast.makeText(
                                    context,
                                    "Las contraseñas no coinciden",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Log.w("Mensaje", "Reautenticación fallida.", task.exception)
                            Toast.makeText(
                                context,
                                "Por favor, verifica tus credenciales.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Cambiar Contraseña")
        }
    }
}
