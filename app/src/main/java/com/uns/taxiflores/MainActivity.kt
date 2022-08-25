package com.uns.taxiflores.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.uns.taxiflores.databinding.ActivityMainBinding
import com.uns.taxiflores.providers.AuthProvider

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val authProvider = AuthProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        binding.btnRegister.setOnClickListener { goToRegister() }
        binding.btnLogin.setOnClickListener { login() }

    }

    private fun login(){
        val email = binding.textFieldEmail.text.toString()
        val password = binding.textFieldPassword.text.toString()

        if(isValidForm(email,password)){
            authProvider.login(email,password).addOnCompleteListener{
                if (it.isSuccessful){
                    goToMap()
                }else{
                    Toast.makeText(this@MainActivity, "Error al ingresar", Toast.LENGTH_SHORT).show()
                    Log.d("FIREBASE","ERROR: ${it.exception.toString()}")
                }
            }
        }
    }
    private  fun goToMap(){
        val i = Intent(this,MapActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }

    private fun isValidForm(email:String, password:String) : Boolean{
        if(email.isEmpty()){
            Toast.makeText(this, "Ingresar su Correo Electronico", Toast.LENGTH_SHORT).show()
            return false
        }
        if(password.isEmpty()){
            Toast.makeText(this, "Ingresar su Contraseña", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun goToRegister(){
        val i= Intent(this, RegisterActivity::class.java)
        startActivity(i)
    }

    override fun onStart() {
        super.onStart()
        if (authProvider.existSession()){
            goToMap()
        }
    }

}