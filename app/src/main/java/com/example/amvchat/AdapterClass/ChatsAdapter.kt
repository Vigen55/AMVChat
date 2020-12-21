package com.example.amvchat.AdapterClass

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.amvchat.ModelClasses.Chats
import com.example.amvchat.R
import com.example.amvchat.ViewFullImageActivity
import com.example.amvchat.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatsAdapter(private val mContext: Context,
                   private val mChat: List<Chats>,
                   private val imageUrl: String): RecyclerView.Adapter<ChatsAdapter.ViewHolder?>() {


    var firebaseUser:FirebaseUser? = FirebaseAuth.getInstance().currentUser



    val MSG_TYPE_LEFT = 0
    val MSG_TYPE_RIGHT = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsAdapter.ViewHolder {
        if (viewType == MSG_TYPE_RIGHT){
            val view = LayoutInflater.from(mContext).inflate(R.layout.massage_item_right, parent, false)
            return ViewHolder(view)
        } else {
            val view = LayoutInflater.from(mContext).inflate(R.layout.massage_item_left, parent, false)
            return ViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return mChat.size
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ChatsAdapter.ViewHolder, position: Int) {

        val chat: Chats = mChat[position]

        Picasso.get().load(imageUrl).into(holder.profile_image)

        if (chat.massage == "sent you an image" && !chat.url.equals("")){


           if (chat.sender == firebaseUser?.uid) {

               holder.show_txt_message?.visibility = View.GONE
               holder.right_image_view?.visibility = View.VISIBLE
               Picasso.get().load(chat.url).into(holder.right_image_view)

               holder.right_image_view?.setOnClickListener {
                   val options = arrayOf<CharSequence>(
                       "View Full Image",
                       "Delete Image",
                       "Cencle"
                   )
                   val builder:AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                   builder.setTitle("Wat do you wanr?")

                   builder.setItems(options, DialogInterface.OnClickListener{
                           dialog, which ->
                       if (which == 0){
                           val intent = Intent(mContext, ViewFullImageActivity::class.java)
                           intent.putExtra("url",chat.url)
                           mContext.startActivity(intent)

                       }
                       else if (which == 1){
                           deleteSentMassage(holder,position)
                       }
                   })
                   builder.show()
               }

            }

            else if (chat.sender != (firebaseUser?.uid)){
                holder.show_txt_message?.visibility = View.GONE
                holder.left_image_view?.visibility = View.VISIBLE
                Picasso.get().load(chat.url).into(holder.left_image_view)

               holder.left_image_view?.setOnClickListener {
                   val options = arrayOf<CharSequence>(
                       "View Full Image",

                       "Cencle"
                   )
                   val builder:AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                   builder.setTitle("Wat do you wanr?")

                   builder.setItems(options, DialogInterface.OnClickListener{
                       dialog, which ->
                       if (which == 0){
                           val intent = Intent(mContext, ViewFullImageActivity::class.java)
                           intent.putExtra("url",chat.url)
                           mContext.startActivity(intent)

                       }

                   })
                   builder.show()
               }
            }
        }
        else{
                holder.show_txt_message?.text = chat.massage

            if (firebaseUser?.uid == chat.sender){
                holder.show_txt_message?.setOnClickListener {
                    val options = arrayOf<CharSequence>(
                        "Delete Massage",
                        "Cencle"
                    )
                    val builder:AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("Wat do you wanr?")

                    builder.setItems(options, DialogInterface.OnClickListener{
                            dialog, which ->

                        if (which == 0){
                            deleteSentMassage(holder,position)
                        }
                    })
                    builder.show()
                }
            }
        }

        if (position == mChat.size-1){
            Log.e(TAG, "onBindViewHolder: ${holder.txt_seen.toString()}")
           if (chat.issen){
               holder.txt_seen?.visibility = View.VISIBLE
               holder.txt_seen?.text = "Seen"

             if (chat.massage.equals("sent you an image") && !chat.url.equals(""))
               {
                   val ip: ConstraintLayout.LayoutParams? = holder.txt_seen!!.layoutParams as ConstraintLayout.LayoutParams
                   ip!!.setMargins(0, 245, 10, 0)
                   holder.txt_seen!!.layoutParams = ip
              }
           }
            else{
               holder.txt_seen?.text = "Sent"

               if (chat.massage.equals("sent you an image") && !chat.url.equals(""))
               {
                   val ip:ConstraintLayout.LayoutParams? = holder.txt_seen?.layoutParams as ConstraintLayout.LayoutParams
                   ip!!.setMargins(0, 245, 10, 0)
                   holder.txt_seen!!.layoutParams = ip
               }
           }
        }
        else{
            holder.txt_seen?.visibility = View.GONE

        }
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        var profile_image:CircleImageView? = null
        var show_txt_message:AppCompatTextView? = null
        var left_image_view:ImageView? = null
        var txt_seen:AppCompatTextView? = null
        var right_image_view:ImageView? = null

        init {
            profile_image = itemView.findViewById(R.id.profile_image)
            show_txt_message = itemView.findViewById(R.id.show_txt_message)
            left_image_view = itemView.findViewById(R.id.left_image_view)
            txt_seen = itemView.findViewById(R.id.txt_seen)
            right_image_view = itemView.findViewById(R.id.right_image_view)
        }


    }

    override fun getItemViewType(position: Int): Int {

        if (mChat[position].sender.equals(firebaseUser?.uid)){
            return MSG_TYPE_RIGHT
        } else {
            return MSG_TYPE_LEFT
        }

    }

    private fun deleteSentMassage(holder: ChatsAdapter.ViewHolder, position: Int){

        val ref = FirebaseDatabase.getInstance().reference.child("Chats")
            .child(mChat.get(position).massageId!!)
            .removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    Toast.makeText(holder.itemView.context, " Deleted",Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(holder.itemView.context, "Failed, Not Deleted",Toast.LENGTH_SHORT).show()

                }
            }
    }

}