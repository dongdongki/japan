const KanaList = {
  render(container, kanaType, data) {
    const kanaData = data[kanaType];
    const title = kanaType === 'hiragana' ? '히라가나' : '카타카나';
    const rowNames = Object.keys(kanaData);

    let html = `<div class="kana-page"><h2>${title}</h2>`;

    for (const rowName of rowNames) {
      const chars = kanaData[rowName];
      const colsClass = chars.length <= 3 ? 'cols-3' : '';

      html += `
        <div class="row-group">
          <div class="row-title">${rowName}</div>
          <div class="kana-grid ${colsClass}">
            ${chars.map(c => `
              <div class="kana-card">
                <div class="kana-char">${c.kana}</div>
                <div class="kana-romaji">${c.romaji}</div>
                <div class="kana-kor">${c.kor}</div>
              </div>
            `).join('')}
          </div>
        </div>`;
    }

    html += '</div>';
    container.innerHTML = html;
  }
};
