package com.alvdela.smartspend.model

data class Pregunta(
    val pregunta: String,
    val opciones: List<String>,
    val respuesta_correcta: String
)