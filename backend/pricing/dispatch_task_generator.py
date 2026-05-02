"""Dispatch task generation for pricing-backed operations.

This module provides:
- DispatchTask: a dataclass describing an activation of a device in the field.
- DispatchTaskGenerator: computes candidate tasks from demand forecasts, inventory
  state and weather data, with simple prioritization and 48h windowing.
"""

from __future__ import annotations

import json
from dataclasses import dataclass, asdict
from typing import Dict, List, Optional, Tuple
from datetime import datetime, timedelta


@dataclass
class DispatchTask:
    base_id: str
    device_id: str
    task_type: str  # BATTERY_REPLACE | REDEPLOY
    suggested_time_window: Tuple[str, str]  # ISO format timestamps (start, end)
    priority: str  # HIGH | MEDIUM | LOW
    reason: str

    def to_dict(self) -> Dict:
        return asdict(self)

    def to_json(self) -> str:
        return json.dumps(self.to_dict())

    @staticmethod
    def from_json(data: str) -> "DispatchTask":
        obj = json.loads(data)
        return DispatchTask(**obj)


class DispatchTaskGenerator:
    """Generate candidate dispatch tasks from inputs.

    The generator is intentionally small and self-contained to avoid external
    dependencies. It relies on simple heuristics to produce a small set of
    candidate tasks that can later be filtered by operational constraints.
    """

    def __init__(self) -> None:
        pass

    def generate_tasks(
        self,
        demand_forecast: Dict[str, float],
        inventory: Dict[str, Dict[str, object]],
        weather_data: Dict[str, object],
    ) -> List[DispatchTask]:
        """Create candidate DispatchTask objects.

        Parameters:
        - demand_forecast: mapping base_id -> expected demand (float) for the near future.
        - inventory: mapping base_id -> state dict with keys:
            - battery_level: int (0-100)
            - device_id: str
            - inventory_count: int
        - weather_data: additional context (not heavily used by heuristic but kept for extensibility)
        """
        tasks: List[DispatchTask] = []
        now = datetime.utcnow()
        horizon = now + timedelta(hours=48)

        for base_id, state in inventory.items():
            battery_level = int(state.get("battery_level", 100))
            device_id = str(state.get("device_id", f"{base_id}-device"))
            inventory_count = int(state.get("inventory_count", 0))

            forecast = float(demand_forecast.get(base_id, 0.0))
            demand_gap = max(0.0, forecast - inventory_count)

            # Determine task type based on battery and demand.
            task_type = None
            if battery_level < 20:
                task_type = "BATTERY_REPLACE"
            elif demand_gap > 0:
                task_type = "REDEPLOY"

            if task_type is None:
                continue

            # Time window suggestion: a window within the next 48 hours. If the
            # forecast is already high, pick the earliest available window.
            # We attempt to find a concrete start time from forecast if possible.
            # If unavailable, fall back to the next hour.
            start_time = now
            end_time = now + timedelta(hours=1)
            if forecast > 0:
                # Prefer 0th hour window from now
                start_time = now
                end_time = now + timedelta(hours=1)
            window_start = start_time.isoformat() + "Z"
            window_end = end_time.isoformat() + "Z"

            priority = "HIGH" if (battery_level < 20 or demand_gap > 0.3 * max(1, inventory_count)) else "MEDIUM"
            reason = (
                f"{task_type} required for base {base_id} due to {'low battery' if battery_level<20 else 'demand gap'}"
            )

            task = DispatchTask(
                base_id=base_id,
                device_id=device_id,
                task_type=task_type,
                suggested_time_window=(window_start, window_end),
                priority=priority,
                reason=reason,
            )
            tasks.append(task)

        return tasks

    def filter_valid_tasks(self,
                           tasks: List[DispatchTask],
                           operational_constraints: Optional[Dict[str, object]] = None) -> List[DispatchTask]:
        """Filter tasks against simple operational constraints.

        Currently supports a max_concurrent_tasks cap and basic priority ordering.
        """
        if not tasks:
            return []
        constraints = operational_constraints or {}
        max_concurrent = int(constraints.get("max_concurrent_tasks", len(tasks)))

        # Sort by priority (HIGH > MEDIUM > LOW) and then by earliest window start
        priority_order = {"HIGH": 0, "MEDIUM": 1, "LOW": 2}
        tasks_sorted = sorted(
            tasks,
            key=lambda t: (priority_order.get(t.priority, 2), t.suggested_time_window[0])
        )
        return tasks_sorted[:max_concurrent]

    # Battery replacement reminder helper (returns human-friendly notes)
    def battery_reminder(self, tasks: List[DispatchTask]) -> List[str]:
        reminders: List[str] = []
        for t in tasks:
            if t.task_type == "BATTERY_REPLACE":
                reminders.append(f"Reminder: Replace battery for device {t.device_id} (base {t.base_id}). Window {t.suggested_time_window[0]} - {t.suggested_time_window[1]}.")
        return reminders
