package domain.news.entity

import kotlinx.serialization.Serializable

@Serializable
data class NewsScript(
    val id: String,
    val newsId: String,
    val script: String,
    val audioUrl: String? = null,
    val duration: Int? = null, // 초 단위
    val createdAt: String
) {
    companion object {
        fun create(
            newsId: String,
            script: String,
            audioUrl: String? = null,
            duration: Int? = null
        ): NewsScript {
            return NewsScript(
                id = generateId(),
                newsId = newsId,
                script = script,
                audioUrl = audioUrl,
                duration = duration,
                createdAt = java.time.LocalDateTime.now().toString()
            )
        }
        
        private fun generateId(): String {
            return "script_" + System.currentTimeMillis().toString() + (1000..9999).random()
        }
    }
}
