# dev-learning — 업무 티켓 앱 실습 기록

Java/Spring 경험을 기반으로 TypeScript·React·JPA·자동 테스트를 학습하는 프로젝트.
**무엇을 설치하고, 어디서 어떤 명령을 실행하며, 왜 필요한지**를 기록한다.

> Windows PowerShell 기준. 예제 경로는 `C:\Users\luna\Documents\dev-learning`이다. 다른 PC에서는 실제 경로로 바꾼다.
> 명령 실패 시 다음 단계로 넘어가지 않고 원인을 확인한다.
> 2026-11-03 기준: 8080에서 React 제공 및 상세 화면 새로고침·직접 접속 확인 완료.
> JAR 실행은 아래 절차로 확인할 다음 단계이며 아직 성공 결과를 기록하지 않았다.

## 자주 쓰는 명령

| 실행 위치 | 명령 | 목적 |
| --- | --- | --- |
| 루트 | `npm.cmd test` | 기초 TypeScript 검색 로직 검사 |
| 루트 | `npm.cmd run typecheck` | 파일 생성 없이 타입 검사 |
| ticket-ui | `npm.cmd run dev` | React 개발 서버 |
| ticket-ui | `npm.cmd test` | Vitest API·화면 테스트 |
| ticket-ui | `npm.cmd run build` | 타입 검사·배포 파일 생성 |
| ticket-ui | `npm.cmd run lint` | ESLint 검사 |
| ticket-api | `.\mvnw.cmd spring-boot:run` | Spring 개발 실행 |
| ticket-api | `.\mvnw.cmd test` | 백엔드 테스트 |
| ticket-api | `.\mvnw.cmd clean package` | 빌드 정리·테스트·JAR 생성 |
| ticket-api | `java -jar .\target\ticket-api-0.0.1-SNAPSHOT.jar` | JAR 실행 |
| 저장소 내부 | `git status` | 브랜치·변경 상태 |

PowerShell에서 npm.ps1 실행 정책 문제를 피하기 위해 npm.cmd를 사용한다. 서버 종료는 해당 터미널에서 Ctrl+C.

## 1. 프로젝트 구조

```text
dev-learning/
├─ tickets.ts / tickets.test.ts   # 공통 타입·검색 로직·기초 테스트
├─ async-demo.ts / http-demo.ts   # 비동기·HTTP 연습
├─ package.json                  # 루트 TypeScript 명령
├─ ticket-ui/                    # React + TypeScript + Vite
│  ├─ src/                       # 화면·API 함수·테스트
│  └─ dist/                      # React 빌드 결과 (Git 제외)
└─ ticket-api/                   # Java 21 + Spring Boot + JPA + H2
   ├─ src/main/java/             # Controller·Service·Entity·Repository
   ├─ src/main/resources/
   │  ├─ application.properties
   │  ├─ schema.sql              # 스키마 준비
   │  └─ static/                 # React 빌드 결과 복사 위치 (Git 제외)
   ├─ src/test/                  # 백엔드 테스트·테스트 데이터
   ├─ data/                      # H2 파일 DB (Git 제외)
   └─ target/                    # 클래스·JAR·보고서 (Git 제외)
```

## 2. 새 PC에 설치하기

| 도구 | 실습 기준 | 목적 |
| --- | --- | --- |
| VS Code | 설치된 버전 사용 | 편집·터미널·디버깅 |
| Git | Git for Windows | 버전 관리·GitHub |
| JDK | 21 | Java 컴파일·Spring 실행 |
| Node.js | 24.x (실습 시 24.21.0) | npm·Vite·Vitest |

