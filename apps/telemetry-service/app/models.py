from datetime import datetime
from typing import Optional
from uuid import UUID

from pydantic import BaseModel, ConfigDict, Field
from pydantic.alias_generators import to_camel


class CamelModel(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)


class Measurement(CamelModel):
    device_id: UUID
    metric: str = Field(min_length=1)
    value: float
    unit: Optional[str] = None
    measured_at: datetime


class KafkaMeasurement(Measurement):
    event_id: UUID
    house_id: Optional[UUID] = None


class Point(CamelModel):
    time: datetime
    value: float


class Series(CamelModel):
    device_id: UUID
    metric: str
    unit: Optional[str] = None
    interval: Optional[str] = None
    points: list[Point]


class ApiError(Exception):
    def __init__(self, status: int, code: str, message: str):
        self.status = status
        self.code = code
        self.message = message
