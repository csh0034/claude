#!/bin/bash
# protect-files.sh

INPUT=$(cat)
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')
COMMAND=$(echo "$INPUT" | jq -r '.tool_input.command // empty')

PROTECTED_PATTERNS=(".env" "README.md" ".git/")

for pattern in "${PROTECTED_PATTERNS[@]}"; do
  if [[ -n "$FILE_PATH" && "$FILE_PATH" == *"$pattern"* ]]; then
    echo "Blocked: $FILE_PATH matches protected pattern '$pattern'" >&2
    exit 2
  fi
  if [[ -n "$COMMAND" && "$COMMAND" == *"$pattern"* ]]; then
    echo "Blocked: command references protected pattern '$pattern'" >&2
    exit 2
  fi
done

exit 0
