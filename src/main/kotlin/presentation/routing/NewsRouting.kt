package presentation.routing

import application.usecase.CollectNewsUseCase
import application.usecase.GenerateScriptUseCase
import application.usecase.GetRandomNewsUseCase
import presentation.dto.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*

fun Route.newsRouting(
    collectNewsUseCase: CollectNewsUseCase,
    generateScriptUseCase: GenerateScriptUseCase,
    getRandomNewsUseCase: GetRandomNewsUseCase
) {
    route("/api/news") {
        
        // 뉴스 수집 API
        post("/collect") {
            try {
                val request = call.receive<CollectNewsRequest>()
                val articles = collectNewsUseCase.execute(request.query, request.count)
                val responses = articles.map { NewsResponse.from(it) }
                call.respond(HttpStatusCode.OK, responses)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
        
        // 랜덤 뉴스 조회 API
        get("/random") {
            try {
                val result = getRandomNewsUseCase.execute()
                if (result != null) {
                    val response = RandomNewsResponse(
                        news = NewsResponse.from(result.article),
                        script = result.script?.let { NewsScriptResponse.from(it) }
                    )
                    call.respond(HttpStatusCode.OK, response)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "뉴스를 찾을 수 없습니다"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
        
        // 스크립트 생성 API
        post("/script/generate") {
            try {
                val request = call.receive<GenerateScriptRequest>()
                val script = generateScriptUseCase.execute(request.newsId)
                if (script != null) {
                    val response = NewsScriptResponse.from(script)
                    call.respond(HttpStatusCode.OK, response)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "뉴스를 찾을 수 없습니다"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
        
        // 랜덤 뉴스용 스크립트 생성 API
        post("/script/generate-random") {
            try {
                val result = generateScriptUseCase.executeForRandomNews()
                if (result != null) {
                    val (title, script) = result
                    val response = mapOf(
                        "title" to title,
                        "script" to NewsScriptResponse.from(script)
                    )
                    call.respond(HttpStatusCode.OK, response)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "뉴스를 찾을 수 없습니다"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
}
