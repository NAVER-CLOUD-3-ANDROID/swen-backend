package domain.news.repository

import domain.news.entity.NewsScript

interface NewsScriptRepository {
    suspend fun save(script: NewsScript): NewsScript
    suspend fun findById(id: String): NewsScript?
    suspend fun findByNewsId(newsId: String): NewsScript?
    suspend fun findAll(): List<NewsScript>
    suspend fun delete(id: String): Boolean
    suspend fun update(script: NewsScript): NewsScript?
}
