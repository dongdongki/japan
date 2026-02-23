# Commit Conventions

## 커밋 메시지 형식

### 기본 구조
```
<type>(<scope>): <subject>

<body>

<footer>
```

### 언어 규칙
- **제목 (subject)**: 영어
- **본문 (body)**: 한국어 (선택)
- **꼬리말 (footer)**: 영어

---

## Type 목록

| Type | 설명 | 예시 |
|------|------|------|
| `feat` | 새로운 기능 추가 | 새 퀴즈 모드 추가 |
| `fix` | 버그 수정 | 앱 크래시 수정 |
| `docs` | 문서 수정 | README 업데이트 |
| `style` | 코드 포맷팅 (기능 변경 X) | 들여쓰기, 세미콜론 |
| `refactor` | 코드 리팩토링 | 함수 추출, 구조 개선 |
| `test` | 테스트 코드 | 유닛 테스트 추가 |
| `chore` | 빌드, 설정 파일 수정 | gradle 설정 변경 |
| `perf` | 성능 개선 | 로딩 속도 최적화 |

---

## Scope 매핑 (Android)

| 파일 패턴 | Scope |
|----------|-------|
| `*/ui/*`, `*/view/*` | **ui** |
| `*/viewmodel/*`, `*ViewModel.kt` | **viewmodel** |
| `*/repository/*` | **repository** |
| `*/model/*`, `*/data/*` | **data** |
| `*/service/*`, `*/network/*` | **network** |
| `*/di/*` | **di** |
| `*/util/*` | **util** |
| `res/layout/*` | **layout** |
| `res/values/*` | **res** |
| `AndroidManifest.xml` | **manifest** |
| `build.gradle*` | **build** |
| `*.md` | **docs** |

### 복수 Scope
여러 영역에 걸친 변경은 가장 중요한 Scope 하나만 선택하거나, 생략 가능

---

## Subject 생성 규칙

### 규칙
1. **50자 이내** 권장
2. **첫 글자 대문자** (영어)
3. **마침표 없음**
4. **명령형** 사용 (Add, Fix, Update, Remove, Refactor)

### 자주 쓰는 동사
| 동사 | 용도 |
|------|------|
| `Add` | 새 기능/파일 추가 |
| `Fix` | 버그 수정 |
| `Update` | 기존 기능 수정/개선 |
| `Remove` | 코드/파일 삭제 |
| `Refactor` | 코드 구조 개선 |
| `Rename` | 이름 변경 |
| `Move` | 파일/코드 이동 |
| `Improve` | 성능/품질 개선 |

---

## Body 작성 가이드

### 작성 시점
- 변경 이유가 명확하지 않을 때
- 복잡한 변경일 때
- 기술적 결정 배경이 필요할 때

### 규칙
1. 한 줄 공백 후 작성
2. **What보다 Why** 중심
3. 한국어 작성 가능
4. 72자 이내로 줄바꿈

### 예시
```
feat(ui): Add kana writing practice mode

가나 필기 연습 기능 추가
- 히라가나/카타카나 선택 가능
- 획순 가이드 표시
- 터치로 필기 입력
```

---

## Footer 작성 가이드

### Breaking Changes
```
BREAKING CHANGE: <설명>
```

### 이슈 참조
```
Closes #42
Fixes #51
Refs #23
```

---

## 실전 예시

### Feature
```bash
# 간단한 기능
feat(ui): Add word quiz fragment

# 상세 설명 포함
feat(viewmodel): Add quiz state management

퀴즈 진행 상태 관리를 위한 ViewModel 추가
- 현재 문제 인덱스 추적
- 정답/오답 카운트
- 퀴즈 완료 상태

Closes #42
```

### Fix
```bash
# 간단한 버그
fix(ui): Fix null pointer in word list

# 상세 설명 포함
fix(repository): Fix data parsing error

JSON 파싱 시 nullable 필드 처리 누락 수정
- meaning 필드 null 체크 추가
- 기본값 설정

Fixes #51
```

### Refactor
```bash
refactor(ui): Extract common quiz logic to base class

공통 퀴즈 로직을 BaseQuizModeFragment로 추출
- 문제 표시 로직
- 정답 확인 로직
- 결과 표시 로직
```

### Docs
```bash
docs(docs): Update project guidelines

프로젝트 지침 문서 추가
- CLAUDE.md: 메인 수칙
- branch-naming.md: 브랜치 규칙
- commit-conventions.md: 커밋 규칙
```

### Chore
```bash
chore(build): Update gradle dependencies

의존성 버전 업데이트
- Hilt 2.48 -> 2.50
- Kotlin 1.9.20 -> 1.9.22
```

---

## 검증 체크리스트

- [ ] type이 올바른가? (feat, fix, docs, style, refactor, test, chore, perf)
- [ ] scope가 적절한가?
- [ ] subject가 50자 이내인가?
- [ ] subject가 명령형인가?
- [ ] 관련 이슈가 있다면 footer에 참조했는가?

---

## Git Hooks 설정 (선택)

### commit-msg hook 예시
```bash
#!/bin/sh
# .git/hooks/commit-msg

commit_regex='^(feat|fix|docs|style|refactor|test|chore|perf)(\([a-z]+\))?: .{1,50}'

if ! grep -qE "$commit_regex" "$1"; then
    echo "Invalid commit message format!"
    echo "Expected: <type>(<scope>): <subject>"
    exit 1
fi
```
