# News Audio App - Backend

네이버 클라우드 교육 프로젝트: 뉴스를 음성으로 읽어주는 앱의 백엔드 서버

## 🏗️ 아키텍처

DDD(Domain Driven Design) 구조 + Profile 기반 환경 분리

```
src/main/kotlin/
├── Application.kt                # 메인 애플리케이션
├── domain/                       # 도메인 레이어
├── infrastructure/               # 인프라 레이어
├── application/                  # 애플리케이션 레이어
└── presentation/                 # 프레젠테이션 레이어

src/main/resources/
├── application.yaml              # 기본 설정
├── application-local.yaml        # 로컬 환경
└── application-ncp.yaml          # NCP 운영 환경
```

## 🔧 기술 스택

- **Framework**: Ktor
- **Language**: Kotlin  
- **Database**: MySQL + Exposed
- **External APIs**: 네이버 뉴스 API, 네이버 하이퍼클로바 API
- **Cloud**: Naver Cloud Platform (NCP)
- **Configuration**: Profile 기반 환경 분리

## 🌍 환경별 설정

### Local (로컬 개발)
- 로컬 MySQL 사용
- 디버그 로깅 활성화
- 개발용 API 키

### NCP (운영 환경)
- NCP Cloud DB for MySQL
- NCP Object Storage (음성 파일)
- NCP Cloud Functions 연동
- 운영 최적화 설정

## 🚀 실행 방법

### 1. 환경변수 설정
```bash
# .env 파일에 각 환경별 설정 입력
APP_PROFILE=local  # local, ncp 중 선택
```

### 2. 환경별 실행
```bash
# 로컬 환경
./scripts/run-local.sh

# NCP 운영 환경
./scripts/run-ncp.sh
```

### 3. 헬스체크 & 설정 확인
- **헬스체크**: http://localhost:8080/health
- **설정 확인**: http://localhost:8080/test/config
- **API 테스트**: http://localhost:8080/test/naver-news

## 📡 API 엔드포인트

### 시스템
- `GET /health` - 서버 상태 및 환경 정보
- `GET /test/config` - 현재 설정 확인

### 테스트
- `GET /test/naver-news` - 네이버 뉴스 API 테스트
- `GET /test/hyperclova` - 하이퍼클로바 스크립트 생성 테스트

## 🔑 환경변수 가이드

- 노션 참고

## 🚀 NCP 배포 준비사항

1. **NCP Cloud DB for MySQL** 생성
2. **NCP Object Storage** 버킷 생성  
3. **NCP VPC** 및 보안그룹 설정
4. **환경변수** NCP 운영값으로 설정
5. **APP_PROFILE=ncp**로 배포

## 🔧 다음 단계

1. **MySQL 데이터베이스 연결**
2. **NCP 환경 구축** 
3. **Repository 구현체 작성**
4. **Vector DB 연동** (RAG 구현)
5. **NCP Object Storage 연동** (음성 파일 저장)

## 🤝 팀원별 작업

- **한진**: 뉴스 수집, 스크립트 생성, DDD 구조, TTS & Dubbing 연결
- **종서**: TTS & Dubbing 연결
- **민혁**: 로그인, 스크랩, 보안 Router  
- **유진**: NCP 인프라, 화면 구축
