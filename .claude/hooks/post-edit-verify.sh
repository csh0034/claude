#!/bin/bash
# post-edit-verify.sh
# PostToolUse 훅: .kt 파일 변경 시 자동 테스트 실행

INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // empty')
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')

# Edit/Write 도구에서 .kt 파일 변경 시에만 실행
if [[ "$TOOL_NAME" != "Edit" && "$TOOL_NAME" != "Write" ]]; then
  exit 0
fi

if [[ ! "$FILE_PATH" == *.kt ]]; then
  exit 0
fi

# 테스트 파일 자체 수정 시에는 전체 테스트가 아닌 해당 테스트만 실행할 수 있도록 빠른 검증
echo "🔍 .kt 파일 변경 감지: $FILE_PATH" >&2
echo "⏳ 자동 테스트 실행 중..." >&2

cd "$CLAUDE_PROJECT_DIR" || exit 0

OUTPUT=$(./gradlew test --fail-fast --quiet 2>&1)
EXIT_CODE=$?

if [[ $EXIT_CODE -ne 0 ]]; then
  echo "❌ 테스트 실패! 아래 오류를 확인하고 수정하세요:" >&2
  echo "$OUTPUT" | tail -30 >&2
  exit 1
fi

echo "✅ 모든 테스트 통과" >&2
exit 0
