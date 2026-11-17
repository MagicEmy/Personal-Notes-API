package org.emanuela.personalnotesapi.controllers

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import org.bson.types.ObjectId
import org.emanuela.personalnotesapi.database.model.Note
import org.emanuela.personalnotesapi.database.repository.NoteRepository
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@RestController
@RequestMapping("/notes")
class NoteController (
    private val repository: NoteRepository
){

    data class NoteRequest(
        @field:NotBlank(message = "Title is required")
        val title: String,
        val content: String,
        val color: Long
    )

    data class NotePatchRequest(
        @field:Pattern(regexp = ".*\\S.*", message = "Title must not be blank")
        val title: String? = null,
        val content: String? = null,
        val color: Long? = null
    )

    data class NoteResponse(
        val id: String,
        val title: String,
        val content: String,
        val color: Long,
        val createdAt: Instant
    )

    @PostMapping
    fun save(
        @Valid @RequestBody body: NoteRequest
    ): NoteResponse {
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        if (!ObjectId.isValid(ownerId)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid access token")
        }
        val ownerObjectId = ObjectId(ownerId)

        val note = repository.save(
            Note(
                id = ObjectId.get(),
                title = body.title,
                content = body.content,
                color = body.color,
                createdAt = Instant.now(),
                ownerId = ownerObjectId
            )
        )
        return note.toResponse()
    }

    @GetMapping
    fun findByOwnerId(): List<NoteResponse> {
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        return repository.findByOwnerId(ObjectId(ownerId)).map {
            it.toResponse()
        }
    }

    @PatchMapping("/{id}")
    fun patch(
        @PathVariable id: String,
        @Valid @RequestBody body: NotePatchRequest
    ): NoteResponse {
        if (!ObjectId.isValid(id)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid note id")
        }
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        if (!ObjectId.isValid(ownerId)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid access token")
        }
        if (body.title == null && body.content == null && body.color == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one field must be provided")
        }

        val note = repository.findById(ObjectId(id)).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found")
        }
        if (note.ownerId != ObjectId(ownerId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found")
        }

        return repository.save(
            note.copy(
                title = body.title ?: note.title,
                content = body.content ?: note.content,
                color = body.color ?: note.color
            )
        ).toResponse()
    }

    @DeleteMapping(path = ["/{id}"])
    fun deleteById(@PathVariable id: String) {
        if (!ObjectId.isValid(id)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid note id")
        }

        val note = repository.findById(ObjectId(id)).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found")
        }

        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        if (!ObjectId.isValid(ownerId)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid access token")
        }

        if (note.ownerId.toHexString() != ownerId) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found")
        }
        repository.deleteById(ObjectId(id))
    }

    private fun Note.toResponse(): NoteController.NoteResponse {
        return NoteResponse(
            id = id.toHexString(),
            title = title,
            content = content,
            color = color,
            createdAt = createdAt
        )
    }
}