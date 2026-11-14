# dev-learning 설치 가이드 (Windows / PowerShell)

업무 티켓 앱(Java 21 + Spring Boot 4.1.1 백엔드, React 19 + TypeScript + Vite 프런트엔드, Spring AI + Ollama) 개발 환경을 이 PC(`C:\dev-learning`) 기준으로 정리한 문서입니다.

- 작성일: 2026-11-14
- 대상 PC: Intel i5-14600KF / RAM 32GB / NVIDIA RTX 4060 Ti 8GB / Windows 11 Pro

## 1. 현재 설치 상태

| 항목 | 요구 버전 | 이 PC에 설치된 버전 | 설치 방법 |
| --- | --- | --- | --- |
| Git for Windows | 최신 | 2.56.0 | `winget install --id Git.Git -e --source winget` |
| JDK | 21 | Temurin 21.0.13 | `winget install --id EclipseAdoptium.Temurin.21.JDK -e --source winget` |
| Node.js | 24.x | 24.22.1 (npm 11.19.1) | `winget install --id OpenJS.NodeJS.LTS -e --source winget --version 24.22.1` |
| Maven | 3.9.16 | Maven Wrapper가 자동으로 받음 | 전역 설치 불필요 (`mvnw.cmd`) |
| Ollama | 0.40.13 이상 | 0.40.16 | <https://ollama.com/download/windows> |
| Ollama 모델 | README는 `qwen3:1.7b` | `qwen3:8b` (5.2GB) | `ollama pull qwen3:8b` |
| VS Code 확장 | Java Extension Pack | 설치됨 | `code --install-extension vscjava.vscode-java-pack` |

> Node.js는 winget의 LTS가 이미 26.x이기 때문에 `--version 24.22.1`로 24.x를 지정했습니다.
> `JAVA_HOME`은 Temurin 설치 프로그램이 `C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot\`로 설정했습니다.

## 2. 설치 확인

새 PowerShell 창을 열고 실행합니다. (설치 전에 열려 있던 터미널과 VS Code는 PATH가 반영되지 않으므로 완전히 종료 후 다시 실행합니다.)

```powershell
git --version
node --version
npm.cmd --version
java -version
javac -version
$env:JAVA_HOME
ollama --version
ollama list
```

`java`와 `javac`가 모두 21이어야 합니다.

## 3. 의존성 설치

```powershell
cd C:\dev-learning
npm.cmd ci

cd C:\dev-learning\ticket-ui
npm.cmd ci