공식 설치 페이지: [VS Code](https://code.visualstudio.com/docs/setup/windows), [Git](https://git-scm.com/install/windows), [JDK 21](https://adoptium.net/temurin/releases/?version=21), [Node.js](https://nodejs.org/en/download).
Node.js는 실습과 같은 24.x를 선택한다. 설치 후 VS Code와 터미널을 다시 열어 PATH를 반영한다.

WinGet이 있는 Windows에서는 Git/JDK를 다음과 같이 설치할 수도 있다. 이미 설치했다면 생략한다.

```powershell
winget install --id Git.Git -e --source winget
winget install --id EclipseAdoptium.Temurin.21.JDK -e --source winget
```

VS Code에서 Extension Pack for Java를 설치한다. code 명령을 사용할 수 있다면:

```powershell
code --install-extension vscjava.vscode-java-pack
```

설치 확인:

```powershell
git --version
node --version
npm.cmd --version
java -version
javac -version
$env:JAVA_HOME
```

java와 javac가 모두 21인지 확인한다. JAVA_HOME은 bin이 아닌 JDK 설치 폴더다.
현재 터미널에서 수정하는 예시(본인 PC의 실제 경로로 변경):

```powershell
$env:JAVA_HOME = "C:\jdk-21.0.12.1+1"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

위 설정은 현재 터미널에만 적용된다. 지속하려면 Windows 환경 변수 JAVA_HOME/Path를 설정하고 오래된 Java 경로가 우선하지 않는지 확인한다.
Maven은 별도 설치하지 않는다. 저장소의 Maven Wrapper가 준비한다.
React·TypeScript·H2도 전역 설치하지 않는다. 프로젝트의 npm/Maven 의존성을 사용한다.

### 저장소와 의존성 내려받기

아직 저장소가 없는 PC에서만 clone한다.

```powershell
cd C:\Users\luna\Documents
git clone https://github.com/LUNASOL9/dev-learning.git
cd dev-learning
npm.cmd ci
cd ticket-ui
npm.cmd ci
cd ..\ticket-api
.\mvnw.cmd -version
```

루트와 ticket-ui는 package.json이 따로 있으므로 각각 설치한다.
npm ci는 커밋된 package-lock.json 기준으로 설치한다. 새 패키지 추가는 npm install을 사용한다.
새 PC에는 기존 PC의 DB가 없다. data 폴더는 Git으로 동기화하지 않는다. 스키마는 생성되지만 테스트 티켓 3건은 실제 앱에 자동 등록하지 않으므로 화면에서 등록한다.

## 3. 개발 실행 — 서버 두 개

터미널 A:

```powershell
cd C:\Users\luna\Documents\dev-learning\ticket-api
.\mvnw.cmd spring-boot:run
```

터미널 B:

```powershell
cd C:\Users\luna\Documents\dev-learning\ticket-ui
npm.cmd run dev
```

화면은 보통 <http://localhost:5173/> (Vite 출력 주소 확인), API는 <http://localhost:8080/api/tickets>.
Vite가 개발 화면을 제공하고 /api 요청을 Spring 8080으로 프록시한다.
vite.config.ts의 server.proxy는 개발 서버 설정이며 배포 파일에 포함되는 프록시 서버가 아니다.

## 4. 테스트·검사

### 루트 TypeScript

```powershell
cd C:\Users\luna\Documents\dev-learning
npm.cmd run typecheck
npm.cmd test
```

루트 test는 컴파일 후 node dist/tickets.test.js를 실행한다.

### 프런트엔드

```powershell
cd C:\Users\luna\Documents\dev-learning\ticket-ui
npm.cmd test
npm.cmd run build
npm.cmd run lint

# 파일 하나만 검사
npm.cmd test -- src/ticket-api.test.ts
npm.cmd test -- src/TicketForm.test.tsx
```

API 테스트는 실제 함수에서 fetch만 교체하여 버전 전송·409 메시지·잘못된 응답 거절을 검사한다.
화면 테스트는 실제 TicketForm을 jsdom에 렌더링하여 입력·클릭·공백 제거·실패 시 입력 유지·50/51자 경계를 검사한다.
가짜 fetch/onCreate를 쓰는 테스트는 실제 DB 저장을 증명하지 않는다. 화면 테스트 완료 시 전체 프런트 테스트는 9개였다.

### 백엔드

```powershell
cd C:\Users\luna\Documents\dev-learning\ticket-api
.\mvnw.cmd test

# PowerShell에서는 -D 인수를 따옴표로 감싼다.
.\mvnw.cmd "-Dtest=TicketServiceTest" test
.\mvnw.cmd "-Dtest=TicketControllerTest" test
.\mvnw.cmd "-Dtest=TicketTransactionTest" test

# 이전 컴파일 결과까지 정리
.\mvnw.cmd clean test
```

test는 Maven 빌드 생명주기 단계다. Surefire/JUnit이 탐색한 테스트를 실행한다.
보고서는 target/surefire-reports에 생성된다. 현재 DB 테스트는 테스트용 H2 메모리 DB를 사용하도록 구성해 실행용 파일 DB와 분리했다.

## 5. React 빌드 결과를 Spring에서 제공

### 빌드 및 복사

```powershell
cd C:\Users\luna\Documents\dev-learning\ticket-ui
npm.cmd run build
```

**빌드 성공 후에만** 복사한다. 실패 후 복사하면 이전 dist가 사용될 수 있다.

```powershell
New-Item -ItemType Directory -Force -Path ..\ticket-api\src\main\resources\static
Copy-Item -Path .\dist\* -Destination ..\ticket-api\src\main\resources\static -Recurse -Force
Get-ChildItem ..\ticket-api\src\main\resources\static
```

결과는 static/index.html과 static/assets다. static/dist/index.html이 아니다.
App.tsx·TicketForm.tsx는 JavaScript로 변환·묶여 assets에 들어간다. 이동이 아니라 복사다.
덮어쓰기는 새 빌드에서 사라진 옛 파일까지 삭제하지 않는다. 산출물 정리·자동화는 후속 단계에서 다룬다.

루트 .gitignore에는 다음 경로를 사용한다.

```gitignore
/ticket-api/src/main/resources/static/
```

/src/main/resources/static/은 ticket-api/.gitignore에 넣을 때 맞다. 앞의 /는 해당 .gitignore 위치 기준이다.
현재 static은 생성된 React 파일 전용이다. 원본과 재생성 방법을 관리하고 생성 파일은 커밋하지 않는다.

### Spring만 재시작

이전 Spring 서버를 Ctrl+C로 종료한다. Vite는 꺼도 된다.

```powershell
cd C:\Users\luna\Documents\dev-learning\ticket-api
.\mvnw.cmd spring-boot:run
```

화면: <http://localhost:8080/>.

| 요청 | 처리 |
| --- | --- |
| / | static/index.html 제공 |
| /assets/...js | 빌드된 JavaScript 제공 |
| /api/tickets | API 컨트롤러가 JSON 반환 |
| /tickets/{ticketId} | FrontendController가 index.html로 forward |

React는 브라우저에서 실행된다. Spring은 ticket-ui 원본을 직접 참조하지 않는다.
fetch("/api/tickets")는 화면을 연 8080 서버로 요청하므로 Vite 프록시를 거치지 않는다.
원본 수정 시 **재빌드 → 재복사 → 서버 재시작**이 필요하다.

### 상세 주소 직접 접속·새로고침

FrontendController의 핵심 코드:

```java
@Controller
public class FrontendController {
    @GetMapping("/tickets/{ticketId}")
    public String ticketDetail() {
        return "forward:/index.html";
    }
}
```

@RestController가 아닌 @Controller다. 주소를 유지하면서 서버 내부에서 index.html로 전달한다.
브라우저에서 React가 시작되면 React Router가 상세 화면을 선택한다. 모든 URL이 아닌 상세 경로만 연결했다.
확인 완료: 8080 목록 조회·상세 새로고침·새 탭 직접 접속.

## 6. 실행 JAR 만들기 — 다음 확인 단계

먼저 5절의 React 빌드·복사를 완료하고 기존 Spring 서버를 종료한다.

```powershell
cd C:\Users\luna\Documents\dev-learning\ticket-api
.\mvnw.cmd clean package
```

BUILD SUCCESS 확인 후 같은 폴더에서:

```powershell
java -jar .\target\ticket-api-0.0.1-SNAPSHOT.jar
```

- clean은 target을 정리한다. src/main/resources/static이나 data를 삭제하지 않는다.
- package는 컴파일·리소스 복사·백엔드 테스트·패키징을 수행한다.
- Spring Boot Maven 플러그인이 의존성·내장 서버를 포함한 실행 JAR을 만든다.
- 현재 Maven 설정은 React 빌드·프런트 테스트를 자동 실행하지 않는다.
- static 파일도 JAR에 포함된다. 새 clone에서는 먼저 React 빌드·복사가 필요하다.
- java -jar는 만들어진 JAR을 실행한다. React 변경 후에는 재빌드·복사·재패키징한다.
- 파일명은 현재 pom.xml의 artifactId/version 기준이며 버전 변경 시 달라진다.

8080에서 조회·등록·완료·재오픈·상세 새로고침을 확인한다.
이것은 로컬 패키징 실습이며 외부 서버 공개 배포를 뜻하지 않는다.

### H2 DB 경로

현재 URL은 jdbc:h2:file:./data/tickets다. **프로세스 실행 폴더 기준**이므로 ticket-api에서 실행한다.
다른 폴더에서 실행하면 다른 DB를 사용하게 될 수 있다.
DB는 ticket-api/data/tickets.mv.db로 JAR 밖에 남는다. JAR만 복사해도 데이터가 함께 옮겨지는 것은 아니다.
같은 파일 DB를 사용하는 Spring 프로세스 두 개를 동시에 실행하지 않는다.

spring.jpa.hibernate.ddl-auto=none은 Hibernate 자동 스키마 변경을 끈다.
별도로 spring.sql.init.mode=always에 의해 schema.sql은 실행된다. none이 모든 스키마 변경을 막는 설정은 아니다.

## 7. 기능을 처음 추가할 때의 설치 명령

복습용 기록이다. clone 후에는 npm ci로 복원하므로 반복할 필요 없다.

| 위치 | 명령 | 이유 |
| --- | --- | --- |
| 루트 | `npm.cmd install -D typescript @types/node` | TypeScript·Node 타입 |
| ticket-ui | `npm.cmd install react-router` | 목록·상세 경로 |
| ticket-ui | `npm.cmd install -D vitest` | 프런트 자동 테스트 |
| ticket-ui | `npm.cmd install -D @testing-library/react @testing-library/dom @testing-library/user-event @testing-library/jest-dom jsdom` | 렌더링·입력·클릭·DOM 검사 |

Spring은 [Initializr](https://start.spring.io/)에서 생성했다. Web MVC는 API, JDBC/H2는 DB 실습, Data JPA는 Hibernate 영속성 실습을 위해 pom.xml에 추가했다.
현재 Service는 JPA Repository를 사용하고 이전 직접 작성 JDBC Repository는 제거했다.

## 8. Git 작업 순서

작업 시작 전 변경이 없는지 확인한다. 브랜치 이름은 새 작업 예시다.

```powershell
cd C:\Users\luna\Documents\dev-learning
git status
git switch master
git pull --ff-only origin master
git switch -c feature/my-next-task
```

작업·테스트 후 파일을 지정하여 커밋한다. README만 저장하는 예시:

```powershell
git diff
git diff --check
git add README.md
git diff --cached --stat
git diff --cached
git commit -m "Document development and build commands"
git status
```

기능 브랜치를 별도로 백업하려면:

```powershell
git push -u origin feature/my-next-task
```

완료 후 master 병합·업로드:

```powershell
git switch master
git pull --ff-only origin master
git merge feature/my-next-task
git push origin master
git log --oneline -3
git status
```

충돌·오류가 나면 멈추고 해결한다. 강제 push로 덮어쓰지 않는다.
브랜치 push와 master merge는 별개다. merge 후 브랜치 이름은 자동 삭제되지 않는다.
working tree clean은 미커밋 변경이 없다는 뜻이다. git status는 원격을 새로 조회하지 않으므로 다른 PC 변경은 fetch/pull로 확인한다.

## 9. 실습 중 만난 문제

| 증상 | 확인할 것 |
| --- | --- |
| git/node 명령을 못 찾음 | 설치·PATH·터미널 재시작 |
| 새 터미널에서 Java 버전 오류 | java/javac/JAVA_HOME과 mvnw.cmd -version 비교 |
| class file 61을 52까지만 지원하는 JVM에서 실행 | 오래된 Java 8 선택 여부. 이 프로젝트는 JDK 21 |
| Maven이 테스트를 못 찾음 | src/test/java·클래스명·.java 확장자·-Dtest 값 |
| 8080 또는 H2 파일 사용 중 | 이전 서버 종료 여부 |
| 8080 화면에 수정 미반영 | React 빌드·복사·Spring 재시작 |
| 상세 주소 직접 접속 404 | FrontendController·static/index.html |
| static이 git status에 표시됨 | 루트 .gitignore 경로 |
| TS 편집기 오류가 빌드와 다름 | TypeScript: Restart TS Server |
| pom 수정 후 동기화 알림 | Java 프로젝트 설정 동기화·다시 불러오기 |

VS Code에서 Debug: Create JavaScript Debug Terminal과 breakpoint를 실습했다.
브라우저 React 디버깅은 브라우저 연결이 필요하며 Node 실행 디버깅과 구분한다.

## 10. 학습 흐름·다음 단계

1. TypeScript 검색 → 비동기·HTTP 응답 검증.
2. React state·props·폼·라우팅 → Spring API.
3. Controller/Service/Repository 분리 → 예외의 HTTP 응답 변환.
4. H2 파일 DB → JPA·변경 감지·트랜잭션 롤백.
5. 낙관적 잠금 → 화면 버전 전송 → 오래된 요청 409 거절.
6. Vitest API 테스트 → React 폼 테스트.
7. Spring에서 React 제공 → 상세 주소 직접 접속.
8. 진행 중: JAR 실행 확인. 다음: 빌드·복사 자동화.

수동 충돌 실습: 두 탭에서 같은 티켓 조회 → A 완료 → 새로고침하지 않은 B 완료 → 409 → 다시 조회 후 재오픈 성공.
향후 Python API·LLM 기능을 연결할 예정이다. 아직 구현하지 않은 기능은 완료로 기록하지 않는다.

### 수업 기록 추가 양식

```text
날짜 / 주제:
무엇을, 왜 했는가:
실행 위치:
설치·실행 명령:
예상 결과와 실제 확인 결과:
오류와 해결 방법:
관련 커밋:
```

## 참고 문서

- [Git Windows 설치](https://git-scm.com/install/windows)
- [Windows Java 개발 환경](https://learn.microsoft.com/en-us/windows/dev-environment/java/)
- [VS Code Java 지원](https://code.visualstudio.com/docs/languages/java)
- [npm ci](https://docs.npmjs.com/cli/v11/commands/npm-ci)
- [Spring 정적 파일 제공](https://spring.io/guides/gs/serving-web-content/)
- [Vitest 테스트 환경](https://vitest.dev/guide/environment.html)
- [React Testing Library 설정](https://testing-library.com/docs/react-testing-library/setup/)
