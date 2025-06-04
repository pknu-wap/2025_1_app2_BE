# 🚕 같이타요 - Backend

## Introduction
> 목적지가 같은 사람끼리 함께 택시를 타고, 요금을 나누는 스마트한 합승 모임 플랫폼

## Stack


[![My Skills](https://skillicons.dev/icons?i=spring,aws,mysql,redis)](https://skillicons.dev)


## Team

| ![프로필1](https://avatars.githubusercontent.com/u/140076739?v=4) | ![프로필2](https://avatars.githubusercontent.com/u/66681282?v=4)|
|----------------------------------------------------------------|---|
| 윤회성                                                            |이은호|
|![Static Badge](https://img.shields.io/badge/squareCaaat-181717?style=flat-square&logo=github&logoColor=white)|![Static Badge](https://img.shields.io/badge/lepitaaar-181717?style=flat-square&logo=github&logoColor=white)
|

## Role

- 윤회성
  - 파티 CRUD 구현, **Facade 패턴**을 이용한 서비스 분리
  - 위치 기반 서비스 활용을 위한 위치 정보 관리 및 MySQL 내장 제공 `ST_DISTANCE_SPHERE` 함수를 통한 위치 검색 제공
  - STOMP 프로토콜 사용해서 **실시간 파티 참가 요청 처리**
    - **비관적 락**을 사용해 동시에 요청이 수락되는 것을 방지
  - 결산 및 최종 정산 서비스 구현
  - 파티, 정산 등의 쿼리 최적화를 위해 `@Query` 직접 작성 및 `Fetch Join` 적극 활용
    - `MultipleFetchBagException` 방지하고자 하나의 `Fetch Join` 및 나머지는 `@BatchSize` 활용하거나 로컬 캐싱을 활용해 쿼리 최적화 수행
- 이은호
  - JWT Access/Refresh 토큰 기반 회원 인증 및 Redis 활용 토큰 재발급 서비스 구현
  - 구글 OAuth를 사용 및 `GoogleIdTokenVerifier` 사용해 IdToken 검증 후 이메일 검증 
  - 회원 정보 CRUD 서비스 제작 및 리뷰 서비스, 태그 관리 서비스 제작
  - SMS 인증 사용해서 무분별한 계정 생성 방지 및 악성 유저 예방

# Feature

## 1. 구글 로그인 및 SMS 인증

- SKT, KT, LG U+ 통신사에 따른 인증
- JWT 토큰 사용
- 웹소켓 custom interceptor 구현으로 인증

## 2. 목적지로 파티 검색

- 가고자하는 목적지에 유사한 파티 검색

## 3. 파티 참가 및 파티 화면

## 4. 금액 기입 및 결산


