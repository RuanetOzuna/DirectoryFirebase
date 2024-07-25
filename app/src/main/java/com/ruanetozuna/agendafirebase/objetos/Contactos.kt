package com.ruanetozuna.agendafirebase.objetos

import java.io.Serializable

data class Contactos(
    var _ID: String? = null,
    var nombre: String? = null,
    var telefono1: String? = null,
    var telefono2: String? = null,
    var direccion: String? = null,
    var notas: String? = null,
    var favorite: Int = 0
) : Serializable
