package com.uns.taxiflores.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.uns.taxiflores.MainActivity
import com.uns.taxiflores.databinding.FragmentRegisterBinding
import com.uns.taxiflores.models.Client
import com.uns.taxiflores.R
import com.uns.taxiflores.providers.AuthProvider
import com.uns.taxiflores.providers.ClientProvider

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null

    private val binding get() = _binding!!

    private val authProvider = AuthProvider()
    private val clientProvider = ClientProvider()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnGoToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
                            Toast.makeText(requireContext(),"Registro exitoso",Toast.LENGTH_SHORT).show()
                            goToMap()
                        }else{
                            Toast.makeText(requireContext(),"Hubo un error Almacenando los datos del usuario ${it.exception.toString()}",Toast.LENGTH_SHORT).show()
                            Log.d("FIREBASE", "error: ${it.exception.toString()}")
                        }
                    }

                }else{
                    Toast.makeText(requireContext(), "Registro fallido ${it.exception.toString()}", Toast.LENGTH_LONG).show()
                    Log.d("FIREBASE","ERROR: ${it.exception.toString()}")
                }
            }
        }

    }

    /*private  fun goToMap(){
        val i = Intent(this,MapFragment::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)

    }*/

    private  fun goToMap(){
        findNavController().navigate(R.id.action_login_to_map)
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
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun goToLogin(){
        findNavController().navigate(R.id.action_register_to_login)
    }

}