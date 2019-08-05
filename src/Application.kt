package org.tyaa.kotlin.ktor

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.features.*
import org.slf4j.event.*
import io.ktor.routing.*
import io.ktor.http.*
import com.fasterxml.jackson.databind.*
import freemarker.cache.ClassTemplateLoader
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.jackson.*
import org.jetbrains.exposed.sql.Database
import org.tyaa.kotlin.ktor.dao.DAOFacadeDatabase

val dao = DAOFacadeDatabase(Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver"))

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    dao.init()

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    install(FreeMarker){
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }

    routing {

        route("/"){
            get{
                call.respond(FreeMarkerContent("index.ftl", mapOf("employees" to dao.getAllEmployees())))
            }
        }
        route("/employee"){
            get {
                val action = (call.request.queryParameters["action"] ?: "new")
                when(action){
                    "new" -> call.respond(FreeMarkerContent("employee.ftl",
                        mapOf("action" to action)))
                    "edit" -> {
                        val id = call.request.queryParameters["id"]
                        if(id != null){
                            call.respond(FreeMarkerContent("employee.ftl",
                                mapOf("employee" to dao.getEmployee(id.toInt()),
                                    "action" to action)))
                        }
                    }
                }
            }
            post{
                val postParameters: Parameters = call.receiveParameters()
                val action = postParameters["action"] ?: "new"
                when(action){
                    "new" -> dao.createEmployee(postParameters["name"] ?: "", postParameters["email"] ?: "", postParameters["city"] ?: "")
                    "edit" ->{
                        val id = postParameters["id"]
                        if(id != null)
                            dao.updateEmployee(id.toInt(), postParameters["name"] ?: "", postParameters["email"] ?: "", postParameters["city"] ?: "")
                    }
                }
                call.respond(FreeMarkerContent("index.ftl", mapOf("employees" to dao.getAllEmployees())))
            }
        }
        route("/delete"){
            get{
                val id = call.request.queryParameters["id"]
                if(id != null){
                    dao.deleteEmployee(id.toInt())
                    call.respond(FreeMarkerContent("index.ftl", mapOf("employees" to dao.getAllEmployees())))
                }
            }
        }
    }
}

