package domain.tts.entity

import java.time.LocalDateTime
import java.util.*

data class Speech(
    val id: String,
    val scriptId: String,
    val requestId: String?,          // 클로바 더빙 요청 ID
    val audioUrl: String?,           // 생성된 오디오 파일 URL
    val downloadUrl: String?,        // 클로바에서 제공하는 다운로드 URL
    val status: SpeechStatus,
    val speaker: String,
    val errorMessage: String? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun create(
            scriptId: String,
            speaker: String,
            requestId: String? = null
        ): Speech {
            val now = LocalDateTime.now()
            return Speech(
                id = UUID.randomUUID().toString(),
                scriptId = scriptId,
                requestId = requestId,
                audioUrl = null,
                downloadUrl = null,
                status = SpeechStatus.PROCESSING,
                speaker = speaker,
                createdAt = now,
                updatedAt = now
            )
        }
    }
    
    fun withCompleted(downloadUrl: String, audioUrl: String): Speech {
        return this.copy(
            downloadUrl = downloadUrl,
            audioUrl = audioUrl,
            status = SpeechStatus.COMPLETED,
            updatedAt = LocalDateTime.now()
        )
    }
    
    fun withFailed(errorMessage: String): Speech {
        return this.copy(
            status = SpeechStatus.FAILED,
            errorMessage = errorMessage,
            updatedAt = LocalDateTime.now()
        )
    }
}

enum class SpeechStatus {
    PROCESSING,  // 생성 중
    COMPLETED,   // 완료
    FAILED       // 실패
}
