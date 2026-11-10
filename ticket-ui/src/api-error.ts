
export async function readErrorMessage(
  response: Response,
  fallbackMessage: string
): Promise<string> {

  try {
    const data: unknown = await response.json();

    if (
      typeof data === "object" &&
      data !== null &&
      "message" in data &&
      typeof data.message === "string" &&
      data.message.trim().length > 0
    ) {
      return data.message.trim();
    }
  } catch {
    // 빈 응답이나 HTML처럼 JSON으로 읽을 수 없는 경우
    return fallbackMessage;
  }
    // JSON이지만 message가 없거나 올바른 문자열이 아닌 경우
    return fallbackMessage;

}