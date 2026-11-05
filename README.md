# dev-learning — 업무 티켓 앱 실습 기록

Java/Spring 경험을 기반으로 TypeScript·React·JPA·자동 테스트를 학습하는 프로젝트.
**무엇을 설치하고, 어디서 어떤 명령을 실행하며, 왜 필요한지**를 기록한다.

> Windows PowerShell 기준. 예제 경로는 `C:\Users\luna\Documents\dev-learning`이다. 다른 PC에서는 실제 경로로 바꾼다.
> 명령 실패 시 다음 단계로 넘어가지 않고 원인을 확인한다.
> 2026-11-03 기준: 8080에서 React 제공 및 상세 화면 새로고침·직접 접속 확인 완료.
> 사용자 실습으로 build.ps1의 Build complete와 생성된 JAR 실행 정상 동작까지 확인했다.

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
덮어쓰기는 새 빌드에서 사라진 옛 파일까지 삭제하지 않는다. 아래 build.ps1은 복사 전에 생성물 전용 static 폴더를 정리해 이 문제를 해결한다.

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

## 6. 실행 JAR 만들기 — 수동 실행 확인 완료

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

## 6-1. 한 번의 명령으로 검사·빌드·패키징

새 PC에서는 2절대로 루트와 ticket-ui에서 npm ci를 먼저 실행한다. build.ps1은 설치 명령을 반복하지 않는다.
실행 중인 Spring/JAR 서버는 Ctrl+C로 종료한다. 프런트/백엔드 검사를 모두 통과해야 새 JAR 패키징이 완료된다.

```powershell
cd C:\Users\luna\Documents\dev-learning
.\build.ps1
```

실행 순서:

1. 루트 TypeScript 테스트
2. 프런트엔드 테스트
3. 프런트엔드 린트
4. React 빌드
5. 생성물 전용 static 폴더 정리 후 새 dist 복사
6. Maven clean package: 백엔드 테스트 및 JAR 생성

PowerShell의 스크립트 실행 정책 오류가 발생하면, 내용을 확인한 이 로컬 스크립트에 한해 다음처럼 실행할 수 있다.
회사 관리 정책으로 제한된 경우에는 우회하지 않고 해당 환경의 정책을 따른다.

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\build.ps1
```

이 옵션은 해당 PowerShell 프로세스에만 적용되며 사용자/시스템 실행 정책을 영구 변경하지 않는다.

- $PSScriptRoot: 스크립트 파일이 있는 폴더. 실행한 터미널 위치와 관계없이 저장소를 기준으로 한다.
- Push-Location / Pop-Location: 작업 폴더 이동 후 복원. finally를 사용해 실패해도 위치를 복원한다.
- $ErrorActionPreference = 'Stop': 파일 복사 등의 PowerShell 오류에서 중단한다.
- $LASTEXITCODE: npm/Maven의 종료 코드. 0이 아니면 throw로 중단하고 스크립트도 종료 코드 1을 반환한다.
- 생성물 전용 static만 정리한다. 원본 src 파일이나 H2 data는 정리 대상이 아니다.
- 이전 파일명을 가진 JS/CSS가 남지 않도록 static을 교체한다. static에 수작업 원본을 저장하지 않는다.
- 자동화가 실패하면 실행 단계로 넘어가지 않는다. 실패 지점에 따라 예전 JAR이 남아 있을 수도 있다.
- 서버 실행·Git 커밋·push는 자동화하지 않는다.

Build complete 출력까지 확인한 뒤 직접 실행한다.

```powershell
cd .\ticket-api
java -jar .\target\ticket-api-0.0.1-SNAPSHOT.jar
```

2026-11-03 실습 완료: 사용자가 build.ps1을 실행하여 Build complete를 확인했고, 생성된 JAR도 실행하여 정상 동작을 확인했다. 자동화는 TypeScript 테스트 → 프런트 테스트·린트·빌드 → static 교체 → 백엔드 테스트·JAR 패키징 순서로 실행된다.

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
8. build.ps1 빌드·복사·패키징 자동화 및 생성된 JAR 실행 확인 완료.

수동 충돌 실습: 두 탭에서 같은 티켓 조회 → A 완료 → 새로고침하지 않은 B 완료 → 409 → 다시 조회 후 재오픈 성공.

### 다음 학습 계획: Spring AI로 티켓 해결 방향 제안하기

기존 티켓 앱의 Controller → Service 구조를 활용한다. 아래 단계는 앞으로 진행할 실습이다.

1. **첫 모델 호출**: Ollama에서 로컬 모델을 실행하고, Spring AI로 같은 모델을 호출해 Java에서 첫 응답을 받는다. 로컬 호출에는 외부 API 키가 필요 없다.
2. **티켓 해결 방향 제안**: 티켓 제목을 프롬프트에 넣어 해결 방향을 요청하고, 제목만으로 판단하기 어려운 부분을 확인한다.
3. **구조화된 응답**: 분류·우선순위·추천 조치를 Java DTO로 받아 각 필드의 값을 검증한다.
4. **오류 처리와 테스트**: 모델 응답 지연과 호출 실패를 재현하고, 사용자에게 안내가 표시되는지 검사한다.
5. **도구 호출**: 기존 티켓 조회 서비스를 도구로 제공해 모델이 조회한 티켓 정보를 바탕으로 답하도록 한다.
6. **업무 매뉴얼 검색(RAG)**: 티켓과 관련된 매뉴얼 문단을 검색해 모델에 전달하고, 제안한 조치가 해당 문단에 근거하는지 확인한다.

### 실습 기록: Ollama에서 로컬 모델 실행

Windows에 [Ollama](https://ollama.com/download/windows)를 설치한 뒤 새 PowerShell에서 실행했다. 프로젝트 폴더로 이동할 필요는 없다.

```powershell
ollama --version
ollama run qwen3:1.7b
```

모델에 “로그인 오류의 원인을 단정하지 말고 먼저 확인할 사항 두 가지를 알려 줘”라고 질문했다. 비밀번호 입력과 계정 잠금 여부를 확인하라는 한국어 답변을 받았다.
대화를 열어 둔 채 다른 PowerShell에서 `ollama ps`를 실행해 모델 배치를 확인했다.

| Ollama 버전 | 모델 | PROCESSOR | CONTEXT |
| --- | --- | --- | --- |
| 0.40.13 | qwen3:1.7b | 100% GPU | 4096 |

### 실습 기록: Java에서 로컬 Ollama 호출

Spring AI BOM 2.0.2와 `spring-ai-starter-model-ollama`를 사용했다. 모델은 `qwen3:1.7b`, 서버 주소는 `http://localhost:11434`로 설정했다.
AI 코드는 `com.example.ticket_api.ai` 하위 패키지에 둔다. 첫 실습에서는 `OllamaDemo`를 `ai-demo` 프로필에서만 실행해 일반 서버 시작 시 모델에 질문하지 않도록 했다.

