package com.uns.taxiflores.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.uns.taxiflores.databinding.ActivityRegisterBinding
import com.uns.taxiflores.models.Client
import com.uns.taxiflores.providers.AuthProvider
import com.uns.taxiflores.providers.ClientProvider

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val authProvider = AuthProvider()
    private val clientProvider = ClientProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        binding.btnGoToLogin.setOnClickListener {goToLogin()}

        binding.btnRegister.setOnClickListener{ register() }
    }

    private fun register(){
        val name = binding.textFieldName.text.toString()
        val lastName = binding.textFieldLastName.text.toString()
        val email = binding.textFieldEmail.text.toString()
        val phone = binding.textFieldPhone.text.toString()
        val password = binding.textFieldPassword.text.toString()
        val confirmPassword = binding.textFieldConfirmPassword.text.toString()

        if(isValidForm(name,lastName, email, phone, password, confirmPassword)){
            authProvider.register(email,password).addOnCompleteListener{
                if (it.isSuccessful){
                    val client= Client(
                        id = authProvider.getId(),
                        name = name,
                        lastName = lastName,
                        email = email,
                        phone = phone
                    )
                    clientProvider.create(client).addOnCompleteListener{
                        if(it.isSuccessful){
                            Toast.makeText(this@RegisterActivity,"Registro exitoso",Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(this@RegisterActivity,"Hubo un error Almacenando los datos del usuario ${it.exception.toString()}",Toast.LENGTH_SHORT).show()
                            Log.d("FIREBASE", "error: ${it.exception.toString()}")
                        }
                    }

                }else{
                    Toast.makeText(this@RegisterActivity, "Registro fallido ${it.exception.toString()}", Toast.LENGTH_LONG).show()
                    Log.d("FIREBASE","ERROR: ${it.exception.toString()}")
                }
            }
        }

    }

    private fun isValidForm(
        name:String,
        lastname:String,
        email:String,
        phone:String,
        password:String,
        confirmPassword:String
    ): Boolean{

        if (
            name.isEmpty()      ||
            lastname.isEmpty()  ||
            email.isEmpty()     ||
            phone.isEmpty()     ||
            password.isEmpty()  ||
            confirmPassword.isEmpty()
        ){
            notification("Llene Todos los campos")
            return false
        }


        if (phone.length!=9){
            notification("Numero de celular no valido")
            return false
        }
        if(password != confirmPassword){
            notification("Las contraseñas deben coincidir")
            return false
        }
        if (password.length < 6){
            notification("La contraseña debe ser mayor a 6 carateres")
            return false
        }

        return true
    }

    private fun notification(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun goToLogin(){
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
    }
}