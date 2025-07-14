package application.usecase

import domain.news.entity.NewsArticle
import domain.news.entity.NewsScript
import domain.tts.entity.Speech
import domain.tts.service.TTSService
import infrastructure.external.NaverNewsClient
import infrastructure.external.NaverHyperclovaClient

class GenerateNewsWithAudioUseCase(
    private val naverNewsClient: NaverNewsClient,
    private val naverHyperclovaClient: NaverHyperclovaClient,
    private val ttsService: TTSService
) {
    
    suspend fun executeRandom(speaker: String = "nara"): NewsWithAudioResult {
        return try {
            // 1. 네이버 뉴스 API에서 최신 뉴스 가져오기
            val newsArticles = naverNewsClient.searchNews(
                display = 20,
                sort = "date"
            )
            
            if (newsArticles.isEmpty()) {
                return NewsWithAudioResult.failure("뉴스를 찾을 수 없습니다.")
            }
            
            // 2. 랜덤으로 하나 선택
            val selectedNews = newsArticles.random()
            
            // 3. 하이퍼클로바로 스크립트 생성
            val scriptContent = naverHyperclovaClient.generateNewsScript(selectedNews)
            
            // 4. NewsScript 엔티티 생성
            val newsScript = NewsScript.create(
                newsId = selectedNews.id,
                script = scriptContent
            )
            
            // 5. TTS 생성
            val speech = ttsService.generateSpeechFromScript(
                scriptId = newsScript.id,
                scriptText = scriptContent,
                speaker = speaker
            )
            
            NewsWithAudioResult.success(
                news = selectedNews,
                script = newsScript,
                speech = speech,
                message = "랜덤 뉴스 오디오 생성 완료"
            )
            
        } catch (e: Exception) {
            NewsWithAudioResult.failure("랜덤 뉴스 오디오 생성 실패: ${e.message}")
        }
    }
    
    suspend fun executeByKeyword(keyword: String, speaker: String = "nara"): NewsWithAudioResult {
        return try {
            // 1. 특정 키워드로 뉴스 검색
            val newsArticles = naverNewsClient.searchNews(
                query = keyword,
                display = 20,
                sort = "date"
            )
            
            if (newsArticles.isEmpty()) {
                return NewsWithAudioResult.failure("'$keyword' 관련 뉴스를 찾을 수 없습니다.")
            }
            
            // 2. 최신 뉴스 중 랜덤 선택
            val latestNews = newsArticles.random()
            
            // 3. 스크립트 생성
            val scriptContent = naverHyperclovaClient.generateNewsScript(latestNews)
            
            val newsScript = NewsScript.create(
                newsId = latestNews.id,
                script = scriptContent
            )
            
            // 4. TTS 생성
            val speech = ttsService.generateSpeechFromScript(
                scriptId = newsScript.id,
                scriptText = scriptContent,
                speaker = speaker
            )
            
            NewsWithAudioResult.success(
                news = latestNews,
                script = newsScript,
                speech = speech,
                message = "'$keyword' 관련 뉴스 오디오 생성 완료"
            )
            
        } catch (e: Exception) {
            NewsWithAudioResult.failure("키워드 뉴스 오디오 생성 실패: ${e.message}")
        }
    }
}

data class NewsWithAudioResult(
    val success: Boolean,
    val news: NewsArticle?,
    val script: NewsScript?,
    val speech: Speech?,
    val message: String,
    val error: String? = null
) {
    companion object {
        fun success(
            news: NewsArticle, 
            script: NewsScript, 
            speech: Speech, 
            message: String
        ): NewsWithAudioResult {
            return NewsWithAudioResult(
                success = true,
                news = news,
                script = script,
                speech = speech,
                message = message
            )
        }
        
        fun failure(error: String): NewsWithAudioResult {
            return NewsWithAudioResult(
                success = false,
                news = null,
                script = null,
                speech = null,
                message = "실패",
                error = error
            )
        }
    }
}
