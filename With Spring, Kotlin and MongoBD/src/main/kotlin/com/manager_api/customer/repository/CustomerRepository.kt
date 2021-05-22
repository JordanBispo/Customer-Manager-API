package com.manager_api.customer.repository

import com.manager_api.customer.documents.Customer
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface CustomerRepository:MongoRepository<Customer, String> {
    fun findByNameAndBirth(name: String, birth: String) : Optional<Customer>
}