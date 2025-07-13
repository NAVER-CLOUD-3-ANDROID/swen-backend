package infrastructure.external

import domain.news.entity.NewsArticle
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

class NaverNewsClient(
    private val httpClient: HttpClient,
    private val clientId: String,
    private val clientSecret: String
) {
    
    suspend fun searchNews(
        query: String? = null,
        display: Int = 10,
        start: Int = 1,
        sort: String = "date"
    ): List<NewsArticle> {
        return try {
            val response = httpClient.get("https://openapi.naver.com/v1/search/news.json") {
                header("X-Naver-Client-Id", clientId)
                header("X-Naver-Client-Secret", clientSecret)
                
                // query가 null이 아닐 때만 파라미터 추가
                query?.let { parameter("query", it) }
                
                parameter("display", display)
                parameter("start", start)
                parameter("sort", sort)
            }
            
            if (response.status == HttpStatusCode.OK) {
                val naverResponse: NaverNewsResponse = response.body()
                naverResponse.items.map { item ->
                    NewsArticle.create(
                        title = item.title.removeHtmlTags(),
                        content = item.description.removeHtmlTags(),
                        url = item.link,
                        publishedAt = item.pubDate,
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("네이버 뉴스 API 호출 실패: ${e.message}")
            emptyList()
        }
    }
    
    private fun String.removeHtmlTags(): String {
        return this.replace(Regex("<[^>]*>"), "")
            .replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
    }
}

@Serializable
data class NaverNewsResponse(
    val lastBuildDate: String,
    val total: Int,
    val start: Int,
    val display: Int,
    val items: List<NaverNewsItem>
)

@Serializable
data class NaverNewsItem(
    val title: String,
    val originallink: String,
    val link: String,
    val description: String,
    val pubDate: String
)
