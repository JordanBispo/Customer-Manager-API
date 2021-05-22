package com.manager_api.customer.documents

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document("Customers")
data class Customer (
    @Id
    val id: String?,
    val name:String,
    val sex: String,
    val birth: String,
    val createdAt: Date = Date(System.currentTimeMillis()),
    val lastModified: Date = Date(System.currentTimeMillis()),
    val healthProblems: MutableList<HealthProblems>? = mutableListOf(HealthProblems(null, null))
)
