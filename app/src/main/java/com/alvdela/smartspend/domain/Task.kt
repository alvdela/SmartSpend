package com.alvdela.smartspend.domain

import java.time.LocalDate

data class Task (
    private var description: String,
    private var limitDate: LocalDate?,
    private var mandatory: Boolean = false,
    private var price: Int = 0,
    private var state: TaskState
    )