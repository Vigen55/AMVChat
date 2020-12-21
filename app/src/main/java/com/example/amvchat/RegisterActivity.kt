package com.example.amvchat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    lateinit var mAuth: FirebaseAuth
    lateinit var reference: DatabaseReference
    private var firebaseUserId : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()



        val toolbar: Toolbar = findViewById(R.id.toolbar_register)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Register"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this,WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        btn_register.setOnClickListener {
            registerUser()
        }

    }

    private fun registerUser() {

            val username:String = username_register.text.toString()
            val email:String = email_register.text.toString()
            val password:String = password_register.text.toString()


            if (username.isEmpty() || email.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "All field are required", Toast.LENGTH_LONG).show()
            } else if (password.length < 6){
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_LONG).show()
            } else {
                mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener{ task ->
                        if (task.isSuccessful){

                           firebaseUserId = mAuth.currentUser!!.uid
                            reference = FirebaseDatabase.getInstance().reference.child("users").child(firebaseUserId)

                            val userHashmap = HashMap<String,Any>()
                            userHashmap["uid"] = firebaseUserId
                            userHashmap["username"] = username
                            userHashmap["profile"] = "https://firebasestorage.googleapis.com/v0/b/amv-chat-3e372.appspot.com/o/profile_image.png?alt=media&token=3f498e45-49ae-49d7-a455-9c6a61028d15"
                            userHashmap["cover"] = "https://firebasestorage.googleapis.com/v0/b/amv-chat-3e372.appspot.com/o/mario.png?alt=media&token=f24449ac-6946-4205-90f5-50ae848cbc9b"
                            userHashmap["status"] = "offline"
                            userHashmap["search"] = username.toLowerCase()
                            userHashmap["facebook"] = "https://m.facebook.com"
                            userHashmap["instagram"] = "https://m.instagram.com"
                            userHashmap["website"] = "https://www.google.com"

                            reference.updateChildren(userHashmap)
                                .addOnCompleteListener {task ->
                                    if (task.isSuccessful){
                                        val  intent = Intent(this,MainActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                        startActivity(intent)
                                        finish()
                                    }
                                }
                        }


                        else{
                            Toast.makeText(this, "Error Massige" + task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
            }

    }
}