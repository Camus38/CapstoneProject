# CapstoneProject

# 팀원
* 한승훈 : 게시판 및 Firebase 데이터베이스 구축
* 이건우 : 꿀팁 Fragment 제작
* 김채민 : UI 디자인 및 어플의 전체적인 디자인
* 김현종 : 날씨, 미세먼지, 위치정보 API

# 소개
* 주 타겟은 '자취생'이며 어플의 용도는 커뮤니티의 성격을 가진다.
* 4개(게시판,날씨,할일,팁)의 화면을 가진다.
* Firebase를 기반으로 유저의 UID를 Firebase DB에 저장하여 자신이 쓴 글과 할 일을 구별한다.

# 각 Fragment 소개
* 게시판

![게시판](https://user-images.githubusercontent.com/101079549/170963014-b91cd1b8-09b2-4b9f-8eb6-caa5e0db96c7.JPG)

회원가입시 입력하는 닉네임을 통해 글을 작성하며 유저의 모든 정보는 UID를 기반으로 Firebase에 저장되어 각 유저를 구별한다.\
각 게시글은 우측하단의 좋아요,사진,댓글의 수를 나타낸다.

------
* 날씨

![날씨](https://user-images.githubusercontent.com/101079549/170964007-5322a1cc-0555-4530-b09a-352110c632f3.JPG)

기상청에서 배포하는 '날씨정보,미세먼지'를 Retrofit2로 배포되는 데이터 중 원하는 데이터만 화면에 출력한다.\
'날씨정보'의 데이터를 받기위해서는 Grid X,Y 좌표와 현재 날짜 및 시간이 필요하며 '미세먼지'데이터는 실시간으로 데이터를 갱신하고 있기에 원하는 지역의 이름만 제공하면 데이터를 수신할 수 있다.\
GPS를 통해 얻게되는 X,Y 좌표를 지명으로 바꾸는 '역 GeoCoding'을 사용하여 어플 사용자의 위치를 지명으로 바꾸어 화면의 출력한다.\
맨 아래 추천 착장 기능은 수신받은 날씨 데이터 중 온도 값에 따라 다르게 출력하도록 했다.

---
* 할 일

![Todo](https://user-images.githubusercontent.com/101079549/170966116-00630111-5ae8-4d82-bff6-4ea3f6dabe28.JPG)

할일 Fragment의 목록 데이터는 각 유저의 UID 아래로 저장되기에 다른 유저의 목록은 출력되지않는다.\
우측 하단의 버튼으로 목록을 작성하며 완료한 목록은 회색 처리 할 수 있고 혹은 각 목록별 휴지통 아이콘을 클릭해 삭제가 가능하다.

---
* 꿀팁

![꿀팁1](https://user-images.githubusercontent.com/101079549/170967186-64bf961b-5865-4a3a-923a-30fdcb31fd43.JPG)

자취생활이나 도움이 될만한 주제를 카테고리 별로 나누었다.

![꿀팁2](https://user-images.githubusercontent.com/101079549/170967394-4e3f1070-948d-4aa9-aecc-db6ed6a80c95.JPG)

하나를 클릭할 시 세부 카테고리로 넘어간다.

![꿀팁3](https://user-images.githubusercontent.com/101079549/170967416-07cdbaee-6a9e-49ce-8273-52b9c9b2de97.JPG)

클릭시 외부 URL과 연결되어 해당 정보를 담고있는 화면으로 넘어간다.\
우측에는 이 정보가 도움이 되었는지를 알 수 있도록 '좋아요'를 클릭하도록 설정했다.

