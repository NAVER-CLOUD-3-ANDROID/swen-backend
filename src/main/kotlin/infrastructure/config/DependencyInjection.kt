package infrastructure.config

import application.usecase.GenerateNewsWithScriptUseCase
import infrastructure.external.NaverHyperclovaClient
import infrastructure.external.NaverNewsClient
import io.ktor.server.application.*

class DependencyInjection(private val application: Application) {
    
    // 설정 로드
    val appConfig = AppConfig.from(application)
    
    // HTTP Client
    val httpClient = createHttpClient()
    
    // External Clients
    val naverNewsClient = NaverNewsClient(
        httpClient = httpClient,
        clientId = appConfig.externalApi.naver.news.clientId,
        clientSecret = appConfig.externalApi.naver.news.clientSecret
    )
    
    val naverHyperclovaClient = NaverHyperclovaClient(
        httpClient = httpClient,
        config = appConfig.externalApi.naver.hyperclova
    )
    
    // 활성화된 UseCase
    val generateNewsWithScriptUseCase = GenerateNewsWithScriptUseCase(
        naverNewsClient = naverNewsClient,
        naverHyperclovaClient = naverHyperclovaClient
    )
    
    fun cleanup() {
        httpClient.close()
    }
}
