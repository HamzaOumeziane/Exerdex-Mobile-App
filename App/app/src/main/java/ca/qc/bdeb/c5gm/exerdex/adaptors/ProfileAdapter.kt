package ca.qc.bdeb.c5gm.exerdex.adaptors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.exerdex.R
import ca.qc.bdeb.c5gm.exerdex.data.User
import ca.qc.bdeb.c5gm.exerdex.viewholders.ProfileViewHolder

class ProfileAdapter(
    private val items: List<User>,
    private val onEditClick: (User) -> Unit,
    private val onLogoutClick: () -> Unit
    ) : RecyclerView.Adapter<ProfileViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile, parent, false)
        return ProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val item = items[position]

        holder.textValue.text = item.title
        holder.mainIcon.setImageResource(item.iconId)

        if (item.hasEditIcon) {
            holder.editIcon.visibility = View.VISIBLE
            holder.editIcon.setOnClickListener {
                onEditClick(item)
            }
            holder.itemView.setOnClickListener(null)
        } else {
            holder.editIcon.visibility = View.GONE
            holder.itemView.setOnClickListener {
                onLogoutClick()
            }
        }
    }

    override fun getItemCount(): Int = items.size
    }