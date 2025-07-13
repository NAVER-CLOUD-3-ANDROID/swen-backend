package presentation.dto

import domain.news.entity.NewsArticle
import domain.news.entity.NewsScript
import kotlinx.serialization.Serializable

@Serializable
data class NewsResponse(
    val id: String,
    val title: String,
    val content: String,
    val url: String,
    val summary: String?,
    val publishedAt: String,
    val category: String?,
    val imageUrl: String?
) {
    companion object {
        fun from(article: NewsArticle): NewsResponse {
            return NewsResponse(
                id = article.id,
                title = article.title,
                content = article.content,
                url = article.url,
                summary = article.summary,
                publishedAt = article.publishedAt,
                category = article.category,
                imageUrl = article.imageUrl
            )
        }
    }
}

@Serializable
data class NewsScriptResponse(
    val id: String,
    val newsId: String,
    val script: String,
    val audioUrl: String?,
    val duration: Int?,
    val createdAt: String
) {
    companion object {
        fun from(script: NewsScript): NewsScriptResponse {
            return NewsScriptResponse(
                id = script.id,
                newsId = script.newsId,
                script = script.script,
                audioUrl = script.audioUrl,
                duration = script.duration,
                createdAt = script.createdAt
            )
        }
    }
}

@Serializable
data class RandomNewsResponse(
    val news: NewsResponse,
    val script: NewsScriptResponse?
)

@Serializable
data class CollectNewsRequest(
    val query: String = "최신뉴스",
    val count: Int = 10
)

@Serializable
data class GenerateScriptRequest(
    val newsId: String
)
