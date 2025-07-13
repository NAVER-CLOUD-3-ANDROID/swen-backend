package application.usecase

import domain.news.entity.NewsScript
import domain.news.service.NewsService
import domain.news.repository.NewsRepository
import infrastructure.external.LLMClient

class GenerateScriptUseCase(
    private val llmClient: LLMClient,
    private val newsService: NewsService,
    private val newsRepository: NewsRepository
) {
    
    suspend fun execute(newsId: String): NewsScript? {
        // 1. 뉴스 기사 조회
        val article = newsRepository.findById(newsId) ?: return null
        
        // 2. LLM을 통해 스크립트 생성
        val script = llmClient.generateNewsScript(article)
        
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
        
        // 3. 스크립트 생성
        val script = llmClient.generateNewsScript(article)
        val savedScript = newsService.generateAndSaveScript(article.id, script)
        
        return Pair(article.title, savedScript)
    }
}
