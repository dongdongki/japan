# Branch Naming Guide

## 브랜치 전략: Git Flow

```
main (production)
  │
  └── dev (development)
       ├── feature/* (새 기능)
       ├── bugfix/* (버그 수정)
       └── hotfix/* (긴급 수정)
```

---

## 브랜치 Prefix 매핑

| Prefix | 용도 | 설명 |
|--------|------|------|
| `feature/` | 새 기능 개발 | 새로운 기능 추가 시 사용 |
| `bugfix/` | 버그 수정 | dev 브랜치에서 발견된 버그 수정 |
| `hotfix/` | 긴급 수정 | production 긴급 버그 수정 |
| `release/` | 릴리스 준비 | 릴리스 전 최종 준비 |
| `refactor/` | 리팩토링 | 기능 변경 없는 코드 개선 |

---

## 브랜치명 생성 규칙

### 기본 형식
```
<prefix>/<issue-number>-<description>
```

### 규칙
1. **소문자** 사용 (대문자 금지)
2. **단어 구분**은 하이픈(`-`) 사용 (언더스코어 금지)
3. **이슈 번호** 포함 권장
4. **간결하고 명확한** 설명 사용 (3~5 단어)
5. **영어** 사용

### 설명 작성 가이드
- 기능/수정 내용을 명확히 표현
- 불필요한 단어 제거
- 동사로 시작 권장

---

## 실전 예시

### Feature 브랜치
```bash
# Good
feature/42-add-word-quiz
feature/15-implement-tts-service
feature/23-add-kana-writing-practice

# Bad
feature/AddWordQuiz          # 대문자 사용
feature/add_word_quiz        # 언더스코어 사용
feature/quiz                 # 너무 모호함
```

### Bugfix 브랜치
```bash
# Good
bugfix/51-fix-quiz-navigation
bugfix/33-fix-null-pointer-in-word-list
bugfix/67-fix-tts-crash

# Bad
bugfix/fix                   # 너무 모호함
bugfix/FixNavigation         # 대문자 사용
```

### Hotfix 브랜치
```bash
# Good
hotfix/89-fix-app-crash-on-launch
hotfix/92-fix-data-loss-issue

# Bad
hotfix/urgent                # 모호함
```

### Refactor 브랜치
```bash
# Good
refactor/45-extract-quiz-common-logic
refactor/78-migrate-to-viewbinding

# Bad
refactor/clean               # 너무 모호함
```

---

## GitHub Issues 연동

### 이슈 번호 포함 형식
```
feature/#42-add-word-quiz
bugfix/#51-fix-quiz-navigation
```

### 자동 이슈 닫기
PR 머지 시 커밋 메시지에 다음 키워드 사용:
- `Closes #42`
- `Fixes #51`
- `Resolves #89`

---

## 검증 규칙

### 브랜치명 검증 정규식
```regex
^(feature|bugfix|hotfix|release|refactor)\/([0-9]+-)?[a-z0-9]+(-[a-z0-9]+)*$
```

### 체크리스트
- [ ] prefix가 올바른가? (feature, bugfix, hotfix, release, refactor)
- [ ] 소문자만 사용했는가?
- [ ] 하이픈으로 단어를 구분했는가?
- [ ] 이슈 번호를 포함했는가? (권장)
- [ ] 설명이 명확한가?

---

## 브랜치 생명주기

### Feature/Bugfix 브랜치
1. `dev` 브랜치에서 생성
2. 작업 완료 후 `dev`로 PR
3. 코드 리뷰 후 머지
4. 머지 후 브랜치 삭제

### Hotfix 브랜치
1. `main` 브랜치에서 생성
2. 수정 완료 후 `main`과 `dev` 모두에 머지
3. 머지 후 브랜치 삭제

### Release 브랜치
1. `dev` 브랜치에서 생성
2. 최종 테스트 및 버그 수정
3. `main`과 `dev` 모두에 머지
4. `main`에 버전 태그 생성
5. 머지 후 브랜치 삭제
