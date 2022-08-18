package com.uns.taxiflores

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import com.uns.taxiflores.databinding.ActivityMainBinding
import com.uns.taxiflores.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

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
        val lastname = binding.textFieldLastName.text.toString()
        val email = binding.textFieldEmail.text.toString()
        val phone = binding.textFieldPhone.text.toString()
        val password = binding.textFieldPassword.text.toString()
        val confirmPassword = binding.textFieldConfirmPassword.text.toString()

        if(isValidForm(name,lastname, email, phone, password, confirmPassword)){
            notification("Form valido")
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