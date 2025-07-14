package presentation.routing

import application.usecase.GenerateNewsWithScriptUseCase
import application.usecase.NewsWithScriptResult
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import presentation.dto.*
import kotlin.system.measureTimeMillis

fun Route.newsRouting(
    generateNewsWithScriptUseCase: GenerateNewsWithScriptUseCase
) {
    route("/api/news") {
        
        // 랜덤 뉴스 + 스크립트 생성
        get("/random-with-script") {
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
        
        // 키워드 기반 뉴스 + 스크립트 생성
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
                
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, NewsResponse(
                    success = false,
                    message = "서버 오류",
                    error = e.message
                ))
            }
        }
    }
}
