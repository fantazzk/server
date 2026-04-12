from __future__ import annotations

import importlib.util
import tempfile
import textwrap
import unittest
from pathlib import Path


SCRIPT_PATH = Path(__file__).resolve().parents[1] / "render_modulith_pr_comment.py"
SPEC = importlib.util.spec_from_file_location("render_modulith_pr_comment", SCRIPT_PATH)
MODULE = importlib.util.module_from_spec(SPEC)
assert SPEC.loader is not None
SPEC.loader.exec_module(MODULE)


class ModulithPr본문렌더링테스트(unittest.TestCase):
    def test_본문_섹션은_mermaid와_관리_마커를_포함한다(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            input_dir = Path(temp_dir)
            input_dir.joinpath("components.puml").write_text(
                textwrap.dedent(
                    """\
                    @startuml
                    Component(foo, "Room", $techn="Module")
                    Component(bar, "Template", $techn="Module")
                    Rel(foo, bar, "uses")
                    @enduml
                    """
                ),
                encoding="utf-8",
            )

            rendered = MODULE.render_pr_body_section(input_dir)

            self.assertIn("<!-- modulith-pr-body:start -->", rendered)
            self.assertIn("<!-- modulith-pr-body:end -->", rendered)
            self.assertIn("```mermaid", rendered)
            self.assertIn('room["Room"]', rendered)
            self.assertIn('room -->|"uses"| template', rendered)

    def test_본문_섹션은_산출물이_없을때도_안내_문구를_보존한다(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            rendered = MODULE.render_pr_body_section(Path(temp_dir) / "missing")

            self.assertIn("<!-- modulith-pr-body:start -->", rendered)
            self.assertIn("Spring Modulith 구조도", rendered)
            self.assertIn("산출물을 찾지 못했습니다", rendered)


if __name__ == "__main__":
    unittest.main()
