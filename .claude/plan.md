# 프로젝트 계획서

## 프로젝트 상태: 유지보수 단계

일본어 학습 앱은 현재 유지보수 단계에 있으며, 주요 기능이 구현된 상태입니다.

---

## 현재 구현된 기능

### 1. 가나 학습
- [x] 히라가나 목록 표시
- [x] 카타카나 목록 표시
- [x] 가나 필기 연습
- [x] 가나 퀴즈

### 2. 단어 학습
- [x] 카테고리별 단어 목록
- [x] 단어 상세 보기
- [x] 단어 퀴즈 (객관식)
- [x] 약점 단어 관리

### 3. 문장 학습
- [x] 문장 목록 표시
- [x] 문장 퀴즈
- [x] 약점 문장 관리

### 4. 노래 학습
- [x] 노래 목록
- [x] 가사 표시
- [x] 노래 퀴즈

### 5. 데일리 단어
- [x] 일일 단어 목록
- [x] 데일리 단어 퀴즈
- [x] 약점 데일리 단어 관리

### 6. AI 기능
- [x] OpenAI API 연동
- [x] AI 문장 생성

### 7. 기타
- [x] TTS (텍스트 음성 변환)
- [x] 필기 연습 (WritingView)

---

## 아키텍처 개요

```
┌──────────────────────────────────────────────────┐
│                    UI Layer                       │
│  ┌─────────────┐  ┌─────────────┐  ┌──────────┐ │
│  │  Fragment   │  │  ViewModel  │  │ Adapter  │ │
│  └─────────────┘  └─────────────┘  └──────────┘ │
└──────────────────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────┐
│                  Domain Layer                     │
│  ┌─────────────────────────────────────────────┐ │
│  │                Repository                    │ │
│  │  (WordRepo, KanaRepo, SentenceRepo, etc.)   │ │
│  └─────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────┐
│                  Data Layer                       │
│  ┌──────────────┐  ┌──────────────┐             │
│  │  Local JSON  │  │  OpenAI API  │             │
│  │   (assets)   │  │  (Retrofit)  │             │
│  └──────────────┘  └──────────────┘             │
└──────────────────────────────────────────────────┘
```

---

## 유지보수 로드맵

### Phase 1: 안정화 (현재)
- [ ] 버그 수정 및 안정성 개선
- [ ] 성능 최적화
- [ ] 코드 품질 개선

### Phase 2: 개선
- [ ] UI/UX 개선
- [ ] 사용자 피드백 반영
- [ ] 접근성 개선

### Phase 3: 기능 확장 (선택)
- [ ] 오프라인 모드 강화
- [ ] 학습 통계/분석
- [ ] 추가 학습 콘텐츠

---

## 주요 파일 구조

### UI Components
```
ui/
├── HomeFragment.kt              # 홈 화면
├── KanaListFragment.kt          # 가나 목록
├── KanaWritingPracticeFragment.kt # 가나 필기 연습
├── WordListFragment.kt          # 단어 목록
├── WordCategoryFragment.kt      # 단어 카테고리
├── SentenceListFragment.kt      # 문장 목록
├── QuizFragment.kt              # 퀴즈 메인
├── BaseQuizModeFragment.kt      # 퀴즈 베이스
├── QuizWordModeFragment.kt      # 단어 퀴즈
├── QuizSentenceModeFragment.kt  # 문장 퀴즈
├── SongListFragment.kt          # 노래 목록
├── SongLyricsFragment.kt        # 노래 가사
├── DailyWordListFragment.kt     # 데일리 단어
└── ...
```

### ViewModels
```
ui/
├── KanaQuizViewModel.kt
├── WordQuizViewModel.kt
├── SentenceQuizViewModel.kt
├── SongQuizViewModel.kt
├── QuizStateViewModel.kt
└── QuizViewModel.kt
```

### Repositories
```
repository/
├── KanaRepository.kt
├── WordRepository.kt
├── SentenceRepository.kt
├── SongRepository.kt
├── DailyWordRepository.kt
├── PreferencesRepository.kt
└── SentenceGeneratorRepository.kt
```

### Models
```
model/
├── KanaCharacter.kt
├── KanaData.kt
├── Word.kt
├── Sentence.kt
├── Song.kt
├── SongInfo.kt
├── DailyWord.kt
├── UnknownWord.kt
└── GeneratedSentence.kt
```

---

## 주의사항

### 코드 품질
- Null 안전성 철저히 준수 (`!!` 금지)
- Dead Code 즉시 제거
- 중복 코드 공통 함수로 추출
- MVVM 패턴 일관성 유지

### UI/UX
- 다크 모드 필수 (텍스트: 흰색, 배경: 검정)
- Material Design 3 가이드라인
- 일관된 네비게이션 패턴

