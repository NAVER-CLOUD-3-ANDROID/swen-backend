package domain.tts.service

import domain.tts.entity.Speech
import domain.tts.entity.SpeechRequest
import domain.tts.repository.SpeechRepository
import infrastructure.external.NaverClovaDubbingClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class TTSService(
    private val clovaDubbingClient: NaverClovaDubbingClient,
    private val speechRepository: SpeechRepository
) {
    private val audioDir = "audio" // 오디오 파일 저장 디렉토리
    
    init {
        // 오디오 디렉토리 생성
        val dir = File(audioDir)
        if (!dir.exists()) {
            dir.mkdirs()
        }
    }
    
    /**
     * 스크립트로부터 TTS 생성
     */
    suspend fun generateSpeechFromScript(
        scriptId: String,
        scriptText: String,
        speaker: String = "nara"
    ): Speech {
        try {
            // 1. Speech 엔티티 생성 및 저장
            var speech = Speech.create(scriptId, speaker)
            speech = speechRepository.save(speech)
            
            // 2. 클로바 더빙 API 호출
            val request = SpeechRequest.fromScript(scriptText, speaker)
            val response = clovaDubbingClient.createSpeech(request)
            
            // 3. 요청 ID 업데이트
            speech = speech.copy(requestId = response.requestId)
            speech = speechRepository.update(speech)
            
            // 4. 생성 완료까지 대기
            val statusResponse = clovaDubbingClient.waitForCompletion(response.requestId)
            
            if (statusResponse?.downloadUrl != null) {
                // 5. 오디오 파일 다운로드 및 저장
                val audioData = clovaDubbingClient.downloadSpeech(statusResponse.downloadUrl)
                val audioFileName = "${speech.id}.mp3"
                val audioFilePath = saveAudioFile(audioData, audioFileName)
                
                // 6. Speech 완료 상태 업데이트
                speech = speech.withCompleted(statusResponse.downloadUrl, audioFilePath)
                return speechRepository.update(speech)
            } else {
                throw Exception("다운로드 URL을 받지 못했습니다.")
            }
            
        } catch (e: Exception) {
            // 실패 상태 업데이트
            val speech = speechRepository.findByScriptId(scriptId)
            if (speech != null) {
                val failedSpeech = speech.withFailed(e.message ?: "알 수 없는 오류")
                speechRepository.update(failedSpeech)
                throw e
            }
            throw e
        }
    }
    
    /**
     * 스크립트 ID로 기존 Speech 조회
     */
    suspend fun findSpeechByScriptId(scriptId: String): Speech? {
        return speechRepository.findByScriptId(scriptId)
    }
    
    /**
     * Speech ID로 조회
     */
    suspend fun findSpeechById(speechId: String): Speech? {
        return speechRepository.findById(speechId)
    }
    
    /**
     * 오디오 파일 로드
     */
    suspend fun getAudioFile(speechId: String): ByteArray? {
        val speech = speechRepository.findById(speechId) ?: return null
        if (speech.audioUrl == null) return null
        
        return withContext(Dispatchers.IO) {
            try {
                Files.readAllBytes(Paths.get(speech.audioUrl))
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * 오디오 파일 저장
     */
    private suspend fun saveAudioFile(audioData: ByteArray, fileName: String): String {
        return withContext(Dispatchers.IO) {
            val filePath = "$audioDir/$fileName"
            Files.write(Paths.get(filePath), audioData)
            filePath
        }
    }
}
