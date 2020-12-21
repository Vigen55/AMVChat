package com.example.amvchat.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.amvchat.AdapterClass.UserAdapter
import com.example.amvchat.ModelClasses.ChatList
import com.example.amvchat.ModelClasses.Users
import com.example.amvchat.Notifications.Token
import com.example.amvchat.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId


class ChatFragment : Fragment() {

    private var userAdapter: UserAdapter? = null
    private var mUsers: MutableList<Users>? = null
    private var userChatList: MutableList<ChatList>? = null

    lateinit var recycler_view_chat_list:RecyclerView

   private var firebaseUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_chat, container, false)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        recycler_view_chat_list = view?.findViewById(R.id.recycler_view_chat_list)!!
        recycler_view_chat_list.setHasFixedSize(true)
        recycler_view_chat_list.layoutManager = LinearLayoutManager(context)

        firebaseUser = FirebaseAuth.getInstance().currentUser

        userChatList = ArrayList()

        val reference = FirebaseDatabase.getInstance().reference.child("ChatList").child(firebaseUser!!.uid)
        reference.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (userChatList as ArrayList).clear()

                for (snapshot in snapshot.children){
                    val chatlist = snapshot.getValue(ChatList::class.java)

                    (userChatList as ArrayList).add(chatlist!!)
                }
                retriveChatList()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        updateToken(FirebaseInstanceId.getInstance().token)



    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
    private fun updateToken(token: String?) {

        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val token1 = token?.let { Token(it) }
        firebaseUser?.uid?.let { ref.child(it).setValue(token1) }
    }

    private fun retriveChatList(){

        mUsers = arrayListOf()
        val reference = FirebaseDatabase.getInstance().reference.child("users")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
               (mUsers as ArrayList).clear()

                for (snapshot in dataSnapshot.children){
                    val user = snapshot.getValue(Users::class.java)

                    for (chatlist in userChatList!!){
                        if (user?.getUID().equals(chatlist.getid())){
                            (mUsers as ArrayList).add(user!!)
                        }
                    }

                }
                userAdapter = context?.let { UserAdapter(it, mUsers as ArrayList<Users>, true) }
                recycler_view_chat_list.adapter = userAdapter
            }

        })
    }

}