### 성능
- 대용량 JSON 로딩 시 비동기 처리
- RecyclerView ViewHolder 재사용
- 이미지 로딩 최적화

### 보안
- API 키 하드코딩 금지
- 민감 정보 로그 출력 금지

---

## 코드 품질 분석 결과 (2026-01-22)

### 분석 요약

| 심각도 | 개수 | 설명 |
|--------|------|------|
| **Critical** | 4 | 즉시 수정 필요, 앱 크래시/데이터 손실 위험 |
| **Major** | 16 | 중요한 문제, 조속한 수정 권장 |
| **Minor** | 14 | 개선 권장 사항 |

---

## GitHub Issues 생성 계획

### 🔴 Critical Issues (P0 - 즉시 수정)

#### Issue #1: [Critical] 메인 스레드에서 파일 I/O 블로킹
- **라벨**: `bug`, `critical`, `performance`
- **파일**:
  - `SongLyricsFragment.kt:221-222`
  - `PreferencesRepository.kt:52-56`
- **문제**: 파일 쓰기/SharedPreferences 저장이 메인 스레드에서 동기적으로 수행되어 UI 프리징 발생 가능
- **해결**: `withContext(Dispatchers.IO)` 사용하여 비동기 처리
- **브랜치**: `bugfix/#1-fix-main-thread-io-blocking`

#### Issue #2: [Critical] MVVM 패턴 위반 - View에서 직접 데이터 접근
- **라벨**: `refactor`, `critical`, `architecture`
- **파일**:
  - `QuizFragment.kt:52-61`
  - `DailyWordListFragment.kt:74-82`
- **문제**: Fragment가 SharedPreferences에 직접 접근하여 MVVM 패턴 위반
- **해결**: `PreferencesRepository`를 통한 데이터 접근으로 변경
- **브랜치**: `refactor/#2-fix-mvvm-violation-data-access`

#### Issue #3: [Critical] WeakWords 데이터 동기화 불일치
- **라벨**: `bug`, `critical`, `data`
- **파일**:
  - `QuizFragment.kt:26` (Fragment 로컬 변수)
  - `QuizViewModel.kt:42` (ViewModel LiveData)
- **문제**: 같은 데이터가 Fragment와 ViewModel에서 별도로 관리되어 데이터 불일치 발생 가능
- **해결**: ViewModel의 LiveData만 사용하고 Fragment 로컬 변수 제거
- **브랜치**: `bugfix/#3-fix-weak-words-sync`

---

### 🟠 Major Issues (P1 - 조속한 수정)

#### Issue #4: [Major] Null 안전성 - !! 연산자 과다 사용
- **라벨**: `bug`, `code-quality`
- **파일**:
  - `QuizFragment.kt:24` - `_binding!!`
  - `QuizViewModel.kt:574` - `cachedSelectedKana!!`
  - `MainActivity.kt:55,74,80` - 강제 타입 캐스팅
- **문제**: `!!` 연산자 사용으로 NPE 위험
- **해결**: safe call `?.`, Elvis `?:`, `requireNotNull()` 사용
- **브랜치**: `bugfix/#4-fix-null-safety`

#### Issue #5: [Major] 메모리 누수 - MediaPlayer 리소스 미해제
- **라벨**: `bug`, `memory-leak`
- **파일**: `SongMediaPlayerHelper.kt:109-137, 186-198`
- **문제**:
  - `playSection()` 중복 호출 시 이전 리스너/Runnable 미정리
  - Handler 누수 가능성
- **해결**: `release()` 호출 전 명시적 정리, WeakReference 사용 고려
- **브랜치**: `bugfix/#5-fix-mediaplayer-memory-leak`

#### Issue #6: [Major] 메모리 누수 - observeForever 사용
- **라벨**: `bug`, `memory-leak`
- **파일**: `QuizViewModel.kt:96`
- **문제**: `observeForever()`는 lifecycle-aware가 아니어서 누수 위험
- **해결**: Fragment에서 lifecycle-aware observe 사용 또는 onCleared에서 확실한 제거
- **브랜치**: `bugfix/#6-fix-observe-forever-leak`

#### Issue #7: [Major] 중복 코드 - WeakWords 로드 로직
- **라벨**: `refactor`, `duplicate-code`
- **파일**:
  - `QuizFragment.kt:52-56`
  - `DailyWordListFragment.kt:73-77`
  - `QuizViewModel.kt:122-125`
- **문제**: 동일한 weakWords 로드 로직이 여러 곳에 산재
- **해결**: `PreferencesRepository`의 단일 메서드로 통합
- **브랜치**: `refactor/#7-consolidate-weak-words-logic`

