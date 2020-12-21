package com.example.amvchat


import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.green
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.amvchat.Fragments.ChatFragment
import com.example.amvchat.Fragments.SearchFragment
import com.example.amvchat.Fragments.SettingsFragment
import com.example.amvchat.ModelClasses.Chats
import com.example.amvchat.ModelClasses.ChatList
import com.example.amvchat.ModelClasses.Users
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var firebaseUser: FirebaseUser? = null
    var reference: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_main))

        firebaseUser = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser?.uid.toString())

        val toolbar:Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        val tablayout:TabLayout = findViewById(R.id.tab_layout)
        val viewpager:ViewPager = findViewById(R.id.view_pager)


        val ref = FirebaseDatabase.getInstance().reference.child("Chats")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)

                var countUnreadMassages = 0

                for (datasnapshot in snapshot.children){

                    val chat = datasnapshot.getValue(Chats::class.java)
                    if (chat != null) {
                        if (chat.receiver.equals(firebaseUser!!.uid) && !chat.issen){

                            countUnreadMassages += 1
                        }
                    }
                }
                if (countUnreadMassages == 0){
                    viewPagerAdapter.addFragment(ChatFragment(),"Chats")
                }
                else{
                    viewPagerAdapter.addFragment(ChatFragment(),"($countUnreadMassages) Chats")
                }
                viewPagerAdapter.addFragment(SearchFragment(),"Search")
                viewPagerAdapter.addFragment(SettingsFragment(),"Settings")

                viewpager.adapter = viewPagerAdapter
                tablayout.setupWithViewPager(viewpager)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(e: DatabaseError) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.exists()){
                    val user:Users? = dataSnapshot.getValue(Users::class.java)

                    if (user != null) {
                        user_name.text = user.getUserName()
                    }
                    user_name.setTextColor(Color.parseColor("#F7FAF9"))
                    if (user != null) {
                        Picasso.get().load(user.getProfile()).placeholder(R.drawable.ic_profile).into(profile_image)
                    }
                }


            }

        })



    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
         when (item.itemId) {
            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()
                val  intent = Intent(this,WelcomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()

                return true
            }
        }
        return false
    }

    internal class ViewPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager){

        private val fragments: ArrayList<Fragment>
        private val titles: ArrayList<String>

        init {
            fragments = ArrayList()
            titles = ArrayList()
        }
        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        fun addFragment(fragment: Fragment,title: String){

            fragments.add(fragment)
            titles.add(title)
        }

        override fun getPageTitle(i: Int): CharSequence? {
            return titles[i]
        }

    }

    private fun updateStatus(status: String){

        val ref = firebaseUser?.uid?.let {
            FirebaseDatabase.getInstance().reference.child("users").child(
                it
            )
        }

        val hashmap = HashMap<String, Any>()
        hashmap["status"] = status
        ref?.updateChildren(hashmap)

    }

    override fun onResume() {
        super.onResume()

        updateStatus("online")
    }

    override fun onPause() {
        super.onPause()

        updateStatus("offline")
    }

}