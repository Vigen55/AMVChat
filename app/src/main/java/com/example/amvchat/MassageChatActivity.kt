package com.example.amvchat

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.amvchat.AdapterClass.ChatsAdapter
import com.example.amvchat.ModelClasses.APIService
import com.example.amvchat.ModelClasses.Chats
import com.example.amvchat.ModelClasses.Users
import com.example.amvchat.Notifications.*
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_massage_chat.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.log

class MassageChatActivity : AppCompatActivity() {

    var userIdVisit: String = ""
    var fuser: FirebaseUser? = null
    var chatsAdapter: ChatsAdapter? = null
    var mChatList: List<Chats>? = null
    lateinit var recycler_view_chat: RecyclerView

    var reference: DatabaseReference? = null

    var notify = false
    var apiService: APIService? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_massage_chat)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_chat)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        apiService =
            Client.Client.getClient("https://fcm.googleapis.com/")!!.create(APIService::class.java)


        intent = intent
        userIdVisit = intent.getStringExtra("visit_id").toString()
        fuser = FirebaseAuth.getInstance().currentUser

        recycler_view_chat = findViewById(R.id.recycler_view_chat)
        recycler_view_chat.setHasFixedSize(true)
        val linerLayoutManager = LinearLayoutManager(applicationContext)
        linerLayoutManager.stackFromEnd = true
        recycler_view_chat.layoutManager = linerLayoutManager

        reference = FirebaseDatabase.getInstance().reference
            .child("users").child(userIdVisit)
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val user: Users? = snapshot.getValue(Users::class.java)
                username_chat.text = user?.getUserName()
                Picasso.get().load(user?.getProfile()).into(profile_image_chat)

                fuser?.uid?.let { retrieveMassages(it, userIdVisit, user?.getProfile()) }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        btn_send_massage_chat.setOnClickListener {
            notify = true
            val msg = text_send_chat.text.toString()
            if (msg.equals("")) {
                Toast.makeText(this, "You can't send empty message", Toast.LENGTH_SHORT).show()

            } else {
                fuser?.uid?.let { it1 -> sendMassageToUser(it1, userIdVisit, msg) }
            }

            text_send_chat.setText("")
        }

        btn_send_file_chat.setOnClickListener {
            notify = true
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Pick image"), 438)
        }

        seenMassage(userIdVisit)

        /*intent?.extras?.let {
            retrieveMassages(it.getString("sender", ""),
                it.getString("user", ""),
                it.getString("icon"))

        }*/
    }


   /* override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.extras?.let {
            retrieveMassages(it.getString("sender", ""),
                it.getString("user", ""),
                it.getString("icon"))
        }



    }*/

    private fun sendMassageToUser(senderId: String, receiverId: String, msg: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val massageKey = reference.push().key

        val massagehashmap = HashMap<String, Any?>()
        massagehashmap["sender"] = senderId
        massagehashmap["massage"] = msg
        massagehashmap["receiver"] = receiverId
        massagehashmap["issen"] = false
        massagehashmap["url"] = "senderId"
        massagehashmap["massageId"] = massageKey
        reference.child("Chats").child(massageKey!!)
            .setValue(massagehashmap)
            .addOnCompleteListener { task ->
                val chatListReferance = FirebaseDatabase.getInstance()
                    .reference
                    .child("ChatList").child(fuser!!.uid)
                    .child(userIdVisit)

                chatListReferance.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists()) {
                            chatListReferance.child("id").setValue(userIdVisit)
                        }
                        val chatListReceiverReferance = fuser?.uid?.let {
                            FirebaseDatabase.getInstance()
                                .reference
                                .child("ChatList").child(userIdVisit)
                                .child(it)
                        }

                        chatListReceiverReferance?.child("id")?.setValue(fuser?.uid)


                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
            }

        fuser?.uid?.let {
            FirebaseDatabase.getInstance().reference
                .child("users").child(it)
        }?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(Users::class.java)
                if (notify) {
                    if (user != null) {
                        sendNotification(receiverId, user.getUserName(), msg)
                    }
                }
                notify = false
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    private fun sendNotification(receiverId: String, userName: String?, msg: String) {

        val ref = FirebaseDatabase.getInstance().getReference().child("Tokens")

        val query = ref.orderByKey().equalTo(receiverId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot in snapshot.children) {
                    val token: Token? = datasnapshot.getValue(Token::class.java)

                    val data = Data(fuser?.uid,
                        R.mipmap.ic_launcher,
                        "$userName: $msg",
                        "New Massage",
                        userIdVisit
                    )

                    val sender = Sender(data, token?.token.toString())

                    apiService?.sendNotification(sender)?.enqueue(object : Callback<MyResponse> {
                        override fun onResponse(
                            call: Call<MyResponse>,
                            response: Response<MyResponse>
                        ) {
                            if (response.code() == 200) {
                                if (response.body()?.success !== 1) {
                                    Toast.makeText(this@MassageChatActivity,
                                        "Failed, Nothing happen",
                                        Toast.LENGTH_LONG).show()
                                }
                            }
                        }

                        override fun onFailure(call: Call<MyResponse>, t: Throwable) {

                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.data != null) {
            val progressBar = ProgressDialog(this)
            progressBar.setMessage("image is uploading, please wait...")
            progressBar.show()

            val fileUrl = data.data
            val storageReferance = FirebaseStorage.getInstance().reference.child("Chat Images")
            val ref = FirebaseDatabase.getInstance().reference
            val massageId = ref.push().key
            val filePath = storageReferance.child("$massageId.jpg")

            var uploadTask: StorageTask<*>
            uploadTask = filePath.putFile(fileUrl!!)

            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    val massagehashmap = HashMap<String, Any?>()
                    massagehashmap["sender"] = fuser!!.uid
                    massagehashmap["massage"] = "sent you an image"
                    massagehashmap["receiver"] = this.userIdVisit
                    massagehashmap["issen"] = false
                    massagehashmap["url"] = url
                    massagehashmap["massageId"] = massageId

                    ref.child("Chats").child(massageId!!).setValue(massagehashmap)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                progressBar.dismiss()

                                FirebaseDatabase.getInstance().reference
                                    .child("users").child(fuser!!.uid)

                                reference?.addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val user = snapshot.getValue(Users::class.java)
                                        if (notify) {
                                            if (user != null) {
                                                sendNotification(userIdVisit,
                                                    user.getUserName(),
                                                    "sent you an image")
                                            }
                                        }
                                        notify = false
                                    }

                                    override fun onCancelled(error: DatabaseError) {

                                    }
                                })
                            }
                        }


                }
            }
        }
    }

    private fun retrieveMassages(senderId: String, receiverId: String, receiverImageUrl: String?) {

        mChatList = ArrayList()
        reference = FirebaseDatabase.getInstance().reference.child("Chats")

        reference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (mChatList as ArrayList<Chats>).clear()
                for (p0 in snapshot.children) {
                    val chat = p0.getValue(Chats::class.java)

                    if (chat?.receiver.equals(senderId) && chat?.sender.equals(receiverId)
                        || chat?.receiver.equals(receiverId) && chat?.sender.equals(senderId)
                    ) {

                        chat?.let { (mChatList as ArrayList<Chats>).add(it) }
                    }
                    chatsAdapter = receiverImageUrl?.let {
                        ChatsAdapter(this@MassageChatActivity, (mChatList as ArrayList<Chats>),
                            it
                        )
                    }
                    recycler_view_chat.adapter = chatsAdapter
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


    }

    var seenListanes: ValueEventListener? = null

    private fun seenMassage(userId: String) {

        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        seenListanes = reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (datasnapshot in snapshot.children) {
                    val chat: Chats? = datasnapshot.getValue(Chats::class.java)
                    if (chat?.receiver.equals(fuser!!.uid) && chat?.sender.equals(userId)) {
                        val hashmap = HashMap<String, Any>()
                        hashmap["issen"] = true
                        datasnapshot.ref.updateChildren(hashmap)

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onPause() {
        super.onPause()

        reference?.removeEventListener(seenListanes!!)
    }

}