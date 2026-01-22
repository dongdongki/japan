# Claude 프로젝트 지침 자동 생성 가이드

> 이 파일을 새 프로젝트의 `.claude/` 폴더에 복사하고, Claude에게 "프로젝트 지침을 생성해줘"라고 요청하세요.

---

## 사용 방법

### Step 1: 새 프로젝트에 이 파일 복사
```
새프로젝트/
├── .claude/
│   └── SETUP-GUIDE.md  ← 이 파일
└── (프로젝트 파일들)
```

### Step 2: Claude에게 요청
```
"SETUP-GUIDE.md를 읽고 이 프로젝트에 맞는 지침 파일들을 생성해줘"
```

### Step 3: Claude가 질문에 답변
Claude가 프로젝트 특성에 맞는 질문을 하면 답변합니다.

### Step 4: 생성되는 파일들
- `CLAUDE.md` - Claude 작업 규칙 (메인 지침)
- `branch-naming.md` - 브랜치 명명 규칙
- `commit-conventions.md` - 커밋 메시지 규칙
- `project-rules.md` - 프로젝트 규칙 및 코딩 표준
- `plan.md` - 프로젝트 계획서 (선택)

---

## Claude 지침 생성 프로세스

### Phase 1: 프로젝트 분석 (자동)

Claude는 다음을 자동으로 분석합니다:

1. **프로젝트 구조 파악**
   - 디렉토리 구조 스캔
   - 주요 파일 확인 (package.json, build.gradle, Cargo.toml 등)
   - 기존 README.md 또는 문서 확인

2. **기술 스택 감지**
   - 언어: 파일 확장자 분석 (.ts, .py, .cpp, .rs 등)
   - 프레임워크: 설정 파일 분석
   - 빌드 시스템: 빌드 파일 확인

3. **Git 상태 확인**
   - 브랜치 전략 추론 (main/master, dev, develop 등)
   - 기존 커밋 메시지 스타일 분석

---

### Phase 2: 사용자 질문 (필수)

Claude는 다음 질문들을 사용자에게 합니다:

#### 2.1 프로젝트 기본 정보
```
Q1. 프로젝트 유형은 무엇인가요?
    [ ] 웹 프론트엔드 (React, Vue, Angular 등)
    [ ] 웹 백엔드 (Node.js, Django, Spring 등)
    [ ] 모바일 앱 (Android, iOS, Flutter, React Native)
    [ ] 데스크톱 앱 (Electron, Tauri, Native)
    [ ] 게임 (Unity, Unreal, Native)
    [ ] 라이브러리/SDK
    [ ] CLI 도구
    [ ] 기타: ___________

Q2. 주요 프로그래밍 언어는 무엇인가요? (복수 선택 가능)
    [ ] TypeScript/JavaScript
    [ ] Python
    [ ] Java/Kotlin
    [ ] C/C++
    [ ] Rust
    [ ] Go
    [ ] C#
    [ ] 기타: ___________

Q3. 프로젝트 설명을 간단히 작성해주세요.
    예: "실시간 채팅 기능이 있는 협업 문서 편집기"
```

#### 2.2 브랜치 전략
```
Q4. Git 브랜치 전략은 어떻게 하시겠습니까?
    [ ] Git Flow (main, dev, feature/*, bugfix/*, hotfix/*)
    [ ] GitHub Flow (main + feature branches)
    [ ] Trunk-Based Development (main + short-lived branches)
    [ ] 기타: ___________

Q5. 기본 브랜치 이름은 무엇인가요?
    [ ] main
    [ ] master
    [ ] dev
    [ ] develop
    [ ] 기타: ___________

Q6. 이슈 트래커를 사용하시나요?
    [ ] GitHub Issues
    [ ] Jira
    [ ] Linear
    [ ] 사용 안 함
    [ ] 기타: ___________
```

#### 2.3 커밋 컨벤션
```
Q7. 커밋 메시지 스타일은 어떻게 하시겠습니까?
    [ ] Conventional Commits (feat, fix, docs, style, refactor, test, chore)
    [ ] Gitmoji (이모지 사용)
    [ ] Angular Style
    [ ] 자유 형식
    [ ] 기타: ___________

Q8. 커밋 메시지 언어는 무엇으로 하시겠습니까?
    [ ] 영어
    [ ] 한국어
    [ ] 혼용 (제목 영어, 본문 한국어 등)
```

