package application.usecase

import domain.news.entity.NewsScript
import domain.news.service.NewsService
import domain.news.repository.NewsRepository
import infrastructure.external.NaverHyperclovaClient

class GenerateScriptUseCase(
    private val naverHyperclovaClient: NaverHyperclovaClient,
    private val newsService: NewsService,
    private val newsRepository: NewsRepository
) {
    
    suspend fun execute(newsId: String): NewsScript? {
        // 1. 뉴스 기사 조회
        val article = newsRepository.findById(newsId) ?: return null
        
        // 2. 하이퍼클로바를 통해 스크립트 생성
        val script = naverHyperclovaClient.generateNewsScript(article)
        
        // 3. 생성된 스크립트 저장
        return newsService.generateAndSaveScript(newsId, script)
    }
    
    suspend fun executeForRandomNews(): Pair<String, NewsScript>? {
        // 1. 랜덤 뉴스 조회
        val article = newsService.getRandomNews() ?: return null
        
        // 2. 이미 스크립트가 있는지 확인
        val existingScript = newsService.getScriptByNewsId(article.id)
        if (existingScript != null) {
            return Pair(article.title, existingScript)
        }
        
        // 3. 하이퍼클로바로 스크립트 생성
        val script = naverHyperclovaClient.generateNewsScript(article)
        val savedScript = newsService.generateAndSaveScript(article.id, script)
        
        return Pair(article.title, savedScript)
    }
}
