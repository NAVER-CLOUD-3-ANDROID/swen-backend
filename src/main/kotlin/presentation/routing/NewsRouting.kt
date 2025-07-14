package presentation.routing

import application.usecase.GenerateNewsWithScriptUseCase
import application.usecase.GenerateNewsWithAudioUseCase
import application.usecase.NewsWithScriptResult
import application.usecase.NewsWithAudioResult
import domain.tts.service.TTSService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import presentation.dto.*
import kotlin.system.measureTimeMillis

fun Route.newsRouting(
    generateNewsWithScriptUseCase: GenerateNewsWithScriptUseCase,
    generateNewsWithAudioUseCase: GenerateNewsWithAudioUseCase,
    ttsService: TTSService
) {
    route("/api/news") {
        
        // 랜덤 뉴스 + 스크립트 생성 (TTS 옵션)
        get("/random-with-script") {
            val includeAudio = call.request.queryParameters["includeAudio"]?.toBoolean() ?: false
            val speaker = call.request.queryParameters["speaker"] ?: "nara"
            
            if (includeAudio) {
                // TTS 포함 생성
                var result: NewsWithAudioResult? = null
                val processingTime = measureTimeMillis {
                    result = generateNewsWithAudioUseCase.executeRandom(speaker)
                }
                
                result?.let { res ->
                    if (res.success && res.news != null && res.script != null && res.speech != null) {
                        call.respond(HttpStatusCode.OK, NewsWithAudioResponse(
                            success = true,
                            message = res.message,
                            data = NewsWithAudioData(
                                news = NewsData.from(res.news),
                                script = NewsScriptData.from(res.script),
                                audio = SpeechData.from(res.speech),
                                processingTime = processingTime
                            )
                        ))
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, NewsWithAudioResponse(
                            success = false,
                            message = "뉴스 오디오 생성 실패",
                            error = res.error
                        ))
                    }
                }
            } else {
                // 기존 스크립트만 생성
                var result: NewsWithScriptResult? = null
                val processingTime = measureTimeMillis {
                    result = generateNewsWithScriptUseCase.executeRandom()
                }
                
                result?.let { res ->
                    if (res.success && res.news != null && res.script != null) {
                        call.respond(HttpStatusCode.OK, NewsResponse(
                            success = true,
                            message = res.message,
                            data = NewsWithScriptData(
                                news = NewsData.from(res.news),
                                script = NewsScriptData.from(res.script),
                                processingTime = processingTime
                            )
                        ))
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, NewsResponse(
                            success = false,
                            message = "뉴스 스크립트 생성 실패",
                            error = res.error
                        ))
                    }
                }
            }
        }
        
        // 키워드 기반 뉴스 + 스크립트 생성 (TTS 옵션)
        post("/keyword-with-script") {
            try {
                val request = call.receive<KeywordNewsRequest>()
                
                if (request.keyword.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, NewsResponse(
                        success = false,
                        message = "키워드를 입력해주세요.",
                        error = "키워드가 비어있습니다."
                    ))
                    return@post
                }
                
                if (request.includeAudio) {
                    // TTS 포함 생성
                    var result: NewsWithAudioResult? = null
                    val processingTime = measureTimeMillis {
                        result = generateNewsWithAudioUseCase.executeByKeyword(request.keyword, request.speaker)
                    }
                    
                    result?.let { res ->
                        if (res.success && res.news != null && res.script != null && res.speech != null) {
                            call.respond(HttpStatusCode.OK, NewsWithAudioResponse(
                                success = true,
                                message = res.message,
                                data = NewsWithAudioData(
                                    news = NewsData.from(res.news),
                                    script = NewsScriptData.from(res.script),
                                    audio = SpeechData.from(res.speech),
                                    processingTime = processingTime
                                )
                            ))
                        } else {
                            call.respond(HttpStatusCode.NotFound, NewsWithAudioResponse(
                                success = false,
                                message = "키워드 뉴스 오디오 생성 실패",
                                error = res.error
                            ))
                        }
                    }
                } else {
                    // 기존 스크립트만 생성
                    var result: NewsWithScriptResult? = null
                    val processingTime = measureTimeMillis {
                        result = generateNewsWithScriptUseCase.executeByKeyword(request.keyword)
                    }
                    
                    result?.let { res ->
                        if (res.success && res.news != null && res.script != null) {
                            call.respond(HttpStatusCode.OK, NewsResponse(
                                success = true,
                                message = res.message,
                                data = NewsWithScriptData(
                                    news = NewsData.from(res.news),
                                    script = NewsScriptData.from(res.script),
                                    processingTime = processingTime
                                )
                            ))
                        } else {
                            call.respond(HttpStatusCode.NotFound, NewsResponse(
                                success = false,
                                message = "키워드 뉴스 생성 실패",
                                error = res.error
                            ))
                        }
                    }
                }
                
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, NewsResponse(
                    success = false,
                    message = "서버 오류",
                    error = e.message
                ))
            }
        }
        
        // 오디오 파일 스트리밍
        get("/audio/{speechId}") {
            try {
                val speechId = call.parameters["speechId"]!!
                val audioData = ttsService.getAudioFile(speechId)
                
                if (audioData != null) {
                    call.response.header("Content-Disposition", "inline; filename=\"$speechId.mp3\"")
                    call.respondBytes(
                        audioData, 
                        ContentType.Audio.MPEG,
                        HttpStatusCode.OK
                    )
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf(
                        "success" to false,
                        "message" to "오디오 파일을 찾을 수 없습니다."
                    ))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf(
                    "success" to false,
                    "message" to "오디오 파일 조회 실패",
                    "error" to e.message
                ))
            }
        }
    }
}
