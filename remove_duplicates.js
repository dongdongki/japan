const fs = require('fs');

// 데이터 읽기
const filePath = 'c:\\Users\\judek\\AndroidStudioProjects\\MyApplication6\\app\\src\\main\\assets\\japanese_words_data.json';
const data = JSON.parse(fs.readFileSync(filePath, 'utf-8'));

console.log('=== 중복 제거 시작 ===\n');
console.log(`전체 단어 개수: ${data.length}개\n`);

// 161-180번 단어들 (새로 추가한 단어들 - 보호해야 함)
const newWords = data.filter(item => item.id >= 161 && item.id <= 180);
console.log(`보호할 단어 (161-180): ${newWords.length}개`);

// 삭제할 ID 목록
const duplicateIds = [257, 391, 439, 134, 571, 567, 59, 139, 398, 589, 113, 426, 9, 623, 342, 370, 290];
console.log(`삭제할 중복 단어 ID: ${duplicateIds.join(', ')}\n`);

// 중복 단어 제거
const filtered = data.filter(item => !duplicateIds.includes(item.id));

console.log(`중복 제거 후: ${filtered.length}개 (${data.length - filtered.length}개 삭제됨)\n`);

// ID 재정렬 (1부터 순차적으로)
filtered.sort((a, b) => a.id - b.id);

filtered.forEach((item, index) => {
  const oldId = item.id;
  item.id = index + 1;
  if (oldId >= 161 && oldId <= 180) {
    console.log(`보호된 단어 재배치: ${oldId} → ${item.id} - ${item.word} (${item.reading})`);
  }
});

console.log(`\nID 재정렬 완료: 1 ~ ${filtered.length}\n`);

// 파일에 저장
fs.writeFileSync(filePath, JSON.stringify(filtered, null, 2), 'utf-8');

console.log('✓ 완료! 파일이 업데이트되었습니다.');
console.log(`최종 단어 개수: ${filtered.length}개`);
