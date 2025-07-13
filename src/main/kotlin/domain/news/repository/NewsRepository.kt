package domain.news.repository

import domain.news.entity.NewsArticle

interface NewsRepository {
    suspend fun save(article: NewsArticle): NewsArticle
    suspend fun findById(id: String): NewsArticle?
    suspend fun findAll(): List<NewsArticle>
    suspend fun findByCategory(category: String): List<NewsArticle>
    suspend fun findRandomNews(): NewsArticle?
    suspend fun delete(id: String): Boolean
    suspend fun update(article: NewsArticle): NewsArticle?
}
