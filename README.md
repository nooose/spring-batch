# Spring Batch 5 (with Spring Boot 3.1.3)

# 아키텍처
배치 핵심 패턴은 3가지로 볼 수 있다.
- `Read`: DB, 파일, 큐에서 다량의 데이터를 조회
- `Process`: 특정 방법으로 데이터를 가공
- `Write`: 데이터를 수정된 양식으로 다시 저장

`ETL`(Extract Transform Load)과 같은 개념으로 볼 수 있다.

## Application
- 스프링 배치 프레임워크를 통해 개발자가 만든 모든 배치 Job과 Custom 코드를 포함
- 개발자는 업무로직의 구현에만 집중하고 공통적인 기반 기술은 프레임워크가 담당하게 함
## Batch Core
- Job을 실행, 모니터링, 관리하는 API로 구성
- JobLauncher, Job, Step, Flow 등
## Batch Infrastructure
- Application, Core 모두 Infrastructure 위에서 동작
- Job 실행의 흐름과 처리를 위한 틀을 제공
- Reader, Processor, Writer, Skip, Retry 등

**배치 시나리오**
- 배치 프로세스를 주기적으로 커밋
- 동시 다발적인 Job의 배치 처리, 대용량 병렬 처리
- 실패 후 수동 또는 스케줄링에 의한 재시작
- 의존관계가 있는 Step 여러 개를 순차적으로 처리
- 조건적 Flow 구성을 통해 체계적이고 유연한 배치 모델 구성
- 반복, 재시도, Skip 처리

## 스키마
- `/org/springframework/batch/core/schema-*.sql` 제공
- `BatchDataSourceScriptDatabaseInitializer` 가 동작되면 실행되는데 옵션에 따라 달라진다.
  - `ALWAYS`: 스크립트 항상 실행
  - `EMBEDDED`: 내장 DB일 때만 실행, 기본값
  - `NEVER`: 스크립트 항상 실행하지 않음
    - 이러한 경우 수동으로 제공된 스크립트를 사용하여 스키마를 생성한다.

