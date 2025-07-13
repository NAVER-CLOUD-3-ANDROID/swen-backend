package application.usecase

import domain.news.entity.NewsArticle
import domain.news.entity.NewsScript
import domain.news.service.NewsService

class GetRandomNewsUseCase(
    private val newsService: NewsService
) {
    
    suspend fun execute(): NewsWithScript? {
        // 1. 랜덤 뉴스 조회
        val article = newsService.getRandomNews() ?: return null
        
        // 2. 해당 뉴스의 스크립트 조회
        val script = newsService.getScriptByNewsId(article.id)
        
        return NewsWithScript(
            article = article,
            script = script
        )
    }
}

data class NewsWithScript(
    val article: NewsArticle,
    val script: NewsScript?
)
