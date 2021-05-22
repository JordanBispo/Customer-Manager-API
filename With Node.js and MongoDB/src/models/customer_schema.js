const mongoose = require('mongoose')

const HealthProblems = new mongoose.Schema({
    name:{
        type: String,
        required: true,
    },
    degree: {
        type: Number,
        min: [1, 'The value of path `{PATH}` ({VALUE}) is beneath the limit ({MIN}).'],
        max: [10, 'The value of path `{PATH}` ({VALUE}) exceeds the limit ({MAX}).'],
        required: true,
    } 
})

const CustomerSchema =  new mongoose.Schema({
    name: {
        type: String,
        required: true
    },
    birth: {
        type: Date,
        required: true
    },
    sex: {
        type: String,
        enum: ['male', 'female', 'masculino', 'feminino'], 
        required: true 
    },
    createAt: {
        type: Date,
        default: Date.now()
    },
    lastUpdate: {
        type: Date,
        default: Date.now()
    },
    healthProblems: [HealthProblems]
})



const Customer =  mongoose.model('CustomersTable', CustomerSchema, 'Customers')

module.exports = Customer