package presentation.dto

import domain.news.entity.NewsArticle
import domain.news.entity.NewsScript
import kotlinx.serialization.Serializable

// 통합 API 응답
@Serializable
data class NewsResponse(
    val success: Boolean,
    val message: String,
    val data: NewsWithScriptData? = null,
    val error: String? = null
)

// 뉴스 + 스크립트 데이터
@Serializable
data class NewsWithScriptData(
    val news: NewsData,
    val script: NewsScriptData,
    val processingTime: Long // 처리 시간 (밀리초)
)

// 뉴스 데이터
@Serializable
data class NewsData(
    val id: String,
    val title: String,
    val content: String,
    val url: String,
    val summary: String?,
    val publishedAt: String,
    val imageUrl: String?
) {
    companion object {
        fun from(article: NewsArticle): NewsData {
            return NewsData(
                id = article.id,
                title = article.title,
                content = article.content,
                url = article.url,
                summary = article.summary,
                publishedAt = article.publishedAt,
                imageUrl = article.imageUrl
            )
        }
    }
}

// 스크립트 데이터
@Serializable
data class NewsScriptData(
    val id: String,
    val newsId: String,
    val script: String,
    val audioUrl: String?,
    val duration: Int?,
    val createdAt: String
) {
    companion object {
        fun from(script: NewsScript): NewsScriptData {
            return NewsScriptData(
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

// 요청 DTO들
@Serializable
data class KeywordNewsRequest(
    val keyword: String
)