#### 2.4 코드 품질 규칙
```
Q9. 특별히 강조하고 싶은 코드 품질 규칙이 있나요? (복수 선택)
    [ ] 엄격한 타입 검사
    [ ] 테스트 커버리지 요구
    [ ] 코드 리뷰 필수
    [ ] 린터/포매터 강제
    [ ] 문서화 필수
    [ ] 기타: ___________

Q10. 프로젝트에서 특별히 주의해야 할 사항이 있나요?
     예: "보안에 민감한 금융 데이터를 다룸", "성능이 매우 중요함"
```

#### 2.5 프로젝트 계획 (선택)
```
Q11. 프로젝트 계획서(plan.md)를 생성할까요?
     [ ] 예 - 로드맵과 마일스톤 포함
     [ ] 아니오 - 나중에 작성

(예 선택 시)
Q12. 주요 마일스톤이나 단계를 알려주세요.
     예: "1단계: MVP, 2단계: 베타, 3단계: 정식 출시"
```

---

### Phase 3: 지침 파일 생성

Claude는 수집한 정보를 바탕으로 다음 파일들을 생성합니다:

#### 3.1 CLAUDE.md (메인 지침)
```markdown
# Claude 수칙

## 작업 규칙
- 이슈 등록 규칙
- 이슈 템플릿
- 브랜치/커밋/PR 규칙 요약
- 브랜치 전략 요약

## 빌드 설정
- 프로젝트 특화 빌드 설정

## [언어] 코드 생성 필수 규칙 (CRITICAL)
- 해당 언어에 맞는 필수 규칙

## 코드 품질 원칙 (필수 준수)
- Dead Code 제거
- 중복 코드 금지
- 공통 함수화
- 적절한 객체화
```

#### 3.2 branch-naming.md
```markdown
# Branch Naming Guide

## 브랜치 Prefix 매핑
## 브랜치명 생성 규칙
## 실전 예시
## 검증 규칙
```

#### 3.3 commit-conventions.md
```markdown
# Commit Conventions

## 파일 경로 → Scope 매핑
## 변경 패턴 → Type 결정
## Subject 생성 규칙
## 실전 예시
```

#### 3.4 project-rules.md
```markdown
# Project Rules

## 프로젝트 개요
## 기술 스택
## 코딩 표준
## 브랜치/커밋/PR 규칙 상세
## 코드 품질 체크리스트
```

#### 3.5 plan.md (선택)
```markdown
# 프로젝트 계획서

## 아키텍처 설계
## 개발 로드맵
## 주의사항
```

---

## 언어별 코드 규칙 템플릿

### C/C++ 프로젝트
```markdown
## C++ 코드 생성 필수 규칙 (CRITICAL)

### 1. Include 완전성
- 사용하는 모든 STL 함수는 해당 헤더를 직접 include
- 예: memcpy → <cstring>, unique_ptr → <memory>

### 2. const 정확성 선행 설계
- API 설계 시 const 버전을 먼저 고려
- Dirty Flag 패턴: mutable 캐시 + const 메서드

### 3. 방어적 입력 검증
- 외부 데이터는 항상 악의적 입력 가정
- 포인터 연산 오버플로우 방지

### 4. 메모리 관리
- RAII 패턴 준수
- 스마트 포인터 사용 권장
```

### TypeScript/JavaScript 프로젝트
```markdown
## TypeScript 코드 생성 필수 규칙 (CRITICAL)

### 1. 타입 안전성
- any 타입 사용 금지 (unknown 사용)
- strict 모드 활성화

### 2. 에러 처리
- try-catch에서 에러 타입 명시
- Promise rejection 처리 필수

### 3. 불변성
- const 선호, let 최소화
- 객체/배열 수정 시 새 참조 생성

### 4. 모듈 구조
- 순환 의존성 금지
- barrel export 패턴 사용
```

### Python 프로젝트
```markdown
## Python 코드 생성 필수 규칙 (CRITICAL)

### 1. 타입 힌트
- 모든 함수 파라미터와 반환값에 타입 힌트 작성
- typing 모듈 활용

### 2. 예외 처리
- 구체적인 예외 타입 사용 (Exception 금지)
- 컨텍스트 매니저(with) 사용

### 3. 코드 스타일
- PEP 8 준수
- f-string 사용

### 4. 의존성 관리
- requirements.txt 또는 pyproject.toml 유지
- 가상환경 사용
```

