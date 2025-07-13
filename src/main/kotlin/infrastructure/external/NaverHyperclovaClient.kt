package infrastructure.external

import domain.news.entity.NewsArticle
import infrastructure.config.NaverHyperclovaConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import java.util.*

class NaverHyperclovaClient(
    private val httpClient: HttpClient,
    private val config: NaverHyperclovaConfig
) {
    
    suspend fun generateNewsScript(article: NewsArticle): String {
        return try {
            val prompt = createScriptPrompt(article)
            
            val requestBody = HyperclovaRequest(
                messages = listOf(
                    HyperclovaMessage(
                        role = "system",
                        content = "당신은 뉴스 기사를 자연스러운 음성 스크립트로 변환하는 전문가입니다."
                    ),
                    HyperclovaMessage(
                        role = "user", 
                        content = prompt
                    )
                ),
                topP = 0.8,
                topK = 0,
                maxTokens = 500,
                temperature = 0.5,
                repeatPenalty = 5.0,
                stopBefore = listOf(),
                includeAiFilters = true
            )
            
            val response = httpClient.post(config.apiGatewayUrl) {
                header("X-NCP-CLOVASTUDIO-API-KEY", config.apiKey)
                header("X-NCP-CLOVASTUDIO-API-KEY-PRIMARY", config.apiKeyPrimary)
                header("X-NCP-CLOVASTUDIO-REQUEST-ID", generateRequestId())
                header("Content-Type", "application/json")
                header("Accept", "text/event-stream")
                setBody(requestBody)
            }
            
            if (response.status == HttpStatusCode.OK) {
                val hyperclovaResponse: HyperclovaResponse = response.body()
                hyperclovaResponse.result.message.content.takeIf { it.isNotBlank() }
                    ?: generateFallbackScript(article)
            } else {
                println("하이퍼클로바 API 호출 실패: ${response.status}")
                generateFallbackScript(article)
            }
        } catch (e: Exception) {
            println("하이퍼클로바 API 호출 실패: ${e.message}")
            generateFallbackScript(article)
        }
    }
    
    private fun createScriptPrompt(article: NewsArticle): String {
        return """
다음 뉴스 기사를 음성으로 읽기 적합한 자연스러운 스크립트로 변환해주세요.

제목: ${article.title}
내용: ${article.content}

변환 규칙:
1. 1분 내로 읽을 수 있는 적절한 길이로 요약
2. 딱딱한 문어체를 자연스러운 구어체로 변환
3. 음성으로 들었을 때 이해하기 쉽게 구성
4. 사실 기반으로 중요한 정보는 유지하되 불필요한 세부사항은 제거
5. "안녕하세요" 같은 인사말로 시작하고 "이상입니다" 같은 마무리로 끝내기
6. 숫자나 전문용어는 쉽게 읽을 수 있도록 표현

스크립트:
        """.trimIndent()
    }
    
    private fun generateFallbackScript(article: NewsArticle): String {
        return """
안녕하세요. 오늘의 뉴스를 전해드리겠습니다.

${article.title}

${article.content.take(300).let { 
    if (article.content.length > 300) "$it..." else it 
}}

자세한 내용은 원문을 확인해주시기 바랍니다. 이상으로 뉴스를 마치겠습니다.
        """.trimIndent()
    }
    
    private fun generateRequestId(): String {
        return UUID.randomUUID().toString()
    }
}

@Serializable
data class HyperclovaRequest(
    val messages: List<HyperclovaMessage>,
    val topP: Double,
    val topK: Int,
    val maxTokens: Int,
    val temperature: Double,
    val repeatPenalty: Double,
    val stopBefore: List<String>,
    val includeAiFilters: Boolean
)

@Serializable
data class HyperclovaMessage(
    val role: String,
    val content: String
)

@Serializable
data class HyperclovaResponse(
    val status: HyperclovaStatus,
    val result: HyperclovaResult
)

@Serializable
data class HyperclovaStatus(
    val code: String,
    val message: String
)

@Serializable
data class HyperclovaResult(
    val message: HyperclovaMessage,
    val inputLength: Int,
    val outputLength: Int,
    val stopReason: String,
    val seed: Long,
    val aiFilter: List<HyperclovaAiFilter>? = null
)

@Serializable
data class HyperclovaAiFilter(
    val groupName: String,
    val name: String,
    val score: Double,
    val result: String
)