### Job 테이블
#### `BATCH_JOB_INSTANCE`
- Job이 실행될 대 JobInstance 정보가 저장되며 `job_name`과 `job_key`(hash 값)로 하나의 데이터가 저장
- 동일한 `job_name`과 `job_key`로 중복 저장될 수 없다.
#### `BATCH_JOB_EXECUTION`
- Job의 실행정보가 저장되며 Job 생성, 시작, 종료 시간, 실행상태, 메시지 등을 관리
- `BATCH_JOB_INSTANCE` 테이블과 N:1 관계
```mysql
CREATE TABLE BATCH_JOB_EXECUTION  (
	JOB_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
	VERSION BIGINT  , # 업데이트 될 때마다 1씩 증가
	JOB_INSTANCE_ID BIGINT NOT NULL, 
	CREATE_TIME DATETIME(6) NOT NULL, # 실행(Execution)이 생성된 시점 기록
	START_TIME DATETIME(6) DEFAULT NULL , # 실행(Execution)이 시작된 시점 기록
	END_TIME DATETIME(6) DEFAULT NULL , # 실행이 종료된 시점을 기록하며 Job 실행 도중 오류가 발생해서 Job이 중단된다면 저장되지 않을 수 있음 
	STATUS VARCHAR(10) , # 실행 상태(BatchStatus) 저장 (COMPLETED, FAILED, STOPPED, ...)
	EXIT_CODE VARCHAR(2500) , # 실행 종료 코드(ExitStatus)를 저장 (COMPLETED, FAILED, ...)
	EXIT_MESSAGE VARCHAR(2500) , # Status가 실패일 때 실패 원인 등의 내용을 저장
	LAST_UPDATED DATETIME(6), # 마지막 실행 시점 기록
	constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID)
	references BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
) ENGINE=InnoDB;
``` 
#### `BATCH_JOB_EXECUTION_PARAMS`
- Job과 함께 실행되는 JobParameter 정보가 저장
- `BATCH_JOB_EXECUTION` 테이블과 N:1 관계
```mysql
CREATE TABLE BATCH_JOB_EXECUTION_PARAMS  (
	JOB_EXECUTION_ID BIGINT NOT NULL ,
	PARAMETER_NAME VARCHAR(100) NOT NULL , # 파라미터 키
	PARAMETER_TYPE VARCHAR(100) NOT NULL , # 파라미터 타입 정보
	PARAMETER_VALUE VARCHAR(2500) , # 파라미터 값
	IDENTIFYING CHAR(1) NOT NULL , # 식별 여부
	constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
	references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;
```
#### `BATCH_JOB_EXECUTION_CONTEXT`
- Job의 실행 동안 여러 가지 상태 정보, 공유 데이터를 직렬화(Json) 해서 저장
- Step 간 서로 공유 가능
- `BATCH_JOB_EXECUTION` 테이블과 N:1 관계
```mysql
CREATE TABLE BATCH_JOB_EXECUTION_CONTEXT (
    JOB_EXECUTION_ID   BIGINT        NOT NULL PRIMARY KEY,
    SHORT_CONTEXT      VARCHAR(2500) NOT NULL, # Job의 실행 상태 정보, 공유 데이터 등의 정보를 문자열로 저장
    SERIALIZED_CONTEXT TEXT, # 직렬화된 전체 Context
    constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID)
        references BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
) ENGINE = InnoDB;
```
### Step 테이블
#### `BATCH_STEP_EXECUTION`
- Step의 실행정보가 저장되며 Job 생성, 시작, 종료 시간, 실행상태, 메시지 등을 관리
- `BATCH_JOB_EXECUTION` 테이블과 N:1 관계
```mysql
CREATE TABLE BATCH_STEP_EXECUTION  (
	STEP_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
	VERSION BIGINT NOT NULL, # 업데이트 될 때마다 1씩 증가
	STEP_NAME VARCHAR(100) NOT NULL, # Step 이름
	JOB_EXECUTION_ID BIGINT NOT NULL,
	CREATE_TIME DATETIME(6) NOT NULL,
	START_TIME DATETIME(6) DEFAULT NULL ,
	END_TIME DATETIME(6) DEFAULT NULL ,
	STATUS VARCHAR(10) ,
	COMMIT_COUNT BIGINT , # 트랜잭션 당 커밋되는 수를 기록
	READ_COUNT BIGINT , # 실행 시점에 Read한 Item 수를 기록
	FILTER_COUNT BIGINT , # 실행 중 필터링된 Item 수를 기록
	WRITE_COUNT BIGINT , # 실행 중 저장되고 커밋된 Item 수를 기록
	READ_SKIP_COUNT BIGINT , # 실행 중 Read가 Skip된 Item 수를 기록
	WRITE_SKIP_COUNT BIGINT , # 실행 중 Write가 Skip된 Item 수를 기록
	PROCESS_SKIP_COUNT BIGINT , # 실행 중 Process가 Skip된 Item 수를 기록
	ROLLBACK_COUNT BIGINT , # 실행 중 Rollback이 일어난 수를 기록
	EXIT_CODE VARCHAR(2500) ,
	EXIT_MESSAGE VARCHAR(2500) ,
	LAST_UPDATED DATETIME(6),
	constraint JOB_EXEC_STEP_FK foreign key (JOB_EXECUTION_ID)
	references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;
```
#### `BATCH_STEP_EXECUTION_CONTEXT`
- Step의 실행 동안 여러 가지 상태 정보, 공유 데이터를 직렬화(Json) 해서 저장
- Step 간 서로 공유할 수 없음
- `BATCH_STEP_EXECUTION` 테이블과 N:1 관계
### `BATCH_STEP_EXECUTION_SEQ`
### `BATCH_JOB_EXECUTION_SEQ`
### `BATCH_JOB_SEQ`

---
# 스프링 배치 도메인 이해
## Job
- 가장 상위에 있는 개념으로 하나의 배치 작업 자체를 의미한다.
- 여러 Step을 포함하고 있는 컨테이너로써 반드시 한 개 이상의 Step으로 구성해야 한다.
### Job
- SimpleJob
  - 순차적으로 Step을 실행시킴
- FlowJob
  - 특정한 조건과 흐름에 따라 Step을 구성하여 실행시킴
