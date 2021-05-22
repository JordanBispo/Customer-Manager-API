const express = require('express')
const app = express()
const dotenv = require('dotenv')
const Routes = require('./routes')
const mongoose = require('mongoose')
const PORT = process.env.PORT || 4000

dotenv.config() 

mongoose.connect(process.env.DATABASE_ACCESS, { useNewUrlParser: true, useUnifiedTopology: true, useFindAndModify: false },  (e)=>{if(e){console.log(e)}else console.log("Data base conected with sucess")})
mongoose.Promise = global.Promise


app.use(express.json())
app.use('/api', Routes)
app.listen(PORT, () => console.log('Server runing in port: '+PORT)) 

    