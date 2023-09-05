# Spring Batch (with Spring Boot 3.1.3)

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
