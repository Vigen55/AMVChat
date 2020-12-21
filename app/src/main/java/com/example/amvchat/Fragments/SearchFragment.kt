package com.example.amvchat.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.amvchat.AdapterClass.UserAdapter
import com.example.amvchat.ModelClasses.Users
import com.example.amvchat.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*


class SearchFragment : Fragment() {

    private var userAdapter: UserAdapter? = null
    private var mUsers: MutableList<Users>? = null
    private var recyclerView:RecyclerView? = null
    private var searchEdtText: EditText? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view:View = inflater.inflate(R.layout.fragment_search, container, false)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        recyclerView = view?.searchList

        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        searchEdtText = view?.findViewById(R.id.searchUserET)

        mUsers = ArrayList()
        retrieveAllUsers()

        searchEdtText!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchForUsers(s.toString().toLowerCase())
            }

        })
    }
    private fun retrieveAllUsers() {
        val firebaseUserId = FirebaseAuth.getInstance().currentUser
        val refusers = FirebaseDatabase.getInstance().getReference("users")

        //Log.d("G", FirebaseDatabase.getInstance().reference.toString())

        refusers.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (mUsers as ArrayList<Users>).clear()

                   if (searchEdtText?.text.toString() == ""){
                       for (dataSnapshot in dataSnapshot.children){
                           val user = dataSnapshot.getValue(Users::class.java)

                           if (!(user?.getUID())!!.equals(firebaseUserId)){
                               (mUsers as ArrayList<Users>).add(user)
                           }
                       }
                       userAdapter =
                           context?.let { UserAdapter(it, mUsers as ArrayList<Users>, false) }
                       recyclerView?.adapter = userAdapter
                   }
            }
        })
    }
    private fun searchForUsers(str:String){

        val fuser = FirebaseAuth.getInstance().currentUser
        val query = FirebaseDatabase.getInstance().getReference("users")
            .orderByChild("search")
            .startAt(str)
            .endAt("$str\uf8ff")

        query.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {


            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (mUsers as ArrayList<Users>).clear()

                for (dataSnapshot in dataSnapshot.children){
                    val user = dataSnapshot.getValue(Users::class.java)

                    if (!(user!!.getUID())!!.equals(fuser)){
                        (mUsers as ArrayList<Users>).add(user)
                    }
                    userAdapter = context?.let { UserAdapter(it, mUsers as ArrayList<Users>, false) }
                    recyclerView!!.adapter = userAdapter
                }

            }

        })
    }


}