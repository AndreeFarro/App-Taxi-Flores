package com.uns.taxiflores.fragment

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.uns.taxiflores.R
import com.uns.taxiflores.databinding.FragmentProfileBinding
import com.uns.taxiflores.models.Client
import com.uns.taxiflores.models.Driver
import com.uns.taxiflores.providers.AuthProvider
import com.uns.taxiflores.providers.ClientProvider

import java.io.File


class ProfileFragment : Fragment() {

    private var afterImageProfile: String = ""
    val clientProvider = ClientProvider()
    val authProvider = AuthProvider()
    private val modalMenu = ModalBottomSheetMenu()

    private var _binding : FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var imageFile : File? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        getClient()
        binding.imageViewBack.setOnClickListener { showModalMenu()}
        binding.btnUpdate.setOnClickListener { updateInfo()}
        binding.circleImageProfile.setOnClickListener { selectImage() }

        return binding.root
    }

    private fun showModalMenu(){
        findNavController().navigate(R.id.action_profileFragment_to_map)
    }

    private fun updateInfo(){
        val name = binding.textFieldName.text.toString()
        val lastName = binding.textFieldLastName.text.toString()
        val phone = binding.textFieldPhone.text.toString()


        val client= Client(
            id = authProvider.getId(),
            name = name,
            lastName = lastName,
            phone = phone,
            image = afterImageProfile
        )

        if (imageFile != null){
            clientProvider.uploadImage(authProvider.getId(),imageFile!!).addOnSuccessListener{ taskSnapshot ->
                clientProvider.getImageUrl().addOnSuccessListener { url ->
                    val imageUrl = url.toString()
                    client.image = imageUrl
                    update(client)
                    Log.d("STORAGE", "URL: ${imageUrl}")
                }
            }
        }else{
            update(client)
        }

    }

    private fun update(client: Client){
        clientProvider.update(client).addOnCompleteListener{
            if (it.isSuccessful){
                Toast.makeText(context, "Datos Actualizados", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context, "No se pudo actualizar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getClient(){
        clientProvider.getClientById(authProvider.getId()).addOnSuccessListener { document ->
            if (document.exists()){
                val client = document.toObject(Driver::class.java)
                binding.textViewEmail.text = client?.email
                binding.textFieldName.setText(client?.name)
                binding.textFieldLastName.setText(client?.lastName)
                binding.textFieldPhone.setText(client?.phone)
                afterImageProfile = client?.image!!
               if (client?.image != null){
                    if (client.image != ""){
                        Glide.with(requireContext()).load(client.image).into(binding.circleImageProfile)
                    }
                }
            }
        }
    }

    private val startImageForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result : ActivityResult ->

        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == Activity.RESULT_OK){
            val fileUri = data?.data
            imageFile = File(fileUri?.path)
            binding.circleImageProfile.setImageURI(fileUri)
        }
        else if(resultCode == ImagePicker.RESULT_ERROR){
            Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_LONG).show()
        }
        else{
            Toast.makeText(context,"Tarea Cancelada", Toast.LENGTH_LONG).show()
        }
    }

    private fun selectImage(){
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080,1080)
            .createIntent { intent ->
                startImageForResult.launch(intent)
            }
    }


}