from datetime import datetime, timezone
from typing import Literal, Optional
from uuid import UUID

from fastapi import APIRouter, Query, Request

from .models import ApiError, Measurement, Series

router = APIRouter(prefix="/api/v1")


def repo_of(request: Request):
    return request.app.state.repo


@router.get("/telemetry/devices/{device_id}/latest", response_model_by_alias=True)
def get_latest(device_id: UUID, request: Request) -> list[Measurement]:
    items = repo_of(request).latest(device_id)
    if not items:
        raise ApiError(404, "NO_DATA", "Для устройства нет показаний")
    return items


@router.get("/telemetry/devices/{device_id}")
def get_history(
    device_id: UUID,
    request: Request,
    metric: str,
    start: datetime = Query(alias="from"),
    end: Optional[datetime] = Query(default=None, alias="to"),
    interval: Optional[Literal["1m", "5m", "1h", "1d"]] = None,
    limit: int = Query(default=1000, ge=1, le=10000),
) -> Series:
    if start.tzinfo is None:
        start = start.replace(tzinfo=timezone.utc)
    if end is None:
        end = datetime.now(timezone.utc)
    elif end.tzinfo is None:
        end = end.replace(tzinfo=timezone.utc)
    if start >= end:
        raise ApiError(400, "VALIDATION_ERROR", "from должен быть раньше to")

    series = repo_of(request).history(device_id, metric, start, end, interval, limit)
    if not series.points:
        raise ApiError(404, "NO_DATA", "Для устройства нет показаний")
    return series


@router.post("/measurements", status_code=201)
def add_measurement(m: Measurement, request: Request) -> Measurement:
    repo_of(request).add(m)
    return m