#### Issue #8: [Major] 중복 코드 - 4개 Choice 버튼 반복 처리
- **라벨**: `refactor`, `duplicate-code`
- **파일**:
  - `QuizFragment.kt:255-258`
  - `QuizUiHelper.kt:154-161, 202-207`
- **문제**: 4개 버튼을 개별적으로 반복 처리
- **해결**: 버튼 배열/리스트로 통합 관리
- **브랜치**: `refactor/#8-consolidate-choice-buttons`

#### Issue #9: [Major] 타입 안전성 - 문자열 기반 타입 비교
- **라벨**: `refactor`, `type-safety`
- **파일**: `QuizFragment.kt:76-86`
- **문제**: `quizType`이 문자열 기반으로 비교 후 unsafe cast 사용
- **해결**: `sealed class QuizType`으로 변경
- **브랜치**: `refactor/#9-implement-sealed-quiz-type`

#### Issue #10: [Major] 성능 - 데이터 캐싱 부재
- **라벨**: `performance`, `optimization`
- **파일**: `QuizViewModel.kt:210, 300-301`
- **문제**: 자주 호출되는 데이터 조회가 캐싱되지 않음
- **해결**: Repository 레벨에서 캐싱 구현
- **브랜치**: `feature/#10-implement-data-caching`

---

### 🟡 Minor Issues (P2 - 개선 권장)

#### Issue #11: [Minor] 하드코딩 - 문자열 리소스화
- **라벨**: `enhancement`, `i18n`
- **파일**:
  - `DailyWordListFragment.kt:46` - `"${day}일차\n(${words.size}단어)"`
  - `SongLyricsFragment.kt:136` - `"닫기"`, `"편집"`
- **문제**: 한글 문자열이 코드에 하드코딩
- **해결**: `strings.xml` 리소스로 이동
- **브랜치**: `refactor/#11-extract-string-resources`

#### Issue #12: [Minor] 하드코딩 - 상수값 추출
- **라벨**: `enhancement`, `code-quality`
- **파일**:
  - `QuizStateViewModel.kt:28-29` - `penWidth = 12f`, `eraserWidth = 40f`
  - `WritingView.kt:32, 40` - `strokeWidth = 12f, 40f`
  - `QuizViewModel.kt:67` - `"pretender"`
- **문제**: 숫자/문자열 상수가 하드코딩
- **해결**: `Constants.kt`에 상수 정의
- **브랜치**: `refactor/#12-extract-constants`

#### Issue #13: [Minor] 하드코딩 - 색상 리소스화
- **라벨**: `enhancement`, `ui`
- **파일**:
  - `SongMediaPlayerHelper.kt:102` - `Color.parseColor("#33FFFFFF")`
  - `WritingView.kt:30` - `Color.WHITE`
- **문제**: 색상값이 코드에 하드코딩
- **해결**: `colors.xml` 리소스로 이동
- **브랜치**: `refactor/#13-extract-color-resources`

#### Issue #14: [Minor] 성능 - 정규식 재컴파일
- **라벨**: `performance`, `optimization`
- **파일**: `SongMediaPlayerHelper.kt:235`
- **문제**: 정규식이 매 호출마다 컴파일됨
- **해결**: companion object에서 미리 컴파일
- **브랜치**: `refactor/#14-precompile-regex`

#### Issue #15: [Minor] Dead Code 제거
- **라벨**: `cleanup`, `code-quality`
- **파일**: `QuizViewModel.kt:445-447`
- **문제**: `@Deprecated` 메서드가 여전히 존재
- **해결**: 사용처 확인 후 제거
- **브랜치**: `refactor/#15-remove-dead-code`

---

## 이슈 우선순위 및 작업 순서

### Sprint 1: 안정성 확보 (Critical + Major Bug)
1. Issue #1: 메인 스레드 I/O 블로킹
2. Issue #3: WeakWords 동기화 불일치
3. Issue #4: Null 안전성
4. Issue #5: MediaPlayer 메모리 누수
5. Issue #6: observeForever 누수

### Sprint 2: 아키텍처 개선 (Refactoring)
6. Issue #2: MVVM 패턴 위반
7. Issue #7: WeakWords 로직 통합
8. Issue #8: Choice 버튼 통합
9. Issue #9: sealed class QuizType

### Sprint 3: 성능 및 품질 개선 (Minor)
10. Issue #10: 데이터 캐싱
11. Issue #11-13: 하드코딩 제거
12. Issue #14: 정규식 최적화
13. Issue #15: Dead Code 제거

---

## 관련 문서
- [CLAUDE.md](CLAUDE.md) - 메인 수칙
- [branch-naming.md](branch-naming.md) - 브랜치 규칙
- [commit-conventions.md](commit-conventions.md) - 커밋 규칙
- [project-rules.md](project-rules.md) - 프로젝트 규칙
