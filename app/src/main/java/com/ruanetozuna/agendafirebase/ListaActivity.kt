package com.ruanetozuna.agendafirebase

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.ruanetozuna.agendafirebase.objetos.Contactos
import com.ruanetozuna.agendafirebase.objetos.ReferenciasFirebase

class ListaActivity : AppCompatActivity() {
    private lateinit var basedatabase: FirebaseDatabase
    private lateinit var referencia: DatabaseReference
    private lateinit var btnNuevo: Button
    private lateinit var listView: ListView
    private val context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista)

        basedatabase = FirebaseDatabase.getInstance()
        referencia = basedatabase.getReferenceFromUrl(
            "${ReferenciasFirebase.URL_DATABASE}${ReferenciasFirebase.DATABASE_NAME}/${ReferenciasFirebase.TABLE_NAME}"
        )

        btnNuevo = findViewById(R.id.btnNuevo)
        listView = findViewById(R.id.listView)
        obtenerContactos()

        btnNuevo.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun obtenerContactos() {
        val contactos = ArrayList<Contactos>()
        val listener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                val contacto = dataSnapshot.getValue(Contactos::class.java)
                contacto?.let { contactos.add(it) }
                val adapter = MyArrayAdapter(context, R.layout.layout_contacto, contactos)
                listView.adapter = adapter
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        referencia.addChildEventListener(listener)
    }

    private inner class MyArrayAdapter(
        context: Context,
        private val resource: Int,
        private val items: ArrayList<Contactos>
    ) : ArrayAdapter<Contactos>(context, resource, items) {
        private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: inflater.inflate(resource, parent, false)
            val lblNombre = view.findViewById<TextView>(R.id.lblNombreContacto)
            val lblTelefono = view.findViewById<TextView>(R.id.lblTelefonoContacto)
            val btnModificar = view.findViewById<Button>(R.id.btnModificar)
            val btnBorrar = view.findViewById<Button>(R.id.btnBorrar)

            val contacto = items[position]

            lblNombre.text = contacto.nombre
            lblTelefono.text = contacto.telefono1

            if (contacto.favorite > 0) {
                lblNombre.setTextColor(Color.BLUE)
                lblTelefono.setTextColor(Color.BLUE)
            } else {
                lblNombre.setTextColor(Color.BLACK)
                lblTelefono.setTextColor(Color.BLACK)
            }

            btnBorrar.setOnClickListener {
                borrarContacto(contacto._ID!!)
                items.remove(contacto)
                notifyDataSetChanged()
                Toast.makeText(context, "Contacto eliminado con éxito", Toast.LENGTH_SHORT).show()
            }

            btnModificar.setOnClickListener {
                val oBundle = Bundle()
                oBundle.putSerializable("contacto", contacto)
                val i = Intent()
                i.putExtras(oBundle)
                setResult(Activity.RESULT_OK, i)
                finish()
            }

            return view
        }
    }

    private fun borrarContacto(childIndex: String) {
        referencia.child(childIndex).removeValue()
    }
}
