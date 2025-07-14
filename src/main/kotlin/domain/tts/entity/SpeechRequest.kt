package domain.tts.entity

import kotlinx.serialization.Serializable

@Serializable
data class SpeechRequest(
    val text: String,
    val speaker: String = "nara",    // 기본 음성: nara, clara, matt, danna 등
    val speed: Int = 0,              // 속도: -5 ~ 5 (0이 기본)
    val pitch: Int = 0,              // 음높이: -5 ~ 5 (0이 기본)
    val emotion: Int = 0,            // 감정: 0(평범) ~ 2(강함)
    val format: String = "mp3"       // 출력 포맷: mp3, wav
) {
    companion object {
        fun fromScript(scriptText: String, speaker: String = "nara"): SpeechRequest {
            return SpeechRequest(
                text = scriptText,
                speaker = speaker,
                speed = 0,
                pitch = 0,
                emotion = 0,
                format = "mp3"
            )
        }
    }
}
