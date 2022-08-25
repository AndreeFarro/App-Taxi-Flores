package com.uns.taxiflores.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.uns.taxiflores.R
import com.uns.taxiflores.databinding.FragmentLoginBinding
import com.uns.taxiflores.providers.AuthProvider

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val authProvider = AuthProvider()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener { login() }
        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }
    }

    private fun login(){
        val email = _binding?.textFieldEmail?.text.toString()
        val password = _binding?.textFieldPassword?.text.toString()

        if(isValidForm(email,password)){
            authProvider.login(email,password).addOnCompleteListener{
                if (it.isSuccessful){
                    goToMap()
                    Toast.makeText(context, "ingreso de sesion", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(context, "Error al ingresar", Toast.LENGTH_SHORT).show()
                    Log.d("FIREBASE","ERROR: ${it.exception.toString()}")
                }
            }
        }
    }

    private fun isValidForm(email:String, password:String) : Boolean{
        if(email.isEmpty()){
            Toast.makeText(context, "Ingresar su Correo Electronico", Toast.LENGTH_SHORT).show()
            return false
        }
        if(password.isEmpty()){
            Toast.makeText(context, "Ingresar su Contraseña", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private  fun goToMap(){
        findNavController().navigate(R.id.action_login_to_map)
    }

    override fun onStart() {
        super.onStart()
        if (authProvider.existSession()){
            goToMap()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}