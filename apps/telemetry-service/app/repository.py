from datetime import datetime
from typing import Optional
from uuid import UUID

from psycopg.rows import dict_row
from psycopg_pool import ConnectionPool

from .models import Measurement, Point, Series

SCHEMA = """
CREATE TABLE IF NOT EXISTS measurements (
    device_id   uuid NOT NULL,
    metric      text NOT NULL,
    value       double precision NOT NULL,
    unit        text,
    measured_at timestamptz NOT NULL,
    received_at timestamptz NOT NULL DEFAULT now(),
    event_id    uuid UNIQUE
);
CREATE INDEX IF NOT EXISTS measurements_device_metric_time_idx
    ON measurements (device_id, metric, measured_at DESC);
"""

INTERVALS = {"1m": "1 minute", "5m": "5 minutes", "1h": "1 hour", "1d": "1 day"}


class MeasurementRepository:
    def __init__(self, dsn: str):
        self.pool = ConnectionPool(dsn, min_size=1, max_size=5, open=False,
                                   kwargs={"row_factory": dict_row})

    def open(self):
        self.pool.open(wait=True, timeout=30)
        with self.pool.connection() as conn:
            conn.execute(SCHEMA)

    def close(self):
        self.pool.close()

    def add(self, m: Measurement, event_id: Optional[UUID] = None) -> bool:
        with self.pool.connection() as conn:
            cur = conn.execute(
                """INSERT INTO measurements (device_id, metric, value, unit, measured_at, event_id)
                   VALUES (%s, %s, %s, %s, %s, %s)
                   ON CONFLICT DO NOTHING""",
                (m.device_id, m.metric, m.value, m.unit, m.measured_at, event_id),
            )
            return cur.rowcount > 0

    def latest(self, device_id: UUID) -> list[Measurement]:
        with self.pool.connection() as conn:
            rows = conn.execute(
                """SELECT DISTINCT ON (metric) device_id, metric, value, unit, measured_at
                   FROM measurements WHERE device_id = %s
                   ORDER BY metric, measured_at DESC""",
                (device_id,),
            ).fetchall()
        return [Measurement(**r) for r in rows]

    def history(self, device_id: UUID, metric: str, start: datetime, end: datetime,
                interval: Optional[str], limit: int) -> Series:
        with self.pool.connection() as conn:
            if interval:
                rows = conn.execute(
                    """SELECT * FROM (
                           SELECT date_bin(%s::interval, measured_at, TIMESTAMPTZ '2000-01-01 00:00:00+00') AS time,
                                  avg(value) AS value
                           FROM measurements
                           WHERE device_id = %s AND metric = %s AND measured_at >= %s AND measured_at < %s
                           GROUP BY 1 ORDER BY 1 DESC LIMIT %s
                       ) t ORDER BY time""",
                    (INTERVALS[interval], device_id, metric, start, end, limit),
                ).fetchall()
            else:
                rows = conn.execute(
                    """SELECT * FROM (
                           SELECT measured_at AS time, value
                           FROM measurements
                           WHERE device_id = %s AND metric = %s AND measured_at >= %s AND measured_at < %s
                           ORDER BY measured_at DESC LIMIT %s
                       ) t ORDER BY time""",
                    (device_id, metric, start, end, limit),
                ).fetchall()
            unit = conn.execute(
                """SELECT unit FROM measurements WHERE device_id = %s AND metric = %s
                   ORDER BY measured_at DESC LIMIT 1""",
                (device_id, metric),
            ).fetchone()
        return Series(device_id=device_id, metric=metric, unit=unit["unit"] if unit else None,
                      interval=interval, points=[Point(**r) for r in rows])