### JobInstance
- Job이 실행될 때 생성되는 Job의 논리적 실행 단위 객체, 고유하게 식별 가능한 작업 실행을 나타냄
- Job의 설정과 구성은 동일하지만, 실행되는 시점에 처리하는 내용은 다르기 때문에 Job의 실행을 구분해야 함
  - 예) 하루 한 번 배치 Job이 실행된다면, 하루마다 생성되는 JobInstance는 달라야 한다.
- 생성 및 실행
  - 처음 시작하는 Job + JobParameters 일 경우 새로운 JobInstance 생성
  - 이전과 동일한 Job + JobParameters 일 경우 이미 존재하는 JobInstance를 반환
    - 내부적으로 jobName + jobKey(jobParameters의 해시값)를 가지고 JobInstacne를 가져온다.
- `BATCH_JOB_INSTANCE` 테이블과 매핑
  - JOB_NAME과 JOB_KEY(jobParameters 해시값)가 동일한 데이터는 중복해서 저장할 수 없다.
### JobParameters
- Job을 실행할 때 함께 포함되어 사용되는 파라미터를 가진 객체
- Job에 존재할 수 있는 여러개의 JobInstance를 구분하기 위한 용도
  - JobParameters와 JobInstance는 1:1 관계
- `BATCH_JOB_EXECUTION_PARAM` 테이블과 매핑
  - `BATCH_JOB_EXECUTION`과 1:N 관계
### JobExecution
- JobInstance에 대한 한번의 시도를 의미하는 객체, Job 실행 중에 발생한 정보들을 저장하고 있다.
  - 시작 시간, 종료 시간, 상태 속성을 가짐
- JobExecution은 `FAILED`, `COMPLETED` 등의 실행 결과 상태를 가지고 있다.
- 실행 상태 결과가 `COMPLETED`이면 완료된 것으로 간주해서 재실행이 불가능
- 반대로 `FAILED`이면 JobInstance 실행이 완료되지 않은 것으로 간주해서 재실행이 가능하다.
  - JobParameter가 동일한 값으로 Job을 실행할지라도 JobInstance를 계속 실행할 수 있음
- 즉, 상태가 COMPLETED가 될 때까지 하나의 JobInstance 내에서 여러번의 시도가 생길 수 있다.
- `BATCH_JOB_EXECUTION` 테이블과 매핑
  - JobInstance와 JobExecution은 1:N 관계
## Step
- 배치 작업을 어떻게 구성하고 실행할 것인지 Job의 세부 작업을 Task 기반으로 설정하고 명세해 놓은 객체
- 모든 Job은 하나 이상의 Step으로 구성된다.
- **구현체**
  - `TaskletStep`: 가장 기본이 되는 클래스
  - `PartitionStep`: 멀티 스레드 방식으로 Step을 여러 개로 분리해서 실행
  - `JobStep`: Step 내에서 Job을 실행하도록 함
  - `FlowStep`: Step 내에서 Flow를 실행하도록 한다.
### StepExecution
- Step 실행 중에 발생한 정보들을 저장하고 있는 객체
- Step이 매번 시도될 때마다 생성되며 Step별로 생성됨
- Job이 재시작 하더라도 이미 성공적으로 완료된 Step은 재실행되지 않고 실패한 Step만 실행된다.
- 이전 단계 Step이 실패해서 다음 Step을 실행하지 않았다면 StepExecution을 생성하지 않는다.
### StepContribution
- Chunk 프로세스의 변경 사항을 버퍼링 한 후 StepExecution 상태를 업데이트하는 도메인 객체
- Chunk 커밋 직전에 StepExecution의 apply 메서드를 호출하여 상태를 업데이트
- ExitStatus의 기본 종료 코드 외 사용자 정의 종료 코드를 생성해서 적용할 수 있음
## ExecutionContext
- StepExecution 또는 JobExecution 객체의 상태를 저장하는 공유 객체
- DB에 직렬화된 값으로 저장됨
- 공유 범위
  - StepExecution은 Step 간 서로 공유 안됨
  - JobExecution은 Job 간 서로 공유 안되며 해당 Job의 Step 간 서로 공유됨
