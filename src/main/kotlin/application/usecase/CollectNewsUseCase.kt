package application.usecase

import domain.news.entity.NewsArticle
import domain.news.service.NewsService
import infrastructure.external.NaverNewsClient

class CollectNewsUseCase(
    private val naverNewsClient: NaverNewsClient,
    private val newsService: NewsService
) {
    
    suspend fun execute(query: String = "최신뉴스", count: Int = 10): List<NewsArticle> {
        // 1. 네이버 뉴스 API에서 뉴스 수집
        val newsArticles = naverNewsClient.searchNews(
            query = query,
            display = count
        )
        
        // 2. 수집한 뉴스를 데이터베이스에 저장
        val savedArticles = mutableListOf<NewsArticle>()
        for (article in newsArticles) {
            try {
                val savedArticle = newsService.saveNews(article)
                savedArticles.add(savedArticle)
            } catch (e: Exception) {
                println("뉴스 저장 실패: ${article.title} - ${e.message}")
            }
        }
        
        return savedArticles
    }
}
