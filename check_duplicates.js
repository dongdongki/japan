const fs = require('fs');

// 데이터 읽기
const filePath = 'c:\\Users\\judek\\AndroidStudioProjects\\MyApplication6\\app\\src\\main\\assets\\japanese_words_data.json';
const data = JSON.parse(fs.readFileSync(filePath, 'utf-8'));

console.log('=== 중복 검사 시작 ===\n');

// 161-180번 단어들
const newWords = data.filter(item => item.id >= 161 && item.id <= 180);
// 나머지 단어들
const otherWords = data.filter(item => item.id < 161 || item.id > 180);

console.log(`새로 추가된 단어: ${newWords.length}개`);
console.log(`기존 단어: ${otherWords.length}개\n`);

let duplicateFound = false;

// 각 새 단어에 대해 중복 검사
newWords.forEach(newWord => {
  const duplicates = otherWords.filter(other =>
    other.word === newWord.word ||
    (other.reading === newWord.reading && other.meaning === newWord.meaning)
  );

  if (duplicates.length > 0) {
    duplicateFound = true;
    console.log(`[중복 발견!]`);
    console.log(`  새 단어: ID ${newWord.id} - ${newWord.word} (${newWord.reading}) - ${newWord.meaning}`);
    duplicates.forEach(dup => {
      console.log(`  ↔ 기존: ID ${dup.id} - ${dup.word} (${dup.reading}) - ${dup.meaning}`);
    });
    console.log();
  }
});

if (!duplicateFound) {
  console.log('✓ 중복된 단어가 없습니다!\n');
} else {
  console.log('=== 중복 요약 ===');
  console.log('위에 표시된 중복 단어들을 확인해주세요.\n');
}

// 추가: 161-180번 단어 목록 출력
console.log('=== 161-180번 단어 목록 ===');
newWords.forEach(word => {
  console.log(`${word.id}: ${word.word} (${word.reading}) - ${word.meaning}`);
});
