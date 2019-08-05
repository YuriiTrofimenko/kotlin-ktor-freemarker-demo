package org.tyaa.kotlin.ktor.dao

import org.tyaa.kotlin.ktor.model.Employee
import java.io.Closeable

interface IDAOFacade: Closeable {
    fun init()
    fun createEmployee(name:String, email:String, city:String)
    fun updateEmployee(id:Int, name:String, email:String, city:String)
    fun deleteEmployee(id:Int)
    fun getEmployee(id:Int): Employee?
    fun getAllEmployees(): List<Employee>
}