- Job 재시작 시 이미 처리한 Row 데이터는 건너뛰고 이후 수행할 때 상태 정보를 활용
## JobRepository
- Job 배치 작업의 수행과 관련된 모든 meta data를 저장
- JobRepository 설정
  - JobRepositoryFactoryBean
    - 많은 사용자가 동시에 접근하는 것이 아니기 때문에 트랜잭션 isolation 레벨이 `SERIALIZEBLE`이다.
    - 성능 이유, Test 나 프로토타입에 사용하고 싶은 경우에는 DB 타입을 H2로 변경할 수 있다.

## JobLauncher
- Job을 실행시키는 역할
  - 동기적 실행 (`SyncTaskExecutor`)
    - 스케줄러에 의한 배치처리에 적합
  - 비동기적 실행 (`SimpleAsyncTaskExecutor`)
    - `TaskExecutorJobLauncher`로 실행시킬 수 있다.
    - HTTP 요청에 의한 배치저리에 적합(배치처리 시간이 길 경우 응답이 늦어지지 않도록 함)
- Job과 JobParameters를 인자로 받으며 작업을 수행한 후 최종 Client에게 `JobExecution`을 반환한다.

---
# Chunk
- 여러 개의 아이템을 묶은 하나의 덩어리
- 하나씩 아이템을 입력 받아 Chunk 단위의 덩어이로 만든 후, Chunk 단위로 트랜잭션을 처리
- 일반적으로 대용량 데이터를 한번에 처리하는 것이 아닌 청크 단위로 쪼개어서 더 이상 처리할 데이터가 없을 때 까지 반복

- `Chunk<I>` vs `Chunk <O>`
  - `Chunk<I>`: ItemReader로 읽은 하나의 아이템을 Chunk에서 정한 개수만큼 반복해서 저장하는 타입
  - `Chunk <O>`: ItemReader로부터 전달받은 `Chunk<I>`를 참조해서 ItemProcessor에서 적절하게 가공, 필터링한 다음 ItemWriter에 전달  

## ItemStream
- ItemReader, ItemWriter 처리 과정 중 상태를 저장하고 오류가 발생하면 해당 상태를 참조하여 실패한 곳에서 재시작 하도록 지원
- ExecutionContext를 매개변수로 받아서 상태 정보를 업데이트한다.

## Cursor & Paging 이해
### Cursor Based 처리
- JDBC ResultSet의 기본 메커니즘을 사용
- 현재 행에 커서를 유지하며 다음 데이터를 호출하면 다음 행으로 커서를 이동하며 데이터 반환이 이루어지는 Streaming 방식의 I/O
- ResultSet이 open될 때 마다 `next()` 메서드가 호출되어 DB 데이터가 반환되고 객체와 매핑이 이루어짐
- DB Connection이 연결되면 배치 처리가 완료될 때 까지 데이터를 읽어오기 때문에 DB와 Timeout을 충분히 큰 값으로 설정 필요
- 모든 결과를 메모리에 할당하기 때문에 메모리 사용량이 많아지는 단점이 있다
- Connection 연결 유지 시간과 메모리 공간이 충분하다면 대량의 데이터 처리에 적합할 수 있다 (fetchSize 조절)

#### JdbcCursorItemReader
- Thread 안정성을 보장하지 않기 때문에  멀티 스레드 환경에서 사용할 경우 동시성 이슈가 발생하지 않도록 별도 동기화 처리가 필요

### Paging Based 처리
- 페이징 단위로 데이터를 조회하는 방식으로 Page Size 만큼 한번에 메모리로 가지고 온 다음 한개씩 읽는다
- 한 페이지를 읽을때 마다 Connection을 맺고 끊기 때문에 대량의 데이터를 처리하더라도 SocketTimeout 예외가 거의 일어나지 않는다
- 시작 행 번호를 지정하고 페이지에 반환하고자 하는 행의 수를 지정한 후 사용 - Offset, Litmit
- 페이징 단위의 결과만 메모리에 할당하기 때문에 메모리 사용량이 적어지는 장점이 있다
- Connection 연결 유지 시간이 길지 않고 메모리 공간을 효율적으로 사용해야 하는 데이터 처리에 적합할 수 있다
