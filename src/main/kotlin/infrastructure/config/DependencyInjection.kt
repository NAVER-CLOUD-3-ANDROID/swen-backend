package infrastructure.config

import application.usecase.GenerateNewsWithScriptUseCase
import application.usecase.GenerateNewsWithAudioUseCase
import domain.tts.service.TTSService
import infrastructure.external.NaverHyperclovaClient
import infrastructure.external.NaverNewsClient
import infrastructure.external.NaverClovaDubbingClient
import infrastructure.repository.SpeechRepositoryImpl
import infrastructure.repository.SpeechTable
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

class DependencyInjection(private val application: Application) {
    
    // 설정 로드
    val appConfig = AppConfig.from(application)
    
    // 데이터베이스 초기화
    private val dataSource = createDataSource()
    private val database = Database.connect(dataSource)
    
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
    
    val naverClovaDubbingClient = NaverClovaDubbingClient(
        httpClient = httpClient,
        appConfig = appConfig
    )
    
    // Repositories
    val speechRepository = SpeechRepositoryImpl()
    
    // Services
    val ttsService = TTSService(
        clovaDubbingClient = naverClovaDubbingClient,
        speechRepository = speechRepository
    )
    
    // UseCases
    val generateNewsWithScriptUseCase = GenerateNewsWithScriptUseCase(
        naverNewsClient = naverNewsClient,
        naverHyperclovaClient = naverHyperclovaClient
    )
    
    val generateNewsWithAudioUseCase = GenerateNewsWithAudioUseCase(
        naverNewsClient = naverNewsClient,
        naverHyperclovaClient = naverHyperclovaClient,
        ttsService = ttsService
    )
    
    init {
        initializeDatabase()
    }
    
    private fun createDataSource(): HikariDataSource {
        val config = HikariConfig().apply {
            jdbcUrl = appConfig.database.mysql.jdbcUrl
            username = appConfig.database.mysql.username
            password = appConfig.database.mysql.password
            driverClassName = "com.mysql.cj.jdbc.Driver"
            
            // HikariCP 설정
            maximumPoolSize = appConfig.database.hikari.maximumPoolSize
            minimumIdle = appConfig.database.hikari.minimumIdle
            connectionTimeout = appConfig.database.hikari.connectionTimeout
            idleTimeout = appConfig.database.hikari.idleTimeout
            maxLifetime = appConfig.database.hikari.maxLifetime
            
            // MySQL 최적화
            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
            addDataSourceProperty("useServerPrepStmts", "true")
            addDataSourceProperty("useLocalSessionState", "true")
            addDataSourceProperty("rewriteBatchedStatements", "true")
            addDataSourceProperty("cacheResultSetMetadata", "true")
            addDataSourceProperty("cacheServerConfiguration", "true")
            addDataSourceProperty("elideSetAutoCommits", "true")
            addDataSourceProperty("maintainTimeStats", "false")
        }
        
        return HikariDataSource(config)
    }
    
    private fun initializeDatabase() {
        transaction {
            SchemaUtils.createMissingTablesAndColumns(SpeechTable)
        }
        println("✅ 데이터베이스 테이블 초기화 완료")
    }
    
    fun cleanup() {
        httpClient.close()
        dataSource.close()
    }
}
