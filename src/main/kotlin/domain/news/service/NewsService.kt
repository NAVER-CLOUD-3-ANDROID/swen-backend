package domain.news.service

import domain.news.entity.NewsArticle
import domain.news.entity.NewsScript
import domain.news.repository.NewsRepository
import domain.news.repository.NewsScriptRepository

class NewsService(
    private val newsRepository: NewsRepository,
    private val newsScriptRepository: NewsScriptRepository
) {
    
    suspend fun saveNews(article: NewsArticle): NewsArticle {
        return newsRepository.save(article)
    }
    
    suspend fun getRandomNews(): NewsArticle? {
        return newsRepository.findRandomNews()
    }
    
    suspend fun getNewsByCategory(category: String): List<NewsArticle> {
        return newsRepository.findByCategory(category)
    }
    
    suspend fun getAllNews(): List<NewsArticle> {
        return newsRepository.findAll()
    }
    
    suspend fun generateAndSaveScript(newsId: String, script: String): NewsScript {
        val newsScript = NewsScript.create(
            newsId = newsId,
            script = script
        )
        return newsScriptRepository.save(newsScript)
    }
    
    suspend fun getScriptByNewsId(newsId: String): NewsScript? {
        return newsScriptRepository.findByNewsId(newsId)
    }
    
    suspend fun updateScriptWithAudio(scriptId: String, audioUrl: String, duration: Int): NewsScript? {
        val script = newsScriptRepository.findById(scriptId) ?: return null
        val updatedScript = script.copy(audioUrl = audioUrl, duration = duration)
        return newsScriptRepository.update(updatedScript)
    }
}
