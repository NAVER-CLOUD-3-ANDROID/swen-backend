package domain.tts.repository

import domain.tts.entity.Speech

interface SpeechRepository {
    suspend fun save(speech: Speech): Speech
    suspend fun findById(id: String): Speech?
    suspend fun findByScriptId(scriptId: String): Speech?
    suspend fun findByRequestId(requestId: String): Speech?
    suspend fun update(speech: Speech): Speech
    suspend fun delete(id: String): Boolean
}
