package com.manager_api.customer. controllers

import com.manager_api.customer.documents.*
import com.manager_api.customer.repository.CustomerRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.text.ParseException
import java.text.SimpleDateFormat
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

@RestController
@RequestMapping("/api")
class CustomerController (val repository: CustomerRepository) {

    private fun validateData(dataString: String) : Boolean {
        if (dataString.trim() == "") return false
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        sdf.isLenient = false

        try {
            sdf.parse(dataString)
        }catch (e: ParseException) {return false}
        return true
    }

    private fun response(
        message: String, c: Customer?=null, cList: List<Customer>?=null, changes: CustomerChanges?=null
    ): Response {
        val response = Response(message)
        if(c != null) response.customer = c
        if(changes !=null) response.changes = changes
        if(cList != null) response.customersList = cList
        return response
    }

    private fun validateCustomer(customer: Customer): String{
        if(customer.name.isNotEmpty()&&customer.birth.isNotEmpty()&&customer.sex.isNotEmpty()){
            if (when (customer.sex.map { it.toLowerCase() }.joinToString("")) {
                    "male" -> true
                    "female" -> true
                    "masculino" -> true
                    "feminino" -> true
                    else -> false
                }
            ) {
                customer.healthProblems?.forEach { healthProblems ->
                    if (healthProblems.name?.isEmpty() == true) return "One or more fields are missing or invalid."
                    if (healthProblems.degree?.toInt() !in 1..10) return "Health problem degree is out of the range (1 - 10)"
                }
                if (!validateData(customer.birth)) return "Invalid Date, the date format must be YYYY-MM-DD"
                return ""
            } else {
                return "the sex field is only accepting {'male', 'female', 'feminino', 'masculino'}, is not sensitive key."
            }
        }else return "One or more fields are missing or invalid."
    }


    @PostMapping("/newCustomer")
    fun newCustomer(@RequestBody customer: Customer?): ResponseEntity<Response>{
        if(customer==null) return ResponseEntity.badRequest()
            .body(response("Customer not found in the request body"))
        val isValid = validateCustomer(customer)
        if(isValid.isNotBlank())
            return ResponseEntity.badRequest().body(
                response(isValid))
        val result = repository.findByNameAndBirth(customer.name, customer.birth)

        if (result.isPresent) return ResponseEntity.badRequest()
            .body(response("This customer has already been registered"))

        return ResponseEntity.ok().body(response(
            "New customer ${customer.name} has been successfully registered",
            repository.save(customer)
        ))
    }

    @GetMapping("/getCustomer")
    fun getCustomer(@RequestParam name: String?, birth: String?): ResponseEntity<Response>{
        if(name.isNullOrEmpty()||birth.isNullOrEmpty()) {
            // GET ALL USERS
            val result = repository.findAll()
            return ResponseEntity.ok().body(response(
                "List of all customers successfully obtained.",
                null,result))
        }
        if(!validateData(birth)) return ResponseEntity.badRequest()
            .body(response("Invalid Date, the date format must be YYYY-MM-DD"))
        val result = repository.findByNameAndBirth(name, birth)
        if(!result.isPresent) return ResponseEntity.ok()
            .body(response("Customer not found!"))
        return ResponseEntity.ok()
            .body(response(
                "Customer successfully obtained", result.get()))
    }

    @PutMapping("editCustomer")
    fun editCustomer(
        @RequestBody newCustomer: Customer?, @RequestParam name: String?, birth: String?
    ): ResponseEntity<Response>{

        if(name.isNullOrEmpty()||birth.isNullOrEmpty()) {
            // GET ALL USERS
            val result = repository.findAll()
            return ResponseEntity.ok().body(response(
                "List of all customers successfully obtained.",
                null,result))
        }
        if(!validateData(birth)) return ResponseEntity.badRequest()
            .body(response("Invalid Date, the date format must be YYYY-MM-DD"))
        val oldCustomer = repository.findByNameAndBirth(name, birth)
        if(oldCustomer.isEmpty) return ResponseEntity.ok()
            .body(response("Customer not found!"))

        if(newCustomer==null) return ResponseEntity.badRequest()
            .body(response("Customer not found in the request body"))
        val isValid = validateCustomer(newCustomer)
        if(isValid.isNotBlank())
            return ResponseEntity.badRequest().body(response(isValid))

        repository.delete(oldCustomer.get())
        repository.save(newCustomer)
        return ResponseEntity.ok().body(response("Customer ${newCustomer.name} edited with successfully"))
    }

    @PatchMapping("/ECHP")
    fun editCustomerHealthProblem(
        @RequestParam id: String?,@RequestBody list: BodyList?
    ): ResponseEntity<Response>{
        if(id.isNullOrEmpty()||list == null|| list.healthProblems?.isEmpty() == true) return ResponseEntity
            .badRequest().body(response("Invalid request body/query"))
        val c = repository.findById(id)
        if(c.isEmpty) return ResponseEntity.badRequest()
           .body(response("Customer not found!"))
        val customer = c.get()
        val removed: MutableList<HealthProblems> = mutableListOf()
        val added: MutableList<HealthProblems> = mutableListOf()

        list.healthProblems?.forEach { hp ->
            if(c.get().healthProblems?.contains(hp) == true){
                removed.add(hp)
            }else{
                added.add(hp)
            }
        }
        customer.healthProblems?.addAll(added)
        customer.healthProblems?.removeAll(removed)

        val result = repository.save(c.get().copy(healthProblems = customer.healthProblems))
        return ResponseEntity.ok().body(response(
                "The list of customer health problems has been updated successfully",
                result, null, CustomerChanges(added, removed)
            ))
    }

}