# Project Rules

## 프로젝트 개요

### 기본 정보
- **프로젝트명**: 일본어 학습 앱 (Japanese Study App)
- **패키지명**: com.example.myapplication
- **유형**: Android 네이티브 앱
- **현재 단계**: 유지보수

### 프로젝트 설명
히라가나/카타카나 학습, 단어 암기, 문장 학습, 퀴즈, 필기 연습 등 다양한 일본어 학습 기능을 제공하는 Android 앱

### 주요 기능
- **가나 학습**: 히라가나/카타카나 목록, 필기 연습
- **단어 학습**: 카테고리별 단어 목록, 단어 퀴즈
- **문장 학습**: 문장 목록, 문장 퀴즈
- **노래 학습**: 노래 가사를 통한 학습
- **데일리 단어**: 일일 단어 학습
- **AI 문장 생성**: OpenAI API를 통한 예문 생성
- **TTS**: 텍스트 음성 변환

---

## 기술 스택

### 언어 및 빌드
| 항목 | 버전/내용 |
|------|-----------|
| Kotlin | 1.9.22 |
| Gradle | Kotlin DSL |
| compileSdk | 34 |
| minSdk | 26 |
| targetSdk | 34 |
| JVM Target | 1.8 |

### 주요 라이브러리

#### Android Jetpack
- **Navigation**: 2.7.7 (Single Activity + Fragments)
- **ViewModel**: 2.7.0
- **LiveData**: 2.7.0
- **ViewBinding**: enabled

#### 의존성 주입
- **Hilt**: 2.50

#### 네트워크
- **Retrofit**: 2.9.0
- **OkHttp**: 4.12.0

#### 기타
- **Coroutines**: 1.7.3
- **Gson**: 2.10.1
- **Material Design**: 1.11.0

---

## 아키텍처

### MVVM 패턴
```
View (Fragment) ←→ ViewModel ←→ Repository ←→ Data Source
                      ↓
                   LiveData
```

### 레이어 책임

| 레이어 | 책임 |
|--------|------|
| **View (Fragment)** | UI 표시, 사용자 입력 처리 |
| **ViewModel** | UI 상태 관리, 비즈니스 로직 |
| **Repository** | 데이터 소스 추상화 |
| **Data Source** | 로컬 JSON, 네트워크 API |

---

## 코딩 표준

### 네이밍 규칙

#### 클래스
| 유형 | 접미사 | 예시 |
|------|--------|------|
| Fragment | Fragment | `WordListFragment` |
| ViewModel | ViewModel | `WordQuizViewModel` |
| Repository | Repository | `WordRepository` |
| Adapter | Adapter | `WordListAdapter` |
| Data Class | - | `Word`, `Sentence` |
| Service | Service | `TtsService` |

#### 파일
- **Layout**: `fragment_*.xml`, `item_*.xml`, `activity_*.xml`
- **Navigation**: `nav_graph.xml`
- **Values**: `colors.xml`, `strings.xml`, `themes.xml`

#### 변수/함수
```kotlin
// 변수: camelCase
private val wordList: List<Word>
private var currentIndex: Int

// 상수: SCREAMING_SNAKE_CASE
companion object {
    private const val MAX_RETRY_COUNT = 3
}

// 함수: camelCase, 동사로 시작
fun loadWords()
fun submitAnswer(answer: String)
private fun calculateScore(): Int
```

### 코드 스타일

#### Kotlin 컨벤션
```kotlin
// 1. 널 안전성
val name = user?.name ?: "Unknown"  // Good
val name = user!!.name              // Bad

// 2. when 사용
when (state) {
    is Loading -> showLoading()
    is Success -> showData(state.data)
    is Error -> showError(state.message)
}

// 3. 확장 함수 활용
fun String.toHiragana(): String { ... }

// 4. 스코프 함수
user?.let { updateUI(it) }
binding.apply {
    textView.text = title
    button.setOnClickListener { ... }
}
```

#### XML 레이아웃
- ConstraintLayout 우선 사용
- 하드코딩 문자열 금지 (strings.xml 사용)
- 하드코딩 색상 금지 (colors.xml 사용)
- 다크 모드: 텍스트 흰색, 배경 검정/어두운 색

---

## 브랜치/커밋/PR 규칙 상세

### 브랜치 전략: Git Flow
- **main**: 프로덕션 브랜치
- **dev**: 개발 브랜치
- **feature/***: 새 기능
- **bugfix/***: 버그 수정
- **hotfix/***: 긴급 수정

자세한 내용: [branch-naming.md](branch-naming.md)

### 커밋 컨벤션: Conventional Commits
- **Type**: feat, fix, docs, style, refactor, test, chore, perf
- **제목**: 영어, 명령형
- **본문**: 한국어 허용

자세한 내용: [commit-conventions.md](commit-conventions.md)

### Pull Request
1. dev 브랜치로 PR 생성
2. PR 제목은 커밋 컨벤션 따름
3. 변경 사항 요약 작성
4. 관련 이슈 연결
5. 셀프 리뷰 후 머지

---

## 코드 품질 체크리스트

### 작업 전
- [ ] 최신 dev 브랜치에서 새 브랜치 생성
- [ ] 이슈 번호 확인

### 작업 중
- [ ] 컴파일 에러 없음
- [ ] 기존 기능 정상 동작
- [ ] Null 안전성 확인
- [ ] 하드코딩 값 없음
- [ ] Dead Code 없음
- [ ] 중복 코드 없음

### 커밋 전
- [ ] 불필요한 주석 제거
- [ ] 불필요한 import 제거
- [ ] 로그 정리 (디버그 로그 제거)
- [ ] 커밋 메시지 컨벤션 준수

### PR 전
- [ ] 빌드 성공 확인
- [ ] 기능 테스트 완료
- [ ] PR 설명 작성

---

## 금지 사항

### 코드 금지 사항
- `!!` 연산자 사용
- `any` 타입 사용 (Gson 제외)
- 하드코딩 문자열/색상
- 주석 처리된 코드 커밋
- 무한 루프 가능성 있는 코드

### 프로젝트 금지 사항
- main 브랜치 직접 푸시
- 이슈 없이 작업 시작
- 테스트 없이 머지
- 빌드 실패 상태로 커밋

### 사용자 대응 금지
- "앱 재설치" 요청
- "캐시 삭제" 요청
- 코드 수정으로 해결할 문제를 사용자에게 전가

---

## 리소스 관리

### 색상 (다크 모드)
```xml
<!-- colors.xml -->
<color name="text_primary">#FFFFFF</color>
<color name="text_secondary">#B3FFFFFF</color>
<color name="background">#121212</color>
<color name="surface">#1E1E1E</color>
```

### 문자열
- 모든 사용자 표시 문자열은 `strings.xml`에 정의
- 한국어/일본어 지원 고려

### 이미지
- Vector Drawable 우선 사용
- 필요 시 WebP 형식 사용

---

## 참고 문서
- [CLAUDE.md](CLAUDE.md) - 메인 수칙
- [branch-naming.md](branch-naming.md) - 브랜치 규칙
- [commit-conventions.md](commit-conventions.md) - 커밋 규칙
- [plan.md](plan.md) - 프로젝트 계획서
