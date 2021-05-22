package com.manager_api.customer.controllers

import com.manager_api.customer.documents.HealthProblems

data class BodyList (
    val healthProblems: MutableList<HealthProblems>? = mutableListOf(HealthProblems(null, null))
        )