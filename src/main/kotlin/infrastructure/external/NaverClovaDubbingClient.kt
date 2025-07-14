package infrastructure.external

import domain.tts.entity.SpeechRequest
import infrastructure.config.AppConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable

class NaverClovaDubbingClient(
    private val httpClient: HttpClient,
    private val appConfig: AppConfig
) {
    private val baseUrl = appConfig.externalApi.naver.clovaDubbing.baseUrl
    private val apiKeyId = appConfig.externalApi.naver.clovaDubbing.apiKeyId
    private val apiKey = appConfig.externalApi.naver.clovaDubbing.apiKey
    
    /**
     * TTS 음성 생성 요청
     */
    suspend fun createSpeech(request: SpeechRequest): ClovaDubbingResponse {
        val response = httpClient.post("$baseUrl/13604") { // 실제 서비스 ID로 변경 필요
            headers {
                append("X-NCP-APIGW-API-KEY-ID", apiKeyId)
                append("X-NCP-APIGW-API-KEY", apiKey)
                append("Content-Type", "application/json")
            }
            setBody(request)
        }
        
        return response.body<ClovaDubbingResponse>()
    }
    
    /**
     * TTS 생성 상태 확인
     */
    suspend fun getSpeechStatus(requestId: String): SpeechStatusResponse {
        val response = httpClient.get("$baseUrl/13604/$requestId") { // 실제 서비스 ID로 변경 필요
            headers {
                append("X-NCP-APIGW-API-KEY-ID", apiKeyId)
                append("X-NCP-APIGW-API-KEY", apiKey)
            }
        }
        
        return response.body<SpeechStatusResponse>()
    }
    
    /**
     * 완성된 음성 파일 다운로드
     */
    suspend fun downloadSpeech(downloadUrl: String): ByteArray {
        val response = httpClient.get(downloadUrl)
        return response.body<ByteArray>()
    }
    
    /**
     * TTS 생성 완료까지 대기 (폴링)
     */
    suspend fun waitForCompletion(requestId: String, maxWaitTimeMs: Long = 300000): SpeechStatusResponse? {
        val startTime = System.currentTimeMillis()
        
        while (System.currentTimeMillis() - startTime < maxWaitTimeMs) {
            val status = getSpeechStatus(requestId)
            
            when (status.status) {
                "complete" -> return status
                "fail" -> throw Exception("TTS 생성 실패: ${status.message ?: "알 수 없는 오류"}")
                "progress" -> {
                    delay(5000) // 5초 대기
                    continue
                }
                else -> {
                    delay(5000)
                    continue
                }
            }
        }
        
        throw Exception("TTS 생성 시간 초과")
    }
}

@Serializable
data class ClovaDubbingResponse(
    val requestId: String,
    val status: String,
    val message: String? = null
)

@Serializable
data class SpeechStatusResponse(
    val requestId: String,
    val status: String, // "progress", "complete", "fail"
    val message: String? = null,
    val downloadUrl: String? = null,
    val editScript: String? = null
)
