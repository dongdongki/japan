# Claude 수칙

## 프로젝트 개요
- **프로젝트명**: 일본어 학습 앱 (Japanese Study App)
- **유형**: Android 모바일 앱
- **언어**: Kotlin
- **아키텍처**: MVVM (Model-View-ViewModel)
- **현재 단계**: 유지보수

---

## 작업 규칙

### 이슈 등록 규칙
- GitHub Issues를 사용하여 이슈 관리
- 이슈 제목은 명확하고 간결하게 작성
- 이슈 본문에 재현 단계, 예상 동작, 실제 동작 포함

### 이슈 템플릿
```markdown
## 설명
[이슈에 대한 간단한 설명]

## 재현 단계
1. [첫 번째 단계]
2. [두 번째 단계]
3. [...]

## 예상 동작
[예상되는 동작]

## 실제 동작
[실제 발생한 동작]

## 스크린샷 (선택)
[관련 스크린샷]

## 환경
- 기기: [예: Galaxy S21]
- Android 버전: [예: Android 13]
- 앱 버전: [예: 1.0.0]
```

### 브랜치/커밋/PR 규칙 요약
- **브랜치 전략**: Git Flow (main, dev, feature/*, bugfix/*, hotfix/*)
- **기본 브랜치**: main
- **커밋 스타일**: Conventional Commits (제목 영어, 본문 한국어 혼용)
- 자세한 내용은 [branch-naming.md](branch-naming.md) 및 [commit-conventions.md](commit-conventions.md) 참조

---

## 빌드 설정

### 기본 빌드 명령어
```bash
# Debug 빌드
./gradlew assembleDebug

# Release 빌드
./gradlew assembleRelease

# 테스트 실행
./gradlew test
./gradlew connectedAndroidTest
```

### 빌드 설정
- **compileSdk**: 34
- **minSdk**: 26
- **targetSdk**: 34
- **Kotlin JVM Target**: 1.8

### 프로젝트 의존성
- **UI**: ViewBinding, Material Design 3
- **네비게이션**: Jetpack Navigation
- **DI**: Hilt (Dagger)
- **네트워크**: Retrofit, OkHttp (OpenAI API)
- **비동기**: Coroutines
- **직렬화**: Gson

---

## Kotlin 코드 생성 필수 규칙 (CRITICAL)

### 1. Null 안전성
- nullable 타입 최소화
- `!!` 연산자 사용 금지 (반드시 safe call `?.` 또는 Elvis `?:` 사용)
- 외부 데이터 파싱 시 null 체크 필수

```kotlin
// Bad
val name = user!!.name

// Good
val name = user?.name ?: "Unknown"
```

### 2. 불변성
- `val` 선호, `var` 최소화
- `data class` 활용
- 컬렉션 변경 시 새 인스턴스 생성

```kotlin
// Bad
var list = mutableListOf<String>()

// Good
val list = listOf<String>()
```

### 3. 코루틴 사용
- 적절한 Dispatcher 사용 (IO, Main, Default)
- 구조화된 동시성 (viewModelScope, lifecycleScope)
- 예외 처리 필수

```kotlin
// Good
viewModelScope.launch {
    try {
        val result = withContext(Dispatchers.IO) {
            repository.fetchData()
        }
        _uiState.value = result
    } catch (e: Exception) {
        _error.value = e.message
    }
}
```

### 4. 자원 관리
- `use` 블록으로 자원 자동 해제
- Context 누수 방지 (Activity 대신 Application Context 사용)

---

## 코드 품질 원칙 (필수 준수)

### Dead Code 제거
- 사용되지 않는 코드, 파일, 리소스 즉시 삭제
- 주석 처리된 코드 금지

### 중복 코드 금지
- DRY (Don't Repeat Yourself) 원칙 준수
- 공통 로직은 함수/클래스로 추출

### 공통 함수화
- 3회 이상 반복되는 로직은 함수로 추출
- util 패키지에 유틸리티 함수 정리

### 적절한 객체화
- 관련 데이터는 data class로 그룹화
- 단일 책임 원칙 (SRP) 준수

---

## UI/UX 규칙

### 다크 모드 필수
- **텍스트**: 모든 텍스트는 흰색 (`#FFFFFF`)
- **배경**: 검정색 또는 어두운 색상 팔레트
- Material Design 3 가이드라인 준수

### 네비게이션
- Jetpack Navigation 사용
- Single Activity + Multiple Fragments 패턴
- Back Stack 관리 철저

---

## 프로젝트 구조

```
app/src/main/java/com/example/myapplication/
├── di/                 # Hilt 의존성 주입 모듈
├── model/              # 데이터 모델 (data class)
├── repository/         # 데이터 저장소 (Repository 패턴)
├── service/            # 외부 서비스 (TTS, OpenAI)
├── ui/                 # UI 컴포넌트 (Fragment, ViewModel, Adapter)
│   └── theme/          # 테마 관련
├── util/               # 유틸리티 클래스
├── MainActivity.kt
└── JapaneseStudyApp.kt # Application 클래스
```

---

## 관련 문서
- [branch-naming.md](branch-naming.md) - 브랜치 명명 규칙
- [commit-conventions.md](commit-conventions.md) - 커밋 메시지 규칙
- [project-rules.md](project-rules.md) - 프로젝트 규칙 상세
- [plan.md](plan.md) - 프로젝트 계획서
