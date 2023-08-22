# 멱등성 매커니즘 구현 해보기

- [멱등성 고민해보기](https://github.com/SeokRae/idempotence-kata/tree/master/idempotence-api)

## 멱등성이란?

- [MDN Web Docs 용어 사전: 웹 용어 정의 - 멱등성](https://developer.mozilla.org/ko/docs/Glossary/Idempotent)
- [Toss - 멱등성이 뭔가요?](https://blog.tossbusiness.com/articles/dev-1)
- [카카오페이 - MSA 환경에서 네트워크 예외를 잘 다루는 방법](https://tech.kakaopay.com/post/msa-transaction/)
- [위시켓 - 프로그래밍 용어 '멱등성(Idempotence)'알아보기](https://yozm.wishket.com/magazine/detail/2106/)
- [NHN Cloud Meetup - 지속 가능한 소프트웨어를 위한 코딩 방법 - 마지막 맺음말.](https://meetup.nhncloud.com/posts/218)
- [HTTP 메소드의 멱등성, 그리고 안전한 메서드](https://hudi.blog/http-method-idempotent/)

## 멱등성 구현 코드

- 요청 헤더에 특정 키를 추가하여 멱등성을 구현

```text
├── main
│         ├── java
│         │         └── com
│         │             └── example
│         │                 └── idempotence
│         │                     ├── IdempotenceApiApplication.java
│         │                     ├── application
│         │                     │         └── item
│         │                     │             ├── controller
│         │                     │             │         └── IdempotentLocalCachedController.java
│         │                     │             ├── domain
│         │                     │             │         ├── Id.java
│         │                     │             │         ├── Item.java
│         │                     │             │         ├── Stock.java
│         │                     │             │         └── StockRepository.java
│         │                     │             └── service
│         │                     │                 ├── ItemService.java
│         │                     │                 └── StockService.java
│         │                     └── core
│         │                         └── config
│         │                             ├── CacheConfig.java
│         │                             ├── RedissonConfig.java
│         │                             └── RetryConfig.java
│         └── resources
│             ├── application.yml
│             └── redisson-config.yml
└── test
    └── java
        └── com
            └── example
                └── idempotence
                    └── application
                        └── item
                            └── controller
                            │   ├── IdempotentLocalCachedControllerDecreaseLoadTest.java    // (1) 재고 감소 부하 테스트
                            │   ├── IdempotentLocalCachedControllerDeleteTest.java          // (2) 재고 삭제 테스트 (멱등성) 
                            │   ├── IdempotentLocalCachedControllerIncreaseLoadTest.java    // (3) 재고 증가 부하 테스트
                            │   ├── IdempotentLocalCachedControllerNoHeaderTest.java        // (4) 재고 감소 테스트 (멱등성 헤더 제외)
                            │   ├── IdempotentLocalCachedControllerPutTest.java             // (5) 재고 수정 테스트 (멱등성)
                            │   └── IdempotentLocalCachedControllerUnitTest.java            // (6) 재고 수정 단위 테스트
                            └── service
                                ├── StockServiceOptimisticLockExceptionTest.java            // (7) 재고 감소 낙관적 락 예외 테스트
                                ├── StockServiceOptimisticLockRetryTest.java                // (8) 재고 감소 낙관적 락 재시도 테스트
                                ├── StockServiceOptimisticLockSingleThreadTest.java         // (9) 재고 감소 낙관적 락 단일 스레드 테스트
                                ├── StockServicePessimisticLockTest.java                    // (10) 재고 감소 비관적 락 테스트
                                └── StockServiceTest.java                                   // (11) CRUD 테스트
```

- [IdempotentLocalCachedController](https://github.com/SeokRae/idempotence-kata/blob/master/idempotence-api/src/main/java/com/example/idempotence/application/item/controller/IdempotentLocalCachedController.java)

## 멱등성을 테스트 하기 위한 방법

- PUT, DELETE 요청시 idempotent 헤더를 추가하여 테스트
    - 부하 테스트 용으로 증감에 대한 내용 테스트 추가

- (1): [IdempotentLocalCachedControllerDecreaseLoadTest.java](https://github.com/SeokRae/idempotence-kata/blob/master/idempotence-api/src/test/java/com/example/idempotence/application/item/controller/IdempotentLocalCachedControllerDecreaseLoadTest.java)
    - put 멱등성 수량 감소 부하 테스트 (멱등성 헤더 포함)
- (2): [IdempotentLocalCachedControllerDeleteTest.java](https://github.com/SeokRae/idempotence-kata/blob/master/idempotence-api/src/test/java/com/example/idempotence/application/item/controller/IdempotentLocalCachedControllerDeleteTest.java)
    - delete 멱등성 삭제 테스트 (멱등성 헤더 포함)
- (3): [IdempotentLocalCachedControllerIncreaseLoadTest.java](https://github.com/SeokRae/idempotence-kata/blob/master/idempotence-api/src/test/java/com/example/idempotence/application/item/controller/IdempotentLocalCachedControllerIncreaseLoadTest.java)
    - put 멱등성 수량 증가 부하 테스트 (멱등성 헤더 포함)
- (4): [IdempotentLocalCachedControllerNoHeaderTest.java](https://github.com/SeokRae/idempotence-kata/blob/master/idempotence-api/src/test/java/com/example/idempotence/application/item/controller/IdempotentLocalCachedControllerNoHeaderTest.java)
    - 수량 감소 테스트 (멱등성 헤더 제외)
- (5): [IdempotentLocalCachedControllerPutTest.java](https://github.com/SeokRae/idempotence-kata/blob/master/idempotence-api/src/test/java/com/example/idempotence/application/item/controller/IdempotentLocalCachedControllerPutTest.java)
    - 수량 수정 테스트 (멱등성 헤더 포함)
- (6): [IdempotentLocalCachedControllerUnitTest.java](https://github.com/SeokRae/idempotence-kata/blob/master/idempotence-api/src/test/java/com/example/idempotence/application/item/controller/IdempotentLocalCachedControllerUnitTest.java)
    - 수량 수정 단위(Mocking) 테스트

- (7): [StockServiceOptimisticLockExceptionTest.java](https://github.com/SeokRae/idempotence-kata/blob/master/idempotence-api/src/test/java/com/example/idempotence/application/item/service/StockServiceOptimisticLockExceptionTest.java)
  - 재고 감소 낙관적 락 예외 테스트
    - 재고 감소에 대한 값 검증은 불가
    - 단지 경합 발생 시 OptimisticLockingFailureException 발생하는지에 대한 테스트
- (8): [StockServiceOptimisticLockRetryTest.java](https://github.com/SeokRae/idempotence-kata/blob/master/idempotence-api/src/test/java/com/example/idempotence/application/item/service/StockServiceOptimisticLockRetryTest.java)
  - 재고 감소 낙관적 락 재시도 테스트
    - 재시도 횟수에 대한 검증
- (9): [StockServiceOptimisticLockSingleThreadTest.java](https://github.com/SeokRae/idempotence-kata/blob/master/idempotence-api/src/test/java/com/example/idempotence/application/item/service/StockServiceOptimisticLockSingleThreadTest.java)
  - 재고 감소 낙관적 락 단일 스레드 테스트
    - 단일 스레드 접근시 값에 대한 변경을 확인하기 위함
- (10): [StockServicePessimisticLockTest.java](https://github.com/SeokRae/idempotence-kata/blob/master/idempotence-api/src/test/java/com/example/idempotence/application/item/service/StockServicePessimisticLockTest.java)
  - 재고 감소 비관적 락 테스트
    - 재고 감소에 대한 값 검증 가능
- (11): [StockServiceTest.java](https://github.com/SeokRae/idempotence-kata/blob/master/idempotence-api/src/test/java/com/example/idempotence/application/item/service/StockServiceTest.java)
    - 그냥 CRUD 테스트

헤더 값으로 관리 하는 곳들이 있어서 일단은 생각나는대로 구현해봤지만, 로컬 캐시로 구현하여 추후 Redis와 같은 글로벌 캐시로 구현 필요

구현부는 멱등성 구현만 고려만 했기 때문에, 멀티 스레드 환경에서, 분산 시스템 환경에서 동시성 처리를 위한 방법도 자연스레 따라와야 함을 느낌

따라서, 동시성에 대해서 예제를 어떻게 만들어봐야 할까 잠깐 고민해보고 정리

## 동시성 처리를 위한 방법

- Monolithic Application & Databasse
    - DB Lock
        - Row level Lock
            - 특정 레코드 락으로 최소한의 병목 현상을 유발
        - Isolation level
            - 격리 수준을 적절하게 설정하여 다른 트랜잭션과의 상호작용을 제어
    - JPA 사용시
        - 낙관적 락(Optimistic Lock)
            - @Version 어노테이션을 통해 엔티티에 버전 컬럼을 관리
            - 트랜잭션 충돌이 자주 발생하지 않는 경우 사용
            - 성능 향상
                - 레코드를 물리적으로 잠그는 것이 아니기 때문에, 동시에 여러 트랜잭션을 처리할 수 있으며, 성능 향상을 기대할 수 있음
                - 비관적 락과 달리 다른 트랜잭션이 대기 상태에 빠지지 않으므로 병목 현상을 줄일 수 있음
            - 충돌 감지
                - 트랜잭션 충돌이 발생하면 예외가 발생하므로, 충돌을 즉시 감지하고 적절한 조치를 취할 수 있음
            - 읽기 작업에 최적화
                - 비관적 락에 비해서, 읽기 작업에서 락 경쟁으로 인한 지연이 없어, 읽기 성능이 향상
            - 낙관적 락의 문제점은 없을까?
                - 충돌 발생 시 예외에 대한 후처리 필요
                    - 낙관적 락은 다른 트랜잭션이 동일한 데이터를 수정했는지 확인 하고, 충돌이 확인되면 예외를 발생시킴
                    - 충돌 해결을 위한 재시도 로직이 필요, 코드의 복잡성 증가
                - 높은 충돌 비율에 대한 문제
                    - 동시에 같은 데이터를 수정하는 트랜잭션이 많은 경우, 낙관적 락을 사용 시 충돌 비율이 높아져 성능 저하 발생
                - 롤백 비용
                    - 롤백 후 재시도를 통해 충돌을 해결할 수 있지만, 재시도를 위한 리소스 및 시간이 추가로 소모됨
                    - 충돌이 빈번하게 발생한다? 비용이 적지 않을 것이다.
                - 버전 관리의 복잡성
                    - JPA 사용할 때 @Version 컬럼을 추가
            - 낙관적 락에 대한 검증을 어떻게 해?
        - 비관적 락(Pessimistic Lock)
            - 엔티티나 쿼리에 설정
            - @Lock 어노테이션을 통해 엔티티에 락을 걸어 다른 트랜잭션의 접근을 제어
            - 특정 데이터에 대한 동시 접근을 제한하여 충돌을 방지하고 데이터의 일관성을 유지하는데 용이
            - 많은 트랜잭션을 처리하려는 경우 성능 저하가 발생할 수 있음
                - 트랜잭션의 범위가 넓어질수록 락이 걸리는 시간이 길어짐
                - 다른 트랜잭션의 접근을 제한하기 때문에 병목 현상이 발생할 수 있음
            - 데드락 가능성
                - 둘 이상의 트랜잭션이 서로 대기하게 되면 데드락 발생
                - 예를 들어, 트랜잭션 A가 테이블 A의 레코드 1을 락을 걸고 테이블 B의 레코드 2를 락을 걸고, 서로 반대의 레코드에 접근하려고하면 데드락 상태에 빠질 수 있음
                - 데드락을 방지하기 위해 트랜잭션의 범위를 최소화하고, 트랜잭션의 범위가 넓어지는 경우 락의 순서를 정하는 것이 좋음
                - 데드락이 발생하면 트랜잭션을 롤백하고 재시도하는 방법을 사용할 수 있음
- 분산 락
    - 여러 노드에서 동시에 일어나는 트랜잭션을 조정해야 할 때 사용
    - 여러 서버가 동유 캐시에 대한 동시 접근을 제어해야 하는 경우, 분산 락 사용
    - 분산락 구현 방법
        - 중앙 집중형 락 서버: 모든 서버가 락을 얻기 위해 특정 락 서버에 요청하고 락 서버가 락의 상태를 관리하는 방식
        - 데이터베이스 기반 락: 락 정보를 데이터베이스에 저장하고 모든 서버가 해당 데이터베이스를 참조하여 락상태를 판단하는 방식
        - 분산 시스템 기반 락: Zookeeper와 같은 분산 시스템을 사용하여 락을 관리하는 방식
    - Redis를 이용한 분산락 구현
        - [Distributed Locks with Redis](https://redis.io/docs/manual/patterns/distributed-locks/)
        - [Redisson - Distributed locks and synchronizers](https://github.com/redisson/redisson)
    - DB를 위한 분산락 구현
      - [MySQL을 이용한 분산락으로 여러 서버에 걸친 동시성 관리](https://techblog.woowahan.com/2631/)


## 멱등성 하다가 왜 동시성에 대한 내용을 다루는거야?

멱등성, 동시성의 개념이 긴밀하게 연관되어 있다고 생각하진 않았다.

하지만 멱등성은 동시성 환경에서 중요할 수 있다고 생각했다. 예를 들어 동시성에 여러 요청이 동일한 작업을 수행하려고 할 때

멱등성이 보장된다면, 해당 작업의 결과에 대한 예측 가능성이 높아 지지 않을까 생각된다.

마치 동시에 여러 사용자가 같은 데이터를 업데이트 하는 상황이 있다고 가정할 때, 해당 업데이트에 대한 연산이 멱등하다면 동시성 문제로 인한 데이터 일관성 문제를 줄일 수 있지 않을까?

그래서 결론적으로, 멱등성 자체로는 동시성과 직접적인 연관은 없지만, 동시성을 다룰 때 멱등성이 잘 보장된 연산이나 기능이

복잡성을 줄이고, 예측 가능한 결과를 가져오는데 도움이 될 수 있을 것이라 생각한다.
