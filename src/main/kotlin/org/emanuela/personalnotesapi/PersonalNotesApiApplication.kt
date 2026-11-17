package org.emanuela.personalnotesapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PersonalNotesApiApplication

fun main(args: Array<String>) {
    runApplication<PersonalNotesApiApplication>(*args)
}
