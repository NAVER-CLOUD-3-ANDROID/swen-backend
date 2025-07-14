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
            
            get("/test/tts") {
                try {
                    // 테스트용 스크립트
                    val testScript = "안녕하세요. 이것은 네이버 클로바 더빙 API 테스트입니다. 음성이 정상적으로 생성되는지 확인해보겠습니다."
                    
                    val speech = di.ttsService.generateSpeechFromScript(
                        scriptId = "test-script-001",
                        scriptText = testScript,
                        speaker = "nara"
                    )
                    
                    call.respondText("""
                        🎵 TTS 생성 완료!
                        
                        - Speech ID: ${speech.id}
                        - Script ID: ${speech.scriptId}
                        - Speaker: ${speech.speaker}
                        - Status: ${speech.status}
                        - Audio URL: ${speech.audioUrl}
                        
                        🎧 재생 테스트: GET /api/news/audio/${speech.id}
                    """.trimIndent())
                } catch (e: Exception) {
                    call.respondText("TTS 테스트 실패: ${e.message}")
                }
            }
            
            get("/test/news-with-audio") {
                try {
                    val result = di.generateNewsWithAudioUseCase.executeRandom("nara")
                    
                    if (result.success && result.speech != null) {
                        call.respondText("""
                            🎉 뉴스 + 오디오 생성 완료!
                            
                            뉴스: ${result.news?.title}
                            스크립트 ID: ${result.script?.id}
                            오디오 ID: ${result.speech.id}
                            오디오 URL: /api/news/audio/${result.speech.id}
                            
                            상태: ${result.speech.status}
                        """.trimIndent())
                    } else {
                        call.respondText("뉴스 오디오 생성 실패: ${result.error}")
                    }
                } catch (e: Exception) {
                    call.respondText("테스트 실패: ${e.message}")
                }
            }
            
            // 뉴스 라우팅 추가
            newsRouting(
                generateNewsWithScriptUseCase = di.generateNewsWithScriptUseCase,
                generateNewsWithAudioUseCase = di.generateNewsWithAudioUseCase,
                ttsService = di.ttsService
            )
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
