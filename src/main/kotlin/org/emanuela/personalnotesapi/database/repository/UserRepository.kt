package org.emanuela.personalnotesapi.database.repository

import org.bson.types.ObjectId
import org.emanuela.personalnotesapi.database.model.User
import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepository: MongoRepository<User, ObjectId> {
    fun findByEmail(email: String): User?
}