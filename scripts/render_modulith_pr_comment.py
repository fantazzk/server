#!/usr/bin/env python3

from __future__ import annotations

import argparse
import html
import re
from pathlib import Path


COMMENT_MARKER = "<!-- modulith-pr-comment -->"


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Render Spring Modulith documentation into a PR comment."
    )
    parser.add_argument(
        "--input-dir",
        default="build/spring-modulith",
        help="Directory containing Spring Modulith generated files.",
    )
    return parser.parse_args()


def parse_components(puml_path: Path) -> tuple[list[str], list[tuple[str, str, str]]]:
    if not puml_path.exists():
        return [], []

    components: dict[str, str] = {}
    relationships: list[tuple[str, str, str]] = []

    component_pattern = re.compile(r'Component\(([^,]+),\s*"([^"]+)"')
    relationship_pattern = re.compile(r'Rel\(([^,]+),\s*([^,]+),\s*"([^"]*)"')

    for line in puml_path.read_text(encoding="utf-8").splitlines():
        component_match = component_pattern.search(line)
        if component_match:
            alias = component_match.group(1).strip()
            components[alias] = component_match.group(2).strip()
            continue

        relationship_match = relationship_pattern.search(line)
        if relationship_match:
            source_alias = relationship_match.group(1).strip()
            target_alias = relationship_match.group(2).strip()
            label = relationship_match.group(3).strip() or "depends on"
            relationships.append(
                (
                    components.get(source_alias, source_alias),
                    components.get(target_alias, target_alias),
                    label,
                )
            )

    modules = sorted(set(components.values()))
    return modules, relationships


def parse_module_canvas(adoc_path: Path) -> dict[str, str]:
    if not adoc_path.exists():
        return {}

    rows: dict[str, str] = {}
    current_key: str | None = None
    current_value_lines: list[str] = []
    inside_table = False

    def flush() -> None:
        nonlocal current_key, current_value_lines
        if current_key is None:
            return
        value = "\n".join(line.rstrip() for line in current_value_lines).strip()
        rows[current_key] = value
        current_key = None
        current_value_lines = []

    for raw_line in adoc_path.read_text(encoding="utf-8").splitlines():
        line = raw_line.rstrip()

        if line == "|===":
            if inside_table:
                flush()
            inside_table = not inside_table
            continue

        if not inside_table:
            continue

        if line.startswith("|"):
            content = line[1:].strip()
            if current_key is None:
                current_key = content
                continue

            if not current_value_lines:
                current_value_lines.append(content)
                continue

            flush()
            current_key = content
            continue

        if current_key is not None and current_value_lines:
            current_value_lines.append(line)

    flush()
    return rows


def mermaid_id(name: str) -> str:
    normalized = re.sub(r"[^a-zA-Z0-9]+", "_", name).strip("_").lower()
    return normalized or "module"


def render_mermaid(modules: list[str], relationships: list[tuple[str, str, str]]) -> str:
    lines = ["```mermaid", "flowchart LR"]

    for module in modules:
        lines.append(f'    {mermaid_id(module)}["{module}"]')

    if relationships:
        for source, target, label in relationships:
            lines.append(
                f'    {mermaid_id(source)} -->|"{label}"| {mermaid_id(target)}'
            )
    else:
        lines.append("    %% No cross-module dependencies detected.")

    lines.append("```")
    return "\n".join(lines)


def render_module_details(module_name: str, rows: dict[str, str]) -> str:
    if not rows:
        return (
            f"<details>\n"
            f"<summary><code>{html.escape(module_name)}</code></summary>\n\n"
            f"생성된 모듈 세부 정보가 없습니다.\n"
            f"</details>"
        )

    lines = [
        "<details>",
        f"<summary><code>{html.escape(module_name)}</code></summary>",
        "",
    ]

    for key, value in rows.items():
        lines.append(f"**{html.escape(key)}**")
        lines.append("")
        lines.append(value or "_없음_")
        lines.append("")

    lines.append("</details>")
    return "\n".join(lines)


def render_comment(input_dir: Path) -> str:
    modules, relationships = parse_components(input_dir / "components.puml")
    module_rows: dict[str, dict[str, str]] = {}

    for canvas_path in sorted(input_dir.glob("module-*.adoc")):
        module_name = canvas_path.stem.removeprefix("module-").replace("-", " ").title()
        module_rows[module_name] = parse_module_canvas(canvas_path)

    available_modules = sorted(set(modules) | set(module_rows.keys()))

    lines = [
        COMMENT_MARKER,
        "## Spring Modulith 구조 보고서",
        "",
    ]

    if not input_dir.exists() or not available_modules:
        lines.extend(
            [
                "이번 실행에서는 `build/spring-modulith` 산출물을 찾지 못했습니다.",
                "`./gradlew check` 단계가 Modulith 문서 생성 전에 실패했는지 확인해 주세요.",
            ]
        )
        return "\n".join(lines).strip() + "\n"

    lines.extend(
        [
            "CI가 생성한 현재 모듈 구조와 관찰된 의존성입니다.",
            "",
            "### 모듈 그래프",
            "",
            render_mermaid(available_modules, relationships),
            "",
            "### 모듈 의존성",
            "",
        ]
    )

    if relationships:
        for source, target, label in relationships:
            lines.append(f"- `{source}` -> `{target}` (`{label}`)")
    else:
        lines.append("- 감지된 모듈 간 의존성이 없습니다.")

    lines.extend(
        [
            "",
            "### 모듈 세부사항",
            "",
        ]
    )

    for module_name in available_modules:
        lines.append(render_module_details(module_name, module_rows.get(module_name, {})))
        lines.append("")

    return "\n".join(lines).strip() + "\n"


def main() -> None:
    args = parse_args()
    input_dir = Path(args.input_dir)
    print(render_comment(input_dir), end="")


if __name__ == "__main__":
    main()
