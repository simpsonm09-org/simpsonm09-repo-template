from __future__ import annotations

from app import greet


def test_greets_by_name() -> None:
    assert greet("world") == "Hello, world!"
