const Customer = require('./models/customer_schema')
const express = require('express')
const router  = express.Router()


const response = (res, code, message) => {
    return res.status(code).send({message: message})
}

const dateValidation = (date) =>{
    const d = new Date(date)
    if(d.toString()==="Invalid Date") return false
    return d
}


router.post('/newCustomer', async (req, res) => {
    const { name, birth, sex, healthProblems } = req.body
    
    const birthDate = dateValidation(birth)
    if(!birthDate) return response(res, 400, "Invalid Date, the date format must be YYYY-MM-DD")


    if(await Customer.findOne({name: name, birth: birthDate})){
        return response(res, 400, "this customer has already been registered")
    }

    const customer = await Customer.create({name, birth: birthDate, sex, healthProblems}).catch((err)=>{
        return response(res, 500, err.message)
    })

    return response(res, 200, "New customer "+customer.name+" has been successfully registered")

})

router.get('/getCustomer', async (req, res)=>{
    
    const {name, birth} = req.query
    if(!(name||birth)){
        //get all customers
        const customersList = await Customer.find({}, function(err, customers) {
            let list = []    
            customers.forEach(function(customer) {
                list.push(customer)
            }); 
            return list  
        });
        return res.status(200).send({message: 'list of customers successfully obtained', customersList})
    }
    // Get a specific customer
    
    const birthDate = dateValidation(birth) // validate data format
    if(!birthDate) return response(res, 400, "Invalid Date, the date format must be YYYY-MM-DD")
    
    const customer = await Customer.findOne({name: name, birth: birthDate}).catch((err)=>{
        return response(res, 500, err.message)
    })
    if(!customer){
        return response(res, 404, "Customer not found")
    }
    
    return res.status(200).send({message: 'Customer successfully obtained', customer})

})

router.put('/editCustomer', async (req, res)=>{

    const { name, birth, sex, healthProblems } = req.body
    const filter = {name: req.query.name, birth: dateValidation(req.query.birth)}
    const update = {name: name, birth: dateValidation(birth), sex: sex, healthProblems: healthProblems, lastUpdate: Date.now()}
    
    if(!(filter.name&&filter.birth&&update.birth))return response(res, 400, "Invalid query format/data")
        
    const customer = await Customer.findOneAndUpdate(filter, update, {new: true})
        .catch((err)=> {return response(res, 500, err.message)})
    
    if(!customer) return response(res, 404, "Customer not found")

    return response(res, 200, "Customer "+customer.name+" edited with successfully")
})

router.patch('/ECHP', async (req, res)=>{ // ECHP - EDIT CUSTOMER HEALTH PROBLEM 
    const {id} = req.query
    const list = req.body.healthProblems
    
    if(!(id&&list?.length)) return response(res, 400, "Invalid request body/query")
    
    const customer = await Customer.findById(id).exec()
    if(!customer) return response(res, 404, "Customer not found")
    
    const removed = []
    const added = [] 
    
    list.forEach(hp=>{ // hp -> Health Problem of request list // HP -> Health Problem of customer list 
        customer.healthProblems.find((HP, index) => {
            if((HP.name===hp.name)&&(HP.degree===hp.degree)){ //if the health problem already exists, it will be removed
                return removed.push(customer.healthProblems.splice(index, 1))
            }else if(index===customer.healthProblems.length-1){ //if the health problem not exists, it will be additioned
                customer.healthProblems.push(hp)
                return added.push(hp)
            }
        })
    })
    const result = await Customer.findByIdAndUpdate(id, {$set: {healthProblems: customer.healthProblems, lastUpdate: Date.now()}}, {new: true})
        .catch((err)=> {return response(res, 500, err.message)})
    
    return res.status(200).send({
        message: 'The list of customer health problems has been updated successfully',
        changes: { addedToList: added, removedFromList: removed},
        Customer: result
    })  
})

module.exports = router 