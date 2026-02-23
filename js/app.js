const App = {
  data: null,

  async init() {
    await this.loadData();
    this.bindNavigation();
    this.route();
  },

  async loadData() {
    const res = await fetch('data/data.json');
    this.data = await res.json();
  },

  bindNavigation() {
    window.addEventListener('hashchange', () => this.route());
  },

  route() {
    const hash = window.location.hash || '#/hiragana';
    const container = document.getElementById('app');

    document.querySelectorAll('.nav-link').forEach(link => {
      link.classList.toggle('active', link.getAttribute('href') === hash);
    });

    switch (hash) {
      case '#/katakana':
        KanaList.render(container, 'katakana', this.data);
        break;
      case '#/quiz':
        Quiz.renderSetup(container, this.data);
        break;
      default:
        KanaList.render(container, 'hiragana', this.data);
        break;
    }
  }
};

document.addEventListener('DOMContentLoaded', () => App.init());
