# 🎬 워크플로우 및 다이어그램

뉴스 스크립트 TTS 생성 서비스의 상세 워크플로우와 시각화 자료

## 🎯 서비스 워크플로우

```mermaid
graph LR
    A[🔍 뉴스 수집] --> B[📝 스크립트 생성]
    B --> C[🎵 TTS 변환]
    C --> D[🎧 오디오 재생]
    
    A1[네이버 뉴스 API] --> A
    B1[하이퍼클로바 AI] --> B  
    C1[클로바 더빙 API] --> C
    D1[웹 오디오 플레이어] --> D
```

## 🏗️ 시스템 아키텍처

```mermaid
graph TB
    subgraph "🎯 Presentation Layer"
        API[REST API]
        DTO[Request/Response DTO]
    end
    
    subgraph "⚡ Application Layer"
        UC1[GenerateNewsWithScriptUseCase]
        UC2[GenerateNewsWithAudioUseCase]
    end
    
    subgraph "🏛️ Domain Layer"
        subgraph "News Domain"
            NE[NewsArticle Entity]
            NS[NewsScript Entity]
            NR[NewsRepository]
        end
        subgraph "TTS Domain"
            SE[Speech Entity]
            SR[SpeechRequest Entity]
            TR[SpeechRepository]
            TS[TTSService]
        end
    end
    
    subgraph "🔧 Infrastructure Layer"
        subgraph "External APIs"
            NC[NaverNewsClient]
            HC[HyperclovaClient]
            CD[ClovaDubbingClient]
        end
        subgraph "Database"
            DB[(MySQL)]
        end
    end
    
    API --> UC1
    API --> UC2
    UC1 --> NE
    UC1 --> NS
    UC2 --> SE
    UC2 --> TS
    TS --> CD
    UC1 --> NC
    UC1 --> HC
    UC2 --> NC
    UC2 --> HC
    NR --> DB
    TR --> DB
```

## 🔗 API 플로우

```mermaid
sequenceDiagram
    participant User
    participant API
    participant NewsService
    participant HyperClova
    participant ClovaDubbing
    participant Database
    
    User->>+API: GET /api/news/random-with-script?includeAudio=true
    
    API->>+NewsService: searchNews()
    NewsService->>-API: NewsArticle[]
    
    API->>+HyperClova: generateScript(news)
    HyperClova->>-API: Generated Script
    
    API->>+ClovaDubbing: createSpeech(script)
    ClovaDubbing->>API: Request ID
    
    loop TTS Processing
        API->>ClovaDubbing: checkStatus(requestId)
        ClovaDubbing->>API: Status Response
    end
    
    ClovaDubbing->>API: Download URL
    API->>ClovaDubbing: downloadAudio()
    ClovaDubbing->>API: Audio Data
    
    API->>+Database: saveSpeech()
    Database->>-API: Saved
    
    API->>-User: NewsWithAudioResponse
    
    User->>API: GET /api/news/audio/{speechId}
    API->>User: Audio Stream
```

## 🔄 사용자 시나리오

```mermaid
journey
    title 뉴스 방송 생성 여정
    section 사용자 접속
      메인화면 로딩: 3: User
      뉴스+스크립트+TTS 생성: 4: System
      모든 준비 완료: 5: System
    section 즉시 재생
      플레이 버튼 클릭: 5: User
      음성 즉시 재생: 5: System
      방송 청취: 5: User
    section 맞춤 검색
      키워드 입력: 4: User
      맞춤 뉴스 생성: 4: System
      개인화된 방송: 5: User
```

## 📊 데이터베이스 ERD

```mermaid
erDiagram
    NEWS_ARTICLES {
        string id PK
        string title
        text content
        string url
        text summary
        string published_at
        string image_url
        datetime created_at
        datetime updated_at
    }
    
    NEWS_SCRIPTS {
        string id PK
        string news_id FK
        text script
        string audio_url
        int duration
        datetime created_at
        datetime updated_at
    }
    
    SPEECHES {
        string id PK
        string script_id FK
        string request_id
        text audio_url
        text download_url
        string status
        string speaker
        text error_message
        datetime created_at
        datetime updated_at
    }
    
    NEWS_ARTICLES ||--o{ NEWS_SCRIPTS : "1:N"
    NEWS_SCRIPTS ||--o| SPEECHES : "1:1"
```

## 🌐 배포 아키텍처

```mermaid
graph TB
    subgraph "🖥️ Development"
        DEV[Local Environment]
        DEV_DB[(Local MySQL)]
        DEV --> DEV_DB
    end
    
    subgraph "☁️ NCP Production"
        subgraph "Compute"
            SERVER[NCP Server]
            APP[Ktor Application]
            SERVER --> APP
        end
        
        subgraph "Database"
            CLOUD_DB[(NCP Cloud DB for MySQL)]
        end
        
        subgraph "Storage"
            OBJ_STORAGE[Object Storage]
            AUDIO_FILES[Audio Files]
            OBJ_STORAGE --> AUDIO_FILES
        end
        
        subgraph "AI Services"
            NEWS_API[네이버 뉴스 API]
            HYPERCLOVA[하이퍼클로바 AI]
            CLOVA_DUBBING[클로바 더빙]
        end
        
        APP --> CLOUD_DB
        APP --> OBJ_STORAGE
        APP --> NEWS_API
        APP --> HYPERCLOVA
        APP --> CLOVA_DUBBING
    end
    
    DEV -.->|Deploy| SERVER
```

## 🚀 CI/CD 파이프라인

```mermaid
graph LR
    subgraph "🔄 Development Cycle"
        A[Code Push] --> B[GitHub Actions]
        B --> C[Build & Test]
        C --> D[Deploy to NCP]
        D --> E[Health Check]
        E --> F[Service Ready]
    end
    
    subgraph "📋 Quality Gates"
        G[Unit Tests]
        H[Integration Tests]
        I[Code Quality]
        C --> G
        C --> H
        C --> I
    end
    
    subgraph "🚨 Monitoring"
        J[Application Logs]
        K[Performance Metrics]
        L[Error Tracking]
        F --> J
        F --> K
        F --> L
    end
```

## 💡 서비스 혁신 포인트

```mermaid
mindmap
  root((뉴스 TTS 서비스))
    완전 자동화
      뉴스 수집
      스크립트 생성
      음성 변환
      즉시 재생
    AI 기술 활용
      네이버 하이퍼클로바
      클로바 더빙
      자연스러운 음성
      감정 표현
    사용자 경험
      실시간 처리
      맞춤형 컨텐츠
      다양한 음성 옵션
      모바일 최적화
    확장성
      Clean Architecture
      마이크로서비스 준비
      클라우드 네이티브
      API 중심 설계
```

---

*이 문서의 모든 다이어그램은 Mermaid 문법으로 작성되어 GitHub에서 바로 확인할 수 있습니다! 🎯*