### Rust 프로젝트
```markdown
## Rust 코드 생성 필수 규칙 (CRITICAL)

### 1. 에러 처리
- unwrap() 금지 (프로덕션 코드)
- Result와 Option 적절히 활용
- ? 연산자 사용

### 2. 소유권
- 불필요한 clone() 피하기
- 참조 생명주기 명시

### 3. 안전성
- unsafe 블록 최소화
- unsafe 사용 시 문서화 필수

### 4. 성능
- 벡터 사전 할당 (with_capacity)
- 이터레이터 체이닝 활용
```

### Java/Kotlin 프로젝트
```markdown
## Kotlin 코드 생성 필수 규칙 (CRITICAL)

### 1. Null 안전성
- nullable 타입 최소화
- !! 연산자 사용 금지
- safe call(?.)과 Elvis(?:) 활용

### 2. 불변성
- val 선호, var 최소화
- data class 활용

### 3. 코루틴
- 적절한 Dispatcher 사용
- 구조화된 동시성

### 4. 자원 관리
- use 블록으로 자원 해제
- try-with-resources 패턴
```

---

## Scope 매핑 템플릿

### 웹 프론트엔드 (React/Vue/Angular)
| 파일 패턴 | Scope |
|----------|-------|
| `src/components/*` | **ui** |
| `src/pages/*`, `src/views/*` | **page** |
| `src/hooks/*`, `src/composables/*` | **hook** |
| `src/store/*`, `src/state/*` | **state** |
| `src/api/*`, `src/services/*` | **api** |
| `src/utils/*`, `src/helpers/*` | **util** |
| `src/types/*` | **type** |
| `tests/*`, `__tests__/*` | **test** |

### 웹 백엔드 (Node.js/Express)
| 파일 패턴 | Scope |
|----------|-------|
| `src/controllers/*` | **controller** |
| `src/routes/*` | **route** |
| `src/services/*` | **service** |
| `src/models/*` | **model** |
| `src/middlewares/*` | **middleware** |
| `src/utils/*` | **util** |
| `src/config/*` | **config** |
| `tests/*` | **test** |

### 모바일 앱 (Android)
| 파일 패턴 | Scope |
|----------|-------|
| `*/ui/*`, `*/view/*` | **ui** |
| `*/viewmodel/*` | **viewmodel** |
| `*/repository/*` | **repository** |
| `*/data/*`, `*/model/*` | **data** |
| `*/network/*`, `*/api/*` | **network** |
| `*/di/*` | **di** |
| `*/util/*` | **util** |

### 게임 (Unity/Native)
| 파일 패턴 | Scope |
|----------|-------|
| `*/core/*` | **core** |
| `*/rendering/*`, `*/graphics/*` | **renderer** |
| `*/audio/*` | **audio** |
| `*/input/*` | **input** |
| `*/physics/*` | **physics** |
| `*/ai/*` | **ai** |
| `*/ui/*` | **ui** |
| `*/storage/*`, `*/save/*` | **storage** |

---

## 자동 생성 프롬프트 예시

Claude에게 다음과 같이 요청할 수 있습니다:

```
이 프로젝트에 맞는 Claude 지침 파일들을 생성해줘.
프로젝트 정보:
- 유형: React 웹 프론트엔드
- 언어: TypeScript
- 설명: 실시간 협업 문서 편집기
- 브랜치 전략: GitHub Flow
- 이슈 트래커: GitHub Issues
- 커밋 스타일: Conventional Commits (영어)
```

또는 간단히:

```
SETUP-GUIDE.md를 읽고 이 프로젝트에 맞는 지침 파일들을 생성해줘
```

---

## 생성된 파일 관리

### Git 커밋
```bash
git add .claude/
git commit -m "docs(docs): Add Claude project guidelines

Initialize project conventions and coding standards."
```

### 업데이트
프로젝트가 발전함에 따라 지침 파일도 업데이트할 수 있습니다:
```
"project-rules.md에 새로운 코딩 규칙을 추가해줘: [규칙 내용]"
```

---

## 참고

- 이 가이드는 어떤 종류의 프로젝트에도 적용 가능합니다.
- Claude는 프로젝트 특성에 맞게 규칙을 조정합니다.
- 생성된 파일은 필요에 따라 수동으로 수정할 수 있습니다.
