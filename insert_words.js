const fs = require('fs');

// 새로 추가할 단어 목록
const newWords = [
  {
    "id": 161,
    "word": "ある",
    "reading": "ある",
    "meaning": "있다 (사물)",
    "example_jp": "机の上に本があります。",
    "example_kr": "책상 위에 책이 있습니다."
  },
  {
    "id": 162,
    "word": "いる",
    "reading": "いる",
    "meaning": "있다 (사람/동물)",
    "example_jp": "あそこに猫がいます。",
    "example_kr": "저기에 고양이가 있습니다."
  },
  {
    "id": 163,
    "word": "する",
    "reading": "する",
    "meaning": "하다",
    "example_jp": "毎日日本語の勉強をします。",
    "example_kr": "매일 일본어 공부를 합니다."
  },
  {
    "id": 164,
    "word": "来る",
    "reading": "くる",
    "meaning": "오다",
    "example_jp": "友達が家にきます。",
    "example_kr": "친구가 집에 옵니다."
  },
  {
    "id": 165,
    "word": "食べる",
    "reading": "たべる",
    "meaning": "먹다",
    "example_jp": "一緒にりんごを食べましょう。",
    "example_kr": "같이 사과를 먹읍시다."
  },
  {
    "id": 166,
    "word": "飲む",
    "reading": "のむ",
    "meaning": "마시다",
    "example_jp": "コーヒーを飲みますか。",
    "example_kr": "커피를 마실래요?"
  },
  {
    "id": 167,
    "word": "行く",
    "reading": "いく",
    "meaning": "가다",
    "example_jp": "明日学校に行きます。",
    "example_kr": "내일 학교에 갑니다."
  },
  {
    "id": 168,
    "word": "帰る",
    "reading": "かえる",
    "meaning": "돌아가다/귀가하다",
    "example_jp": "夜１０時にうちに帰ります。",
    "example_kr": "밤 10시에 집에 돌아갑니다."
  },
  {
    "id": 169,
    "word": "寝る",
    "reading": "ねる",
    "meaning": "자다",
    "example_jp": "早く寝てください。",
    "example_kr": "빨리 주무세요."
  },
  {
    "id": 170,
    "word": "起きる",
    "reading": "おきる",
    "meaning": "일어나다",
    "example_jp": "毎朝６時に起きます。",
    "example_kr": "매일 아침 6시에 일어납니다."
  },
  {
    "id": 171,
    "word": "見る",
    "reading": "みる",
    "meaning": "보다",
    "example_jp": "週末に映画を見ます。",
    "example_kr": "주말에 영화를 봅니다."
  },
  {
    "id": 172,
    "word": "聞く",
    "reading": "きく",
    "meaning": "듣다/묻다",
    "example_jp": "音楽を聞くのが好きです。",
    "example_kr": "음악을 듣는 것을 좋아합니다."
  },
  {
    "id": 173,
    "word": "話す",
    "reading": "はなす",
    "meaning": "말하다/대화하다",
    "example_jp": "日本語で話しましょう。",
    "example_kr": "일본어로 말해봅시다."
  },
  {
    "id": 174,
    "word": "言う",
    "reading": "いう",
    "meaning": "말하다",
    "example_jp": "名前を言ってください。",
    "example_kr": "이름을 말해 주세요."
  },
  {
    "id": 175,
    "word": "読む",
    "reading": "よむ",
    "meaning": "읽다",
    "example_jp": "この本を読みましたか。",
    "example_kr": "이 책을 읽었습니까?"
  },
  {
    "id": 176,
    "word": "書く",
    "reading": "かく",
    "meaning": "쓰다",
    "example_jp": "ここに住所を書いてください。",
    "example_kr": "여기에 주소를 써 주세요."
  },
  {
    "id": 177,
    "word": "会う",
    "reading": "あう",
    "meaning": "만나다",
    "example_jp": "明日友達に会います。",
    "example_kr": "내일 친구를 만납니다."
  },
  {
    "id": 178,
    "word": "買う",
    "reading": "かう",
    "meaning": "사다",
    "example_jp": "コンビニでパンを買いました。",
    "example_kr": "편의점에서 빵을 샀습니다."
  },
  {
    "id": 179,
    "word": "分かる",
    "reading": "わかる",
    "meaning": "알다/이해하다",
    "example_jp": "はい、よく分かりました。",
    "example_kr": "네, 잘 알겠습니다."
  },
  {
    "id": 180,
    "word": "作る",
    "reading": "つくる",
    "meaning": "만들다",
    "example_jp": "母が料理を作ります。",
    "example_kr": "어머니가 요리를 만듭니다."
  }
];

// 기존 데이터 읽기
const filePath = 'c:\\Users\\judek\\AndroidStudioProjects\\MyApplication6\\app\\src\\main\\assets\\japanese_words_data.json';
const data = JSON.parse(fs.readFileSync(filePath, 'utf-8'));

console.log(`기존 데이터 개수: ${data.length}`);
console.log(`기존 마지막 ID: ${data[data.length - 1].id}`);

// 161번 이전과 이후로 분리
const before161 = data.filter(item => item.id < 161);
const after161 = data.filter(item => item.id >= 161);

console.log(`161 이전 개수: ${before161.length}`);
console.log(`161 이후 개수: ${after161.length}`);

// 161 이후 단어들의 ID를 20씩 증가
after161.forEach(item => {
  item.id += 20;
});

// 모두 합치기
const result = [...before161, ...newWords, ...after161];

// ID 순으로 정렬
result.sort((a, b) => a.id - b.id);

console.log(`\n새로운 데이터 개수: ${result.length}`);
console.log(`새로운 마지막 ID: ${result[result.length - 1].id}`);

// 파일에 저장
fs.writeFileSync(filePath, JSON.stringify(result, null, 2), 'utf-8');

console.log('\n완료! 파일이 업데이트되었습니다.');
console.log('161-180번: 새로 추가된 단어들');
console.log('181번부터: 기존 161번이었던 단어들');
