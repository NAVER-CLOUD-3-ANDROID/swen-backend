package infrastructure.repository

import domain.tts.entity.Speech
import domain.tts.entity.SpeechStatus
import domain.tts.repository.SpeechRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class SpeechRepositoryImpl : SpeechRepository {
    
    override suspend fun save(speech: Speech): Speech {
        return transaction {
            SpeechTable.insert { row ->
                row[id] = speech.id
                row[scriptId] = speech.scriptId
                row[requestId] = speech.requestId
                row[audioUrl] = speech.audioUrl
                row[downloadUrl] = speech.downloadUrl
                row[status] = speech.status.name
                row[speaker] = speech.speaker
                row[errorMessage] = speech.errorMessage
                row[createdAt] = speech.createdAt
                row[updatedAt] = speech.updatedAt
            }
            speech
        }
    }
    
    override suspend fun findById(id: String): Speech? {
        return transaction {
            SpeechTable.select { SpeechTable.id eq id }
                .map { row -> mapRowToSpeech(row) }
                .singleOrNull()
        }
    }
    
    override suspend fun findByScriptId(scriptId: String): Speech? {
        return transaction {
            SpeechTable.select { SpeechTable.scriptId eq scriptId }
                .map { row -> mapRowToSpeech(row) }
                .singleOrNull()
        }
    }
    
    override suspend fun findByRequestId(requestId: String): Speech? {
        return transaction {
            SpeechTable.select { SpeechTable.requestId eq requestId }
                .map { row -> mapRowToSpeech(row) }
                .singleOrNull()
        }
    }
    
    override suspend fun update(speech: Speech): Speech {
        return transaction {
            SpeechTable.update({ SpeechTable.id eq speech.id }) { row ->
                row[scriptId] = speech.scriptId
                row[requestId] = speech.requestId
                row[audioUrl] = speech.audioUrl
                row[downloadUrl] = speech.downloadUrl
                row[status] = speech.status.name
                row[speaker] = speech.speaker
                row[errorMessage] = speech.errorMessage
                row[updatedAt] = speech.updatedAt
            }
            speech
        }
    }
    
    override suspend fun delete(id: String): Boolean {
        return transaction {
            SpeechTable.deleteWhere { SpeechTable.id eq id } > 0
        }
    }
    
    private fun mapRowToSpeech(row: ResultRow): Speech {
        return Speech(
            id = row[SpeechTable.id],
            scriptId = row[SpeechTable.scriptId],
            requestId = row[SpeechTable.requestId],
            audioUrl = row[SpeechTable.audioUrl],
            downloadUrl = row[SpeechTable.downloadUrl],
            status = SpeechStatus.valueOf(row[SpeechTable.status]),
            speaker = row[SpeechTable.speaker],
            errorMessage = row[SpeechTable.errorMessage],
            createdAt = row[SpeechTable.createdAt],
            updatedAt = row[SpeechTable.updatedAt]
        )
    }
}

object SpeechTable : Table("speeches") {
    val id = varchar("id", 255)
    val scriptId = varchar("script_id", 255)
    val requestId = varchar("request_id", 255).nullable()
    val audioUrl = text("audio_url").nullable()
    val downloadUrl = text("download_url").nullable()
    val status = varchar("status", 50)
    val speaker = varchar("speaker", 50)
    val errorMessage = text("error_message").nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
    
    override val primaryKey = PrimaryKey(id)
    
    init {
        index(false, scriptId)
        index(false, requestId)
    }
}
