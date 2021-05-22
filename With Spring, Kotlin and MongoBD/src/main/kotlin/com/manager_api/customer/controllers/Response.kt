package com.manager_api.customer.controllers

import com.manager_api.customer.documents.Customer
import com.manager_api.customer.documents.CustomerChanges

data class Response (
    val message: String? = null,
    var customer: Customer? = null,
    var customersList: List<Customer>? = null,
    var changes: CustomerChanges? = null,
)