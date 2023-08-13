# 멱등성 매커니즘 구현 해보기

## 멱등성이란?

- [MDN Web Docs 용어 사전: 웹 용어 정의 - 멱등성](https://developer.mozilla.org/ko/docs/Glossary/Idempotent)
- [Toss - 멱등성이 뭔가요?](https://blog.tossbusiness.com/articles/dev-1)
- [카카오페이 - MSA 환경에서 네트워크 예외를 잘 다루는 방법](https://tech.kakaopay.com/post/msa-transaction/)
- [위시켓 - 프로그래밍 용어 '멱등성(Idempotence)'알아보기](https://yozm.wishket.com/magazine/detail/2106/)
- [NHN Cloud Meetup - 지속 가능한 소프트웨어를 위한 코딩 방법 - 마지막 맺음말.](https://meetup.nhncloud.com/posts/218)
- [HTTP 메소드의 멱등성, 그리고 안전한 메서드](https://hudi.blog/http-method-idempotent/)

## 멱등성을 테스트 하기 위한 방법

- PUT, DELETE 요청시 idempotent 헤더를 추가하여 테스트
 - 부하 테스트 용으로 증감에 대한 내용 테스트 추가