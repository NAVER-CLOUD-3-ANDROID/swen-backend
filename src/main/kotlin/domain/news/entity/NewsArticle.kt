package domain.news.entity

import kotlinx.serialization.Serializable

@Serializable
data class NewsArticle(
    val id: String,
    val title: String,
    val content: String,
    val url: String,
    val summary: String? = null,
    val publishedAt: String,
    val category: String? = null,
    val imageUrl: String? = null
) {
    companion object {
        fun create(
            title: String,
            content: String,
            url: String,
            publishedAt: String,
            category: String? = null,
            imageUrl: String? = null
        ): NewsArticle {
            return NewsArticle(
                id = generateId(),
                title = title,
                content = content,
                url = url,
                publishedAt = publishedAt,
                category = category,
                imageUrl = imageUrl
            )
        }
        
        private fun generateId(): String {
            return System.currentTimeMillis().toString() + (1000..9999).random()
        }
    }
}
