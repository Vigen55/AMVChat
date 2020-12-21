package com.example.amvchat.AdapterClass

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.amvchat.MassageChatActivity
import com.example.amvchat.ModelClasses.Chats
import com.example.amvchat.ModelClasses.Users
import com.example.amvchat.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.user_search_item_layout.view.*

class UserAdapter( mContext: Context,  mUsers: MutableList<Users>,  isChatCheck: Boolean): RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private val mContext:Context
    private val mUsers:List<Users>
    private var isChatCheck:Boolean

    var lastMsg:String = ""

    init {
        this.mContext = mContext
        this.mUsers = mUsers
        this.isChatCheck = isChatCheck
    }


    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        var  userNameTxt:TextView
        var  profileImageView:CircleImageView
        var  onlineImageView:CircleImageView
        var oflineImageView:CircleImageView
        var  lastMassageTxt:TextView

        init {
            userNameTxt = itemView.findViewById(R.id.username)
            profileImageView = itemView.findViewById(R.id.profile_image)
            onlineImageView = itemView.findViewById(R.id.img_on)
            oflineImageView = itemView.findViewById(R.id.img_off)
            lastMassageTxt = itemView.findViewById(R.id.last_msg)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): UserAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_search_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mUsers.size

    override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {
        val user = mUsers.get(position)
        holder.itemView.username.text = user?.getUserName()
        holder.itemView.username.setTextColor(Color.parseColor("#F7FAF9"))
        Picasso.get().load(user.getProfile()).placeholder(R.drawable.ic_profile)
            .into(holder.profileImageView)

        if (isChatCheck){
            retrievLastMassage(user.getUID(), holder.lastMassageTxt)
        }
        else{
            holder.lastMassageTxt.visibility = View.GONE
        }

        if (isChatCheck){
            if (user.getstatus() == "online"){
                holder.onlineImageView.visibility = View.VISIBLE
                holder.oflineImageView.visibility = View.GONE
            }
            else  {
                holder.onlineImageView.visibility = View.GONE
                holder.oflineImageView.visibility = View.VISIBLE
            }
        }
        else{
            holder.onlineImageView.visibility = View.GONE
            holder.oflineImageView.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val options = arrayOf<CharSequence>(
                "Send Massage",
                "Visit Profile"
            )
            val builder:AlertDialog.Builder = AlertDialog.Builder(mContext)
            builder.setTitle("what do you want?")
            builder.setItems(options, DialogInterface.OnClickListener { dialog, position ->
                if (position == 0){
                    val intent = Intent(mContext, MassageChatActivity::class.java)
                    intent.putExtra("visit_id", user.getUID())
                    mContext.startActivity(intent)
                }
                if (position == 1){

                }
            })
            builder.show()
        }


    }

    private fun retrievLastMassage(chateUserId: String?, lastMassageTxt: TextView) {

        lastMsg = "defaultMsg"

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        reference.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                for (datasnapshot in snapshot.children){
                    val chat: Chats? = datasnapshot.getValue(Chats::class.java)

                    if (firebaseUser != null && chat != null){
                        if (chat.receiver == firebaseUser?.uid &&
                                chat.sender == chateUserId ||
                                chat.receiver == chateUserId &&
                                chat.sender == firebaseUser?.uid)
                        {
                            lastMsg = chat.massage!!

                        }
                    }
                }
                when(lastMsg){
                    "defaultMsg" -> lastMassageTxt.text = "No Massage"
                    "sent you an image" -> lastMassageTxt.text = "Image sent"
                    else -> lastMassageTxt.text = lastMsg
                }
                lastMsg = "defaultMsg"
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

}