package aziz6292.studio.mad_a2_bcsf19a026_bcsf20a046


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.appcompat.app.AlertDialog
import android.util.Log
import android.widget.EditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext




private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var contactRepository: ContactRepository
    private lateinit var contactAdapter: ContactAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val database = AppDatabase.getInstance(application)
        val contactDao = database.contactDao() // This assumes your database has a contactDao() method

        contactRepository = ContactRepository(contactDao)
        contactAdapter = ContactAdapter(
            updateClickListener = { clickedContact ->
                // Handle the update button click for the clicked contact
                showUpdateContactDialog(clickedContact)
            },
            deleteClickListener = { clickedContact ->
                // Handle the delete button click for the clicked contact
                showDeleteContactDialog(clickedContact)
            }
        )

        val recyclerViewContacts = findViewById<RecyclerView>(R.id.recyclerViewContacts)
        recyclerViewContacts.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = contactAdapter
        }

        val fabAddContact = findViewById<FloatingActionButton>(R.id.fabAddContact)
        fabAddContact.setOnClickListener {
            showAddContactDialog()
        }

        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Perform search when the user submits the query
                query?.let { searchContacts(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Perform search as the user types
                newText?.let { searchContacts(it) }
                return true
            }
        })

        loadContacts()
    }

    private fun loadContacts() {
        lifecycleScope.launch {
            try {
                val contacts = contactRepository.getAllContacts()
                contactAdapter.submitList(contacts)
            } catch (e: Exception) {
                // Log the exception (you can replace 'Log.e' with your preferred logging method)
                Log.e("MainActivity", "Error loading contacts", e)

                // Show an error message to the user
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "An error occurred while loading contacts",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }



    private fun showAddContactDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null)
        val etContactName = dialogView.findViewById<EditText>(R.id.etContactName)
        val etContactPhoneNumber = dialogView.findViewById<EditText>(R.id.etContactPhoneNumber)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = etContactName.text.toString()
                val phoneNumber = etContactPhoneNumber.text.toString()

                if (name.isNotEmpty() && phoneNumber.isNotEmpty()) {
                    lifecycleScope.launch {
                        try {
                            val contactId = contactRepository.addContact(Contact(0, name, phoneNumber))
                            if (contactId > 0) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Contact added successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                loadContacts()
                            } else {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Failed to add contact",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error adding contact: ${e.message}")
                            Toast.makeText(
                                this@MainActivity,
                                "Error adding contact",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Please enter both name and phone number",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }


    private fun showUpdateContactDialog(contact: Contact) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.update_contact_dialog, null)
        val etUpdateContactName = dialogView.findViewById<EditText>(R.id.etUpdatedContactName)
        val etUpdateContactPhoneNumber = dialogView.findViewById<EditText>(R.id.etUpdatedContactPhoneNumber)

        etUpdateContactName.setText(contact.name)
        etUpdateContactPhoneNumber.setText(contact.phoneNumber)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val updatedName = etUpdateContactName.text.toString()
                val updatedPhoneNumber = etUpdateContactPhoneNumber.text.toString()
                updateContact(contact, updatedName, updatedPhoneNumber)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun updateContact(contact: Contact, updatedName: String, updatedPhoneNumber: String) {
        lifecycleScope.launch {
            try {
                val updatedContact = Contact(contact.id, updatedName, updatedPhoneNumber)
                contactRepository.updateContact(updatedContact)
                loadContacts()
            } catch (e: Exception) {
                Log.e(TAG, "Error updating contact: ${e.message}")
                Toast.makeText(
                    this@MainActivity,
                    "Error updating contact",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun showDeleteContactDialog(contact: Contact) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.delete_contact_dialog, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    deleteContact(contact)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun deleteContact(contact: Contact) {
        // Implement the logic to delete the contact
        lifecycleScope.launch{
        contactRepository.deleteContact(contact)
       }
        loadContacts()
    }

    private fun searchContacts(query: String) {
        lifecycleScope.launch {
            try {
                val contacts = withContext(Dispatchers.IO) {
                    contactRepository.searchContacts(query)
                }
                Log.d(TAG, "Search results: $contacts")
                contactAdapter.submitList(contacts)
            } catch (e: Exception) {
                Log.e(TAG, "Error searching contacts: ${e.message}")
                Toast.makeText(
                    this@MainActivity,
                    "Error searching contacts",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }



}