Ollama 앱을 켠 상태에서 `ticket-api` 폴더에서 실행:

```powershell
.\mvnw.cmd clean spring-boot:run "-Dspring-boot.run.profiles=ai-demo"
```

패키지를 이동한 뒤 이전 컴파일 결과를 지우려고 `clean`을 사용했다. 사용자가 터미널에서 `[Ollama → Java]` 뒤에 한국어 답변이 출력되는 것을 확인했다.

### 2026-11-04: 티켓 제목으로 AI 점검 사항 제안받기

모델 호출을 다른 곳에서도 재사용할 수 있도록 `ai` 패키지의 `TicketAdviceService.suggest(title)`로 분리했다. `OllamaDemo`는 생성자로 서비스를 받아 제목을 전달하고 결과만 출력한다.

- 실행 위치: `ticket-api`
- 실행 명령: `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=ai-demo"`
- 확인 결과: “프린터 연결 오류”에 대해 케이블·포트, 설정·드라이버 두 가지 점검 제안이 한국어로 출력됐다. 이는 모델 호출 확인이며 실제 장애 원인을 검증한 것은 아니다.

### Postman으로 티켓 AI 제안 API 호출

Java 코드 수정 없이 요청마다 다른 제목을 보낼 수 있도록 컨트롤러에서 `TicketAdviceService.suggest(title)`를 호출한다.
Ollama를 켜고 `ticket-api`에서 `.\mvnw.cmd spring-boot:run`으로 서버를 실행한다. 이 API에는 `ai-demo` 프로필이 필요 없다.

- 요청: `POST http://localhost:8080/api/ai/ticket-advice`
- Postman: **Body → raw → JSON**, 본문 `{"title":"첨부파일 업로드 실패"}`
- 확인 결과: `200 OK`. JSON의 `advice` 필드에 파일 크기 제한과 지원 형식을 점검하라는 한국어 제안이 반환됐다. 답변 문구는 호출마다 달라질 수 있다.
- 공백 제목 확인: 같은 URL에 `{"title":"   "}`를 보내자 `400 Bad Request`가 반환됐다. 실제 응답 본문에는 `timestamp`, `status`, `error`, `path`가 있었고 별도의 안내 메시지는 없었다.

