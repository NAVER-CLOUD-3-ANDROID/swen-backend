package application.usecase

import domain.news.entity.NewsArticle
import domain.news.entity.NewsScript
import infrastructure.external.NaverNewsClient
import infrastructure.external.NaverHyperclovaClient

class GenerateNewsWithScriptUseCase(
    private val naverNewsClient: NaverNewsClient,
    private val naverHyperclovaClient: NaverHyperclovaClient
) {
    
    suspend fun executeRandom(): NewsWithScriptResult {
        return try {
            // 1. 네이버 뉴스 API에서 최신 뉴스 가져오기
            val newsArticles = naverNewsClient.searchNews(
                display = 20,
                sort = "date" // 최신순
            )
            
            if (newsArticles.isEmpty()) {
                return NewsWithScriptResult.failure("뉴스를 찾을 수 없습니다.")
            }
            
            // 2. 랜덤으로 하나 선택
            val selectedNews = newsArticles.random()
            
            // 3. 하이퍼클로바로 스크립트 생성
            val scriptContent = naverHyperclovaClient.generateNewsScript(selectedNews)
            
            // 4. NewsScript 엔티티 생성
            val newsScript = NewsScript.create(
                newsId = selectedNews.id,
                script = scriptContent
            )
            
            NewsWithScriptResult.success(
                news = selectedNews,
                script = newsScript,
                message = "랜덤 뉴스 스크립트 생성 완료"
            )
            
        } catch (e: Exception) {
            NewsWithScriptResult.failure("랜덤 뉴스 스크립트 생성 실패: ${e.message}")
        }
    }
    
    suspend fun executeByKeyword(keyword: String): NewsWithScriptResult {
        return try {
            // 1. 특정 키워드로 뉴스 검색
            val newsArticles = naverNewsClient.searchNews(
                query = keyword,
                display = 20,
                sort = "date" // 최신순
            )
            
            if (newsArticles.isEmpty()) {
                return NewsWithScriptResult.failure("'$keyword' 관련 뉴스를 찾을 수 없습니다.")
            }
            
            // 2. 랜덤으로 하나 선택
            val latestNews = newsArticles.random()
            
            // 3. 스크립트 생성
            val scriptContent = naverHyperclovaClient.generateNewsScript(latestNews)
            
            val newsScript = NewsScript.create(
                newsId = latestNews.id,
                script = scriptContent
            )
            
            NewsWithScriptResult.success(
                news = latestNews,
                script = newsScript,
                message = "'$keyword' 관련 뉴스 스크립트 생성 완료"
            )
            
        } catch (e: Exception) {
            NewsWithScriptResult.failure("키워드 뉴스 스크립트 생성 실패: ${e.message}")
        }
    }
}

data class NewsWithScriptResult(
    val success: Boolean,
    val news: NewsArticle?,
    val script: NewsScript?,
    val message: String,
    val error: String? = null
) {
    companion object {
        fun success(news: NewsArticle, script: NewsScript, message: String): NewsWithScriptResult {
            return NewsWithScriptResult(
                success = true,
                news = news,
                script = script,
                message = message
            )
        }
        
        fun failure(error: String): NewsWithScriptResult {
            return NewsWithScriptResult(
                success = false,
                news = null,
                script = null,
                message = "실패",
                error = error
            )
        }
    }
}
