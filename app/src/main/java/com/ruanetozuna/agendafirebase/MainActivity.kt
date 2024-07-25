package com.ruanetozuna.agendafirebase

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ruanetozuna.agendafirebase.objetos.Contactos
import com.ruanetozuna.agendafirebase.objetos.ReferenciasFirebase

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var btnGuardar: Button
    private lateinit var btnListar: Button
    private lateinit var btnLimpiar: Button
    private lateinit var txtNombre: EditText
    private lateinit var txtDireccion: EditText
    private lateinit var txtTelefono1: EditText
    private lateinit var txtTelefono2: EditText
    private lateinit var txtNotas: EditText
    private lateinit var cbkFavorite: CheckBox
    private lateinit var basedatabase: FirebaseDatabase
    private lateinit var referencia: DatabaseReference
    private var savedContacto: Contactos? = null
    private var id: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initComponents()
        setEvents()
    }

    private fun initComponents() {
        basedatabase = FirebaseDatabase.getInstance()
        referencia = basedatabase.getReferenceFromUrl(
            "https://agendafirebase-ec1fd-default-rtdb.firebaseio.com/agenda/contactos"
        )

        txtNombre = findViewById(R.id.txtNombre)
        txtTelefono1 = findViewById(R.id.txtTelefono1)
        txtTelefono2 = findViewById(R.id.txtTelefono2)
        txtDireccion = findViewById(R.id.txtDireccion)
        txtNotas = findViewById(R.id.txtNotas)
        cbkFavorite = findViewById(R.id.cbxFavorito)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnListar = findViewById(R.id.btnListar)
        btnLimpiar = findViewById(R.id.btnLimpiar)
    }

    private fun setEvents() {
        btnGuardar.setOnClickListener(this)
        btnListar.setOnClickListener(this)
        btnLimpiar.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        if (isNetworkAvailable()) {
            when (view.id) {
                R.id.btnGuardar -> {
                    var completo = true
                    if (txtNombre.text.toString().isEmpty()) {
                        txtNombre.error = "Introduce el Nombre"
                        completo = false
                    }
                    if (txtTelefono1.text.toString().isEmpty()) {
                        txtTelefono1.error = "Introduce el Teléfono Principal"
                        completo = false
                    }
                    if (txtDireccion.text.toString().isEmpty()) {
                        txtDireccion.error = "Introduce la Dirección"
                        completo = false
                    }
                    if (completo) {
                        val nContacto = Contactos(
                            nombre = txtNombre.text.toString(),
                            telefono1 = txtTelefono1.text.toString(),
                            telefono2 = txtTelefono2.text.toString(),
                            direccion = txtDireccion.text.toString(),
                            notas = txtNotas.text.toString(),
                            favorite = if (cbkFavorite.isChecked) 1 else 0
                        )
                        if (savedContacto == null) {
                            agregarContacto(nContacto)
                            Toast.makeText(this, "Contacto guardado con éxito", Toast.LENGTH_SHORT).show()
                            limpiar()
                        } else {
                            actualizarContacto(id!!, nContacto)
                            Toast.makeText(this, "Contacto actualizado con éxito", Toast.LENGTH_SHORT).show()
                            limpiar()
                        }
                    }
                }
                R.id.btnLimpiar -> limpiar()
                R.id.btnListar -> {
                    val i = Intent(this, ListaActivity::class.java)
                    limpiar()
                    startActivityForResult(i, 0)
                }
            }
        } else {
            Toast.makeText(this, "Se necesita tener conexión a internet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun agregarContacto(c: Contactos) {
        val newContactoReference = referencia.push()
        val id = newContactoReference.key
        c._ID = id
        newContactoReference.setValue(c)
    }

    private fun actualizarContacto(id: String, p: Contactos) {
        p._ID = id
        referencia.child(id).setValue(p)
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun limpiar() {
        savedContacto = null
        txtNombre.text.clear()
        txtTelefono1.text.clear()
        txtTelefono2.text.clear()
        txtNotas.text.clear()
        txtDireccion.text.clear()
        cbkFavorite.isChecked = false
        id = ""
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (intent != null) {
            val oBundle = intent.extras
            if (Activity.RESULT_OK == resultCode) {
                val contacto = oBundle?.getSerializable("contacto") as Contactos
                savedContacto = contacto
                id = contacto._ID
                txtNombre.setText(contacto.nombre)
                txtTelefono1.setText(contacto.telefono1)
                txtTelefono2.setText(contacto.telefono2)
                txtDireccion.setText(contacto.direccion)
                txtNotas.setText(contacto.notas)
                cbkFavorite.isChecked = contacto.favorite > 0
            } else {
                limpiar()
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}
