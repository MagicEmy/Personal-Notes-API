package org.emanuela.personalnotesapi.database.repository

import org.bson.types.ObjectId
import org.emanuela.personalnotesapi.database.model.Note
import org.springframework.data.mongodb.repository.MongoRepository

interface NoteRepository: MongoRepository<Note, ObjectId> {
    fun findByOwnerId(ownerId: ObjectId): List<Note>
}