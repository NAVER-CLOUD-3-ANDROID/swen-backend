package com.swen

import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import io.ktor.server.routing.get
import io.ktor.server.response.respondText
import io.ktor.server.response.respond
import infrastructure.config.DependencyInjection
import infrastructure.config.*
import presentation.dto.NewsData
import presentation.routing.newsRouting

fun main() {
    // Profile 기반 설정 로딩
    val profile = System.getenv("APP_PROFILE") ?: "local"
    println("Starting application with profile: $profile")
    
    embeddedServer(Netty, port = System.getenv("PORT")?.toInt() ?: 8080) {
        val di = DependencyInjection(this)
        
        configureHTTP()
        configureMonitoring()
        configureSerialization()
        
        // 임시 테스트 라우팅
        routing {
            get("/") {
                call.respondText("Hello, swen! Profile: ${di.appConfig.profile} 🚀")
            }
            
            get("/health") {
                call.respondText("""
                    {
                        "status": "UP",
                        "profile": "${di.appConfig.profile}",
                        "timestamp": "${java.time.LocalDateTime.now()}",
                        "database": {
                            "host": "${di.appConfig.database.mysql.host}",
                            "name": "${di.appConfig.database.mysql.name}"
                        }
                    }
                """.trimIndent(), 
                contentType = io.ktor.http.ContentType.Application.Json)
            }
            
            get("/test/config") {
                val config = di.appConfig
                val ncpInfo = if (config.ncp != null) {
                    """
                    - NCP Region: ${config.ncp.region}
                    - Object Storage: ${config.ncp.objectStorage.endpoint}
                    - Bucket: ${config.ncp.objectStorage.bucketName}
                    """
                } else {
                    "- NCP 설정 없음 (local 환경)"
                }
                
                call.respondText("""
                    📋 현재 설정 정보:
                    - Profile: ${config.profile}
                    - 서버 포트: ${config.server.port}
                    - 네이버 뉴스 API: ${config.externalApi.naver.news.baseUrl}
                    - 데이터베이스: ${config.database.mysql.host}:${config.database.mysql.port}/${config.database.mysql.name}
                    - 하이퍼클로바 URL: ${config.externalApi.naver.hyperclova.apiGatewayUrl}
                    $ncpInfo
                """.trimIndent())
            }
            
            get("/test/naver-news") {
                try {
                    val news = di.naverNewsClient.searchNews(
                        query = null,  // 기본값 사용
                        display = 3
                    )
                    val responses = news.map { NewsData.from(it) }
                    call.respond(responses)
                } catch (e: Exception) {
                    call.respondText("Error: ${e.message}")
                }
            }
            
            get("/test/hyperclova") {
                try {
                    // 테스트용 더미 뉴스
                    val testNews = domain.news.entity.NewsArticle.create(
                        title = "테스트 뉴스",
                        content = "이것은 하이퍼클로바 API 테스트를 위한 샘플 뉴스입니다. 네이버의 하이퍼클로바가 이 내용을 자연스러운 음성 스크립트로 변환할 예정입니다.",
                        url = "https://test.com",
                        publishedAt = "2025-01-01"
                    )
                    
                    val script = di.naverHyperclovaClient.generateNewsScript(testNews)
                    call.respondText("생성된 스크립트:\n\n$script")
                } catch (e: Exception) {
                    call.respondText("Error: ${e.message}")
                }
            }
            
            // 뉴스 라우팅 추가
            newsRouting(di.generateNewsWithScriptUseCase)
        }
        
        // 애플리케이션 종료 시 리소스 정리
        monitor.subscribe(ApplicationStopped) {
            di.cleanup()
        }
        
        println("Application started successfully on profile: $profile")
    }.start(wait = true)
}

fun Application.module() {
    configureHTTP()
    configureMonitoring()
    configureSerialization()
}
