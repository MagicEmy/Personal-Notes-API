package org.emanuela.personalnotesapi.database.model

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("refresh_tokens")
data class RefreshToken(
    val userId: ObjectId,
    @Indexed(expireAfter = "0s")
    val expiresAt: Instant,
    val hashedToken: String,
    val createdAt: Instant = Instant.now(),
)

// MongoDB will check the expiresAt field and automatically delete
// the document when the time is reached.
// The @Indexed(expireAfter = "0s") annotation tells MongoDB
// to create a TTL index on the expiresAt field,
// which will cause documents to be automatically
// removed when they expire.