### 2026-11-04: AI 제안 컨트롤러의 공백 제목 테스트

`ticket-api`에서 `.\mvnw.cmd "-Dtest=TicketAdviceControllerTest" test` 실행: 테스트 1개 통과.
MockMvc로 공백 제목을 보냈을 때 400을 반환하고, `verifyNoInteractions(adviceService)`로 AI 서비스를 호출하지 않았음을 확인했다.
처음에 테스트 클래스를 `src/main/java`에 두어 테스트 전용 `@WebMvcTest` import를 찾지 못했다. 같은 패키지 구조의 `src/test/java`로 이동해 해결했다.

정상 제목 테스트를 추가한 뒤 같은 명령으로 2개 통과. 요청 제목의 앞뒤 공백을 제거해 서비스에 한 번 전달하고, 서비스가 반환한 조언을 HTTP 200 응답의 `advice` 필드로 돌려주는지 확인했다.

마무리 검증: `ticket-api`에서 `.\mvnw.cmd clean test` 실행. 기존 티켓 기능과 새 AI 컨트롤러 테스트를 포함해 총 39개 통과(실패·오류·건너뜀 0), BUILD SUCCESS.

### 2026-11-05: React 상세 화면에서 AI 도움말 요청

`TicketAdvice` 컴포넌트를 상세 화면에 연결했다. 제목을 `POST /api/ai/ticket-advice`로 보내고, 요청 중에는 버튼을 비활성화하며 성공 답변 또는 실패 안내를 표시한다. `key={ticket.id}`로 다른 티켓으로 이동하면 컴포넌트 상태를 새로 시작한다.
`ticket-advice-api.ts`는 HTTP 실패와 응답 형식 오류를 구분한다. `advice`가 비어 있지 않은 문자열인지 검사한 뒤 반환한다.

추가 설치는 없었다. Windows 시작 메뉴에서 Ollama를 실행하고 `http://localhost:11434`에서 실행 상태를 확인한다. Spring 요청 시 모델이 로드되므로 별도로 CLI 대화를 열 필요는 없다.

첫 번째 터미널에서 Spring Boot 실행:

```powershell
cd C:\Users\luna\Documents\dev-learning\ticket-api
.\mvnw.cmd spring-boot:run
```

두 번째 터미널에서 Vite 실행:

```powershell
cd C:\Users\luna\Documents\dev-learning\ticket-ui
npm.cmd run dev
```

개발 화면은 `http://localhost:5173`에서 확인한다. `/api` 요청은 Vite 프록시가 Spring Boot의 8080 포트로 전달한다. 8080은 이전에 static에 복사한 React 빌드를 제공하므로 소스를 수정해도 바로 바뀌지 않는다. 이번 실습에서도 처음에 8080 화면을 보다가 5173으로 변경해 해결했다. 사용자가 AI 답변 표시와 요청 중/완료 후 동작을 확인했다. 답변 도착은 연동 성공을 뜻하며 내용의 정확성은 별도 검토가 필요하다.

파일별 테스트 실행 위치: `ticket-ui`. 두 파일 모두 각각 3개 통과를 확인했다.

```powershell
npm.cmd test -- src/ticket-advice-api.test.ts
npm.cmd test -- src/TicketAdvice.test.tsx
```

- API 함수 테스트: 정상 제목 전송·답변 반환, advice 누락 거절, HTTP 503 오류 처리. fetch를 가짜로 바꿔 검사한다. 503은 테스트에서 준비한 상태이며 실제 Ollama 중단 시 서버 응답을 확인한 것은 아니다.
- 컴포넌트 테스트: 성공 답변 표시, 실패 안내·버튼 재활성화, 요청 대기 중 버튼 비활성화·중복 요청 방지. API 함수를 가짜로 바꿔 검사한다.
- 대기 상태는 Promise의 resolve 함수를 테스트 지역변수에 보관하고 나중에 호출해 재현했다. 직접 Promise를 완료할 때는 act로 React 상태 업데이트를 처리한 뒤 검사한다. 이 테스트에는 실제 Ollama와 Spring 서버가 필요 없다.

커밋 전 전체 확인 명령(개별 테스트 결과와 별도로 실행 결과 확인 필요):

```powershell
cd C:\Users\luna\Documents\dev-learning\ticket-ui
npm.cmd test
npm.cmd run build
npm.cmd run lint
cd ..
npm.cmd test
git diff --check
git status --short
```

서버 종료는 Spring Boot와 Vite 터미널에서 각각 Ctrl+C로 한다.

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
