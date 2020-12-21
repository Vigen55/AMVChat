package com.example.amvchat.Fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Notification
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.example.amvchat.ModelClasses.Users
import com.example.amvchat.R
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_settings.view.*

class SettingsFragment : Fragment() {

    lateinit var userRefrence: DatabaseReference
    var fuser: FirebaseUser? = null

    private var storageReference: StorageReference? = null
    private var imageUri: Uri? = null

    private val RECUESTCODE = 438
    private var coverchecker:String? = ""
    private var socialchecker:String? = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_settings, container, false)


        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storageReference = FirebaseStorage.getInstance().reference.child("User Images")

        fuser = FirebaseAuth.getInstance().currentUser

        userRefrence = FirebaseDatabase.getInstance().getReference("users").child(fuser?.uid!!)
        userRefrence.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
              if (dataSnapshot.exists()){
                  val user:Users? = dataSnapshot.getValue(Users::class.java)

                  if (context!=null){
                      if (user != null) {
                          view.username_settings.text = user.getUserName()
                      }
                      if (user != null) {
                          Picasso.get().load(user.getProfile()).into(view.profile_image_settings)
                      }
                      if (user != null) {
                          Picasso.get().load(user.getcover()).into(view.cover_image_settings)
                      }
                  }

              }


            }
        })

        view.profile_image_settings.setOnClickListener {
            pickImage()
        }

        view.cover_image_settings.setOnClickListener {
            coverchecker = "cover"
            pickImage()
        }
        view.set_facebook.setOnClickListener {
            socialchecker = "facebook"
            setSocialLink()
        }

   view.set_instagram.setOnClickListener {
            socialchecker = "instagram"
            setSocialLink()
        }

   view.set_website.setOnClickListener {
            socialchecker = "website"
            setSocialLink()
        }

    }


    private fun setSocialLink() {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(context, R.style.ThemeOverlay_AppCompat_Dialog_Alert)

        if (socialchecker == "website"){
            builder.setTitle("write URL:")
        }
        else{
            builder.setTitle("write username:")
        }

        val ediText = EditText(context)

        if (socialchecker == "website"){
           ediText.hint = "e.g. www.google.com"
        }
        else{
            ediText.hint = "e.g. Vigen"
        }
        builder.setView(ediText)

        builder.setPositiveButton("Create",DialogInterface.OnClickListener{
            dialog, which ->
            val str = ediText.text.toString()

            if (str == ""){
                Toast.makeText(context,"please write something....",Toast.LENGTH_LONG).show()
            }
            else{
               saveSocialLink(str)
            }
        })
        builder.setNegativeButton("Cencel",DialogInterface.OnClickListener { dialog, which ->
            dialog.cancel()
        })
        builder.show()
    }

    private fun saveSocialLink(str: String) {

        val mapSocial = HashMap<String, Any>()

        when(socialchecker){

            "facebook"->{
                mapSocial["facebook"] = "https://m.facebook.com/$str"
            }
                "instagram"->{
                mapSocial["instagram"] = "https://m.instagram.com/$str"
            }
                "website"->{
                mapSocial["website"] = "https:///$str"
            }
        }
        userRefrence?.updateChildren(mapSocial).addOnCompleteListener {
            task ->
            if (task.isSuccessful){
                Toast.makeText(context,"updated Successfully.",Toast.LENGTH_LONG).show()

            }
        }

    }


    private fun pickImage() {

        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, RECUESTCODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RECUESTCODE && resultCode == Activity.RESULT_OK && data?.data != null ){

            imageUri = data.data
            Toast.makeText(context,"uploading....",Toast.LENGTH_LONG).show()
            uploadImageToDatabase()
        }
    }

    private fun uploadImageToDatabase() {

        val progressBar = ProgressDialog(context)
        progressBar.setMessage("image is uploading, please wait...")
        progressBar.show()

        if (imageUri != null){
            val fileRef = storageReference!!.child(System.currentTimeMillis().toString() + ".jpg")

            val uploadTask: StorageTask<*>
            uploadTask = fileRef.putFile(imageUri!!)

            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful){
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful){
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    if (coverchecker == "cover"){

                        val  mapCoverImg =HashMap<String, Any>()
                        mapCoverImg["cover"] = url
                        userRefrence.updateChildren(mapCoverImg)
                        coverchecker = ""

                    }else{

                        val  mapProfileImg =HashMap<String, Any>()
                        mapProfileImg["profile"] = url
                        userRefrence.updateChildren(mapProfileImg)
                        coverchecker = ""

                    }
                    progressBar.dismiss()
                }
            }
        }
    }

}