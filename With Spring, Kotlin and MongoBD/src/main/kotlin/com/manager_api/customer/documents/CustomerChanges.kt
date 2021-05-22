package com.manager_api.customer.documents

data class CustomerChanges (
    val addedToList: List<HealthProblems>,
    val removedToList: List<HealthProblems>,
)
