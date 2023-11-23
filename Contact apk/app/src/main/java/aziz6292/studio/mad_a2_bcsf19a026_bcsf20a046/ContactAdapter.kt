package aziz6292.studio.mad_a2_bcsf19a026_bcsf20a046

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import aziz6292.studio.mad_a2_bcsf19a026_bcsf20a046.databinding.ItemContactBinding

class ContactAdapter(
    private val updateClickListener: (Contact) -> Unit,
    private val deleteClickListener: (Contact) -> Unit
) : ListAdapter<Contact, ContactAdapter.ContactViewHolder>(ContactDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(binding, updateClickListener, deleteClickListener)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = getItem(position)
        holder.bind(contact)
    }

    class ContactViewHolder(
        private val binding: ItemContactBinding,
        private val updateClickListener: (Contact) -> Unit,
        private val deleteClickListener: (Contact) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: Contact) {
            binding.tvContactName.text = contact.name
            binding.tvContactPhoneNumber.text = contact.phoneNumber

            // Set click listener for the update button
            binding.ivUpdate.setOnClickListener {
                // Pass the clicked contact to the updateClickListener
                updateClickListener(contact)
            }

            // Set click listener for the delete button
            binding.ivDelete.setOnClickListener {
                // Pass the clicked contact to the deleteClickListener
                deleteClickListener(contact)
            }
        }
    }

    private class ContactDiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem == newItem
        }
    }
}
