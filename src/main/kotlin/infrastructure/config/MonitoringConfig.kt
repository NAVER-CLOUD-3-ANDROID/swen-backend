package infrastructure.config

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import org.slf4j.event.*

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> 
            // 헬스체크나 정적 파일은 로깅에서 제외
            !call.request.path().startsWith("/health") &&
            !call.request.path().startsWith("/static")
        }
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val userAgent = call.request.headers["User-Agent"]
            "$httpMethod ${call.request.path()} - $status - $userAgent"
        }
    }
}
