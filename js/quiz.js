const Quiz = {
  state: {
    questions: [],
    current: 0,
    correct: 0,
    wrongList: [],
    answered: false,
  },

  renderSetup(container, data) {
    const rowNames = Object.keys(data.hiragana);

    container.innerHTML = `
      <div class="quiz-setup">
        <h2>퀴즈 설정</h2>

        <div class="setup-section">
          <h3>문자 종류</h3>
          <div class="radio-group">
            <input type="radio" name="kanaType" id="type-hiragana" value="hiragana" class="radio-btn" checked>
            <label for="type-hiragana" class="radio-label">히라가나</label>
            <input type="radio" name="kanaType" id="type-katakana" value="katakana" class="radio-btn">
            <label for="type-katakana" class="radio-label">카타카나</label>
            <input type="radio" name="kanaType" id="type-both" value="both" class="radio-btn">
            <label for="type-both" class="radio-label">전체</label>
          </div>
        </div>

        <div class="setup-section">
          <h3>범위</h3>
          <div class="radio-group">
            <input type="radio" name="rangeType" id="range-all" value="all" class="radio-btn" checked>
            <label for="range-all" class="radio-label">전체</label>
            <input type="radio" name="rangeType" id="range-row" value="row" class="radio-btn">
            <label for="range-row" class="radio-label">줄별 선택</label>
          </div>
        </div>

        <div class="setup-section" id="row-select" style="display:none;">
          <h3>줄 선택</h3>
          <div class="row-checkboxes">
            ${rowNames.map(name => `
              <input type="checkbox" id="row-${name}" value="${name}" class="row-checkbox">
              <label for="row-${name}" class="row-check-label">${name}</label>
            `).join('')}
          </div>
        </div>

        <button class="btn-start" id="btn-quiz-start">시작</button>
      </div>`;

    this._bindSetupEvents(data);
  },

  _bindSetupEvents(data) {
    const rangeRadios = document.querySelectorAll('input[name="rangeType"]');
    const rowSelect = document.getElementById('row-select');
    const startBtn = document.getElementById('btn-quiz-start');

    rangeRadios.forEach(r => r.addEventListener('change', () => {
      rowSelect.style.display = r.value === 'row' && r.checked ? 'block' : 'none';
    }));

    startBtn.addEventListener('click', () => {
      const kanaType = document.querySelector('input[name="kanaType"]:checked').value;
      const rangeType = document.querySelector('input[name="rangeType"]:checked').value;

      let questions = [];

      if (rangeType === 'all') {
        if (kanaType === 'both') {
          questions = this._getAllChars(data.hiragana).concat(this._getAllChars(data.katakana));
        } else {
          questions = this._getAllChars(data[kanaType]);
        }
      } else {
        const checked = Array.from(document.querySelectorAll('.row-checkbox:checked')).map(c => c.value);
        if (checked.length === 0) return;

        if (kanaType === 'both') {
          questions = this._getRowChars(data.hiragana, checked).concat(this._getRowChars(data.katakana, checked));
        } else {
          questions = this._getRowChars(data[kanaType], checked);
        }
      }

      this._shuffle(questions);
      this.state = { questions, current: 0, correct: 0, wrongList: [], answered: false };
      this._renderPlay(document.getElementById('app'));
    });
  },

  _getAllChars(kanaObj) {
    return Object.values(kanaObj).flat();
  },

  _getRowChars(kanaObj, rowNames) {
    return rowNames.filter(name => kanaObj[name]).flatMap(name => kanaObj[name]);
  },

  _shuffle(arr) {
    for (let i = arr.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [arr[i], arr[j]] = [arr[j], arr[i]];
    }
  },

  _renderPlay(container) {
    const { questions, current, correct } = this.state;
    const total = questions.length;
    const q = questions[current];
    const progress = ((current) / total) * 100;

    container.innerHTML = `
      <div class="quiz-play">
        <div class="quiz-progress">
          <span>${current + 1} / ${total}</span>
          <span>정답 ${correct}개</span>
        </div>
        <div class="progress-bar">
          <div class="progress-fill" style="width: ${progress}%"></div>
        </div>

        <div class="quiz-question">${q.kana}</div>

        <div class="quiz-input-area">
          <input type="text" class="quiz-input" id="quiz-answer" placeholder="한글 발음 입력" autocomplete="off" autofocus>
          <div class="quiz-feedback" id="quiz-feedback"></div>
        </div>
      </div>`;

    const input = document.getElementById('quiz-answer');
    input.focus();

    input.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') this._checkAnswer();
    });
  },

  _checkAnswer() {
    if (this.state.answered) {
      this._next();
      return;
    }

    const input = document.getElementById('quiz-answer');
    const feedback = document.getElementById('quiz-feedback');
    const userAnswer = input.value.trim();
    const q = this.state.questions[this.state.current];
    const correctAnswer = q.kor;

    if (!userAnswer) return;

    this.state.answered = true;

    if (userAnswer === correctAnswer) {
      this.state.correct++;
      input.classList.add('correct');
      feedback.className = 'quiz-feedback correct';
      feedback.textContent = '정답!';
    } else {
      this.state.wrongList.push(q);
      input.classList.add('incorrect');
      feedback.className = 'quiz-feedback incorrect';
      feedback.innerHTML = `오답! <span class="correct-answer">정답: ${q.kor} (${q.romaji})</span>`;
    }

    input.readOnly = true;

    // Enter로 다음 문제 진행 가능하도록 안내 대신 바로 포커스 유지
    setTimeout(() => input.focus(), 10);
  },

  _next() {
    this.state.current++;
    this.state.answered = false;

    if (this.state.current >= this.state.questions.length) {
      this._renderResult(document.getElementById('app'));
    } else {
      this._renderPlay(document.getElementById('app'));
    }
  },

  _renderResult(container) {
    const { questions, correct, wrongList } = this.state;
    const total = questions.length;
    const rate = total > 0 ? Math.round((correct / total) * 100) : 0;

    let wrongHtml = '';
    if (wrongList.length > 0) {
      wrongHtml = `
        <div class="wrong-list">
          <h3>틀린 문자 (${wrongList.length}개)</h3>
          ${wrongList.map(w => `
            <div class="wrong-item">
              <span class="char">${w.kana}</span>
              <span>${w.romaji}</span>
              <span style="color:var(--text-secondary)">${w.kor}</span>
            </div>
          `).join('')}
        </div>`;
    }

    container.innerHTML = `
      <div class="quiz-result">
        <h2>결과</h2>
        <div class="result-stats">
          <div class="stat-box">
            <div class="stat-value">${total}</div>
            <div class="stat-label">총 문제</div>
          </div>
          <div class="stat-box">
            <div class="stat-value">${correct}</div>
            <div class="stat-label">정답</div>
          </div>
          <div class="stat-box">
            <div class="stat-value" style="color: ${rate >= 80 ? 'var(--correct)' : rate >= 50 ? 'var(--accent)' : 'var(--incorrect)'}">${rate}%</div>
            <div class="stat-label">정답률</div>
          </div>
        </div>

        ${wrongHtml}

        <div class="result-buttons">
          ${wrongList.length > 0 ? '<button class="btn-retry" id="btn-retry-wrong">틀린 문제 다시</button>' : ''}
          <button class="btn-retry" id="btn-retry-all">다시 풀기</button>
          <button class="btn-home" id="btn-result-home">홈으로</button>
        </div>
      </div>`;

    const retryWrongBtn = document.getElementById('btn-retry-wrong');
    if (retryWrongBtn) {
      retryWrongBtn.addEventListener('click', () => {
        const retryQuestions = [...wrongList];
        this._shuffle(retryQuestions);
        this.state = { questions: retryQuestions, current: 0, correct: 0, wrongList: [], answered: false };
        this._renderPlay(container);
      });
    }

    document.getElementById('btn-retry-all').addEventListener('click', () => {
      const allQuestions = [...this.state.questions];
      this._shuffle(allQuestions);
      this.state = { questions: allQuestions, current: 0, correct: 0, wrongList: [], answered: false };
      this._renderPlay(container);
    });

    document.getElementById('btn-result-home').addEventListener('click', () => {
      window.location.hash = '#/';
    });
  }
};
