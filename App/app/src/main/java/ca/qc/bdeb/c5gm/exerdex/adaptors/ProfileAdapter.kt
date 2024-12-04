package ca.qc.bdeb.c5gm.exerdex.adaptors

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.exerdex.R
import ca.qc.bdeb.c5gm.exerdex.data.User
import ca.qc.bdeb.c5gm.exerdex.viewholders.ProfileViewHolder

class ProfileAdapter(
    private val context: Context,
    private val items: MutableList<User>,
    private val onEditClick: (User) -> Unit,
    private val onLogoutClick: () -> Unit,
    private val onDeleteClick: () -> Unit
    ) : RecyclerView.Adapter<ProfileViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile, parent, false)
        return ProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val item = items[position]

        holder.textValue.text = if (position == items.size - 4) "********" else item.title
        holder.mainIcon.setImageResource(item.iconId)

        if(item.title == "Log out"){
            holder.mainIcon.setOnClickListener {
                onLogoutClick()
                Log.d("ProfileAdapter", "Logout!!")
            }
        }

        if(item.title == "Delete account"){
            holder.mainIcon.setOnClickListener {
                onDeleteClick()
                Log.d("ProfileAdapter", "Delete !!")
            }
        }

        if (item.hasEditIcon) {
            holder.editIcon.setImageResource(R.drawable.baseline_edit_square_24)
            holder.editIcon.setOnClickListener {
                onEditClick(item)
            }

        } else {
            holder.editIcon.visibility = View.GONE
        }
    }


    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<User>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    }