cd C:\dev-learning\ticket-api
.\mvnw.cmd -version
```

이 PC에서는 위 세 단계와 Maven 의존성 내려받기까지 이미 끝냈습니다.

## 4. Ollama 모델 (RTX 4060 Ti 8GB 기준)

README 실습은 `qwen3:1.7b`(1.4GB)를 썼지만, 이 PC의 VRAM 8GB에는 `qwen3:8b`가 가장 큰 "전부 GPU에 올라가는" 모델입니다.

| 모델 | 크기 | 8GB VRAM 적합성 |
| --- | --- | --- |
| `qwen3:1.7b` | 1.4GB | 여유롭지만 답변 품질이 낮음 |
| `qwen3:4b` | 2.5GB | 여유로움 |
| **`qwen3:8b`** | **5.2GB** | **선택. 100% GPU, VRAM 약 6GB 사용** |
| `qwen3:14b` | 9.3GB | VRAM 초과. 일부가 CPU로 넘어가 느려짐 |

실측(2026-11-14): `ollama ps`에서 `qwen3:8b`가 `100% GPU`, CONTEXT 4096으로 올라갔고 약 25 tokens/s가 나왔습니다.

프로젝트는 `application.properties`의 `${OLLAMA_CHAT_MODEL:qwen3:1.7b}` 설정에 따라 환경변수가 있으면 그 모델을 씁니다. 이 PC에는 사용자 환경변수를 이미 설정했습니다.

```powershell
[Environment]::SetEnvironmentVariable("OLLAMA_CHAT_MODEL", "qwen3:8b", "User")
```

확인(새 터미널에서):

```powershell
$env:OLLAMA_CHAT_MODEL
```

README와 똑같이 `qwen3:1.7b`로 실습하고 싶다면 `ollama pull qwen3:1.7b` 후 위 환경변수를 지우면 됩니다.

```powershell
[Environment]::SetEnvironmentVariable("OLLAMA_CHAT_MODEL", $null, "User")
```

## 5. 실행

Ollama 앱이 켜져 있어야 AI 기능이 동작합니다(시작 메뉴에서 Ollama 실행, <http://localhost:11434> 로 확인).

터미널 1 — 백엔드:

```powershell
cd C:\dev-learning\ticket-api
.\mvnw.cmd spring-boot:run
```

터미널 2 — 프런트엔드:

```powershell
cd C:\dev-learning\ticket-ui
npm.cmd run dev
```

| 주소 | 내용 |
| --- | --- |
| <http://localhost:5173/> | 프런트엔드 (Vite) |
| <http://localhost:8080/api/tickets> | 티켓 API |
| `POST http://localhost:8080/api/ai/ticket-advice` | AI 조언 (본문 예: `{"title":"프린터 연결 오류"}`) |
| `POST http://localhost:8080/api/ai/ticket-advice/structured` | AI 조언 (구조화 응답) |

Ollama 데모 프로필 실행(기존 Spring 서버는 먼저 종료):

```powershell
cd C:\dev-learning\ticket-api
.\mvnw.cmd clean spring-boot:run "-Dspring-boot.run.profiles=ai-demo"
```

## 6. 테스트와 빌드

```powershell
cd C:\dev-learning
npm.cmd run typecheck
npm.cmd test

cd C:\dev-learning\ticket-ui
npm.cmd test
npm.cmd run lint
npm.cmd run build

cd C:\dev-learning\ticket-api
.\mvnw.cmd test
```

전체 패키징(프런트 빌드 → static 복사 → 실행 가능한 JAR):

```powershell
cd C:\dev-learning
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\build.ps1
cd ticket-api
java -jar .\target\ticket-api-0.0.1-SNAPSHOT.jar
```

이 PC에서 확인한 결과(2026-11-14): 루트 `typecheck` 통과, `ticket-ui` 테스트 18개 통과, `ticket-api` `mvnw test` 통과. `build.ps1` 전체 패키징과 서버 실행은 아직 해 보지 않았습니다.

## 7. 직접 해야 할 것

- **Postman**(선택): README의 AI API 호출 실습에 사용합니다. <https://www.postman.com/downloads/> 에서 설치하거나, PowerShell의 `Invoke-RestMethod`로 대신할 수 있습니다.
- **Git 사용자 정보**: 커밋하려면 한 번 설정해야 합니다.

```powershell
git config --global user.name "이름"
git config --global user.email "이메일"
```

## 8. 문제 해결

| 증상 | 확인할 것 |
| --- | --- |
| `git`, `node`, `java`를 찾을 수 없음 | 터미널/VS Code를 완전히 종료하고 다시 실행 |
| `mvnw.cmd`가 JAVA_HOME 오류 | `$env:JAVA_HOME`이 JDK 폴더(bin이 아님)를 가리키는지 확인 |
| AI 요청이 503 | Ollama 앱이 꺼져 있음. 시작 메뉴에서 실행 |
| AI 요청이 504 | 모델 응답이 `read-timeout`(120s)을 넘음. `ollama ps`로 `100% GPU`인지 확인 |
| 모델을 찾을 수 없다는 오류 | `ollama list`의 이름과 `$env:OLLAMA_CHAT_MODEL`이 같은지 확인 |
| `build.ps1` 실행 정책 오류 | 6절의 `-ExecutionPolicy Bypass` 명령 사용 |
