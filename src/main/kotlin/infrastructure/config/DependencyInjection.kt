package infrastructure.config

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
    
    // TODO: Repository 구현체들 추가 (MySQL 연결 후)
    // val newsRepository = NewsRepositoryImpl(database)
    // val newsScriptRepository = NewsScriptRepositoryImpl(database)
    
    // TODO: Service 초기화 (Repository 구현 후)
    // val newsService = NewsService(newsRepository, newsScriptRepository)
    
    // TODO: Use Cases 초기화 (Service 구현 후)
    // val collectNewsUseCase = CollectNewsUseCase(naverNewsClient, newsService)
    // val generateScriptUseCase = GenerateScriptUseCase(naverHyperclovaClient, newsService, newsRepository)
    // val getRandomNewsUseCase = GetRandomNewsUseCase(newsService)
    
    fun cleanup() {
        httpClient.close()
    }
}
