# Remittance 과제 제출 README

## 1. 실행 방법

### 1.1 Docker Compose로 전체 실행 (MySQL + App, 빌드 시 테스트 포함)
```
docker compose up --build
```

- Swagger 확인
http://localhost:8080/swagger-ui.html

## 2. 프로젝트 개요

본 프로젝트는 간단한 송금(Remittance) 서비스를 구현한 과제입니다.

- 계좌 생성 및 조회
- 입금 / 출금
- 계좌 간 송금
- 거래 내역(원장) 기록 및 조회
- 동시성 환경에서의 잔액/한도 정합성 보장

API 명세서: 


## 3. 기술 스택

- Java 21
- Spring Boot 4.0.1
- Spring Data JPA (Hibernate)
- MySQL 8.4 (Docker)
- Gradle (멀티모듈)
- springdoc-openapi (Swagger UI)


## 4. 프로젝트 구조

- remittance-domain  
  도메인 모델, 비즈니스 규칙, enum, 도메인 예외

- remittance-application  
  유스케이스(Application Service), DTO

- remittance-infra  
  JPA Entity, Repository, DB 접근 구현체

- remittance-presentation  
  REST Controller, Request/Response, 예외 처리

- remittance-api  
  Spring Boot 실행 모듈, 설정

## 5. ERD 및 API 명세
<img width="825" height="646" alt="Untitled" src="https://github.com/user-attachments/assets/7f009e3d-788c-459f-82af-769ab52e231e" />

### API 명세 : https://github.com/wkdan/Jang-Muyeong_backend/wiki/API-%EB%AA%85%EC%84%B8%EC%84%9C

## 6. 기능 구현 사항

### 6.1 계좌

- 계좌 생성
- 계좌 조회 (잔액 포함)
- 입금
- 출금

### 6.2 송금

- 계좌 간 송금
- 잔액 부족 시 송금 실패
- 동일 계좌로의 송금 방지

### 6.3 거래 내역 (Ledger)

- 모든 금액 변동(입금/출금/송금)에 대해 거래 내역 기록
- 거래 시점의 잔액(balance_after) 저장
- 계좌 기준 거래 내역 조회 가능


## 7. 동시성 및 정합성 고려

본 프로젝트는 동시 요청 환경에서 데이터 정합성을 유지하기 위해 DB 락 기반 설계를 적용했습니다.

### 7.1 계좌 잔액 동시성

- 입금, 출금, 송금 시 계좌를 PESSIMISTIC_WRITE(SELECT FOR UPDATE)로 조회
- 잔액 검증 → 변경 → 저장 과정이 하나의 트랜잭션 내에서 직렬화됨
- 동일 계좌에 대한 동시 요청에서도 잔액 불일치 방지

### 7.2 일일 한도(Daily Limit) 동시성

- 일일 한도 엔티티 조회/생성 시에도 락 적용
- 동일 날짜에 동시 요청이 들어와도
  - 중복 row 생성 방지
  - 누적 금액 계산 오류 방지

## 8. 예외 및 검증 정책

- 계좌가 존재하지 않을 경우: 404 (ACCOUNT_NOT_FOUND)
- 잔액 부족, 동일 계좌 송금 등 비즈니스 규칙 위반: 400
- 요청 값 검증 실패(@Valid): 400
- JSON 파싱 오류: 400
- 그 외 서버 오류: 500

모든 에러는 공통 에러 응답 포맷으로 반환됩니다.
