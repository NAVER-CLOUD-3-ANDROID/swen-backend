package infrastructure.config

import io.ktor.server.application.*

data class AppConfig(
    val profile: String,
    val server: ServerConfig,
    val externalApi: ExternalApiConfig,
    val database: DatabaseConfig,
    val ncp: NCPConfig? = null
) {
    companion object {
        fun from(application: Application): AppConfig {
            val config = application.environment.config
            val profile = config.propertyOrNull("app.profile")?.getString() ?: "local"
            
            // profile 유효성 검사
            if (profile !in listOf("local", "ncp")) {
                throw IllegalArgumentException("Invalid profile: $profile. Only 'local' and 'ncp' are supported.")
            }
            
            return AppConfig(
                profile = profile,
                server = ServerConfig(
                    port = config.propertyOrNull("ktor.deployment.port")?.getString()?.toInt() ?: 8080
                ),
                externalApi = ExternalApiConfig(
                    naver = NaverApiConfig(
                        news = NaverNewsConfig(
                            clientId = config.property("external-api.naver.news.client-id").getString(),
                            clientSecret = config.property("external-api.naver.news.client-secret").getString(),
                            baseUrl = config.property("external-api.naver.news.base-url").getString()
                        ),
                        hyperclova = NaverHyperclovaConfig(
                            apiKey = config.property("external-api.naver.hyperclova.api-key").getString(),
                            apiKeyPrimary = config.property("external-api.naver.hyperclova.api-key-primary").getString(),
                            apiGatewayUrl = config.property("external-api.naver.hyperclova.api-gateway-url").getString(),
                            requestId = config.property("external-api.naver.hyperclova.request-id").getString()
                        )
                    )
                ),
                database = DatabaseConfig(
                    mysql = MySQLConfig(
                        host = config.property("database.mysql.host").getString(),
                        port = config.property("database.mysql.port").getString().toInt(),
                        name = config.property("database.mysql.name").getString(),
                        username = config.property("database.mysql.username").getString(),
                        password = config.property("database.mysql.password").getString()
                    ),
                    hikari = HikariConfig(
                        maximumPoolSize = config.property("database.hikari.maximum-pool-size").getString().toInt(),
                        minimumIdle = config.property("database.hikari.minimum-idle").getString().toInt(),
                        connectionTimeout = config.property("database.hikari.connection-timeout").getString().toLong(),
                        idleTimeout = config.property("database.hikari.idle-timeout").getString().toLong(),
                        maxLifetime = config.property("database.hikari.max-lifetime").getString().toLong()
                    )
                ),
                ncp = if (profile == "ncp") {
                    NCPConfig(
                        region = config.propertyOrNull("ncp.region")?.getString() ?: "KR",
                        objectStorage = NCPObjectStorageConfig(
                            endpoint = config.property("ncp.object-storage.endpoint").getString(),
                            accessKey = config.property("ncp.object-storage.access-key").getString(),
                            secretKey = config.property("ncp.object-storage.secret-key").getString(),
                            bucketName = config.property("ncp.object-storage.bucket-name").getString()
                        ),
                        cloudFunctions = config.propertyOrNull("ncp.cloud-functions.endpoint")?.getString()?.let {
                            NCPCloudFunctionsConfig(endpoint = it)
                        },
                        vpc = NCPVPCConfig(
                            privateSubnet = config.propertyOrNull("ncp.vpc.private-subnet")?.getString(),
                            securityGroup = config.propertyOrNull("ncp.vpc.security-group")?.getString()
                        )
                    )
                } else null
            )
        }
    }
}

data class ServerConfig(
    val port: Int
)

data class ExternalApiConfig(
    val naver: NaverApiConfig
)

data class NaverApiConfig(
    val news: NaverNewsConfig,
    val hyperclova: NaverHyperclovaConfig
)

data class NaverNewsConfig(
    val clientId: String,
    val clientSecret: String,
    val baseUrl: String
)

data class NaverHyperclovaConfig(
    val apiKey: String,
    val apiKeyPrimary: String,
    val apiGatewayUrl: String,
    val requestId: String
)

data class DatabaseConfig(
    val mysql: MySQLConfig,
    val hikari: HikariConfig
)

data class MySQLConfig(
    val host: String,
    val port: Int,
    val name: String,
    val username: String,
    val password: String
) {
    val jdbcUrl: String
        get() = "jdbc:mysql://$host:$port/$name?useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Seoul"
}

data class HikariConfig(
    val maximumPoolSize: Int,
    val minimumIdle: Int,
    val connectionTimeout: Long,
    val idleTimeout: Long,
    val maxLifetime: Long
)

// NCP 전용 설정
data class NCPConfig(
    val region: String,
    val objectStorage: NCPObjectStorageConfig,
    val cloudFunctions: NCPCloudFunctionsConfig? = null,
    val vpc: NCPVPCConfig
)

data class NCPObjectStorageConfig(
    val endpoint: String,
    val accessKey: String,
    val secretKey: String,
    val bucketName: String
)

data class NCPCloudFunctionsConfig(
    val endpoint: String
)

data class NCPVPCConfig(
    val privateSubnet: String?,
    val securityGroup: String?
)
