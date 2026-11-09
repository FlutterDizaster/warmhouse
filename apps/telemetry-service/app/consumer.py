import json
import logging
import threading

from confluent_kafka import Consumer
from pydantic import ValidationError

from .models import KafkaMeasurement
from .repository import MeasurementRepository

log = logging.getLogger("telemetry.consumer")

TOPIC = "telemetry.measured"


class MeasurementConsumer:
    def __init__(self, bootstrap_servers: str, repo: MeasurementRepository):
        self.repo = repo
        self.consumer = Consumer({
            "bootstrap.servers": bootstrap_servers,
            "group.id": "telemetry-service",
            "auto.offset.reset": "earliest",
        })
        self.stop_event = threading.Event()
        self.thread = threading.Thread(target=self._run, name="kafka-consumer", daemon=True)

    def start(self):
        self.thread.start()

    def stop(self):
        self.stop_event.set()
        self.thread.join(timeout=10)

    def _run(self):
        self.consumer.subscribe([TOPIC])
        try:
            while not self.stop_event.is_set():
                msg = self.consumer.poll(1.0)
                if msg is None:
                    continue
                if msg.error():
                    log.warning("kafka: %s", msg.error())
                    continue
                self._handle(msg.value())
        finally:
            self.consumer.close()

    def _handle(self, raw: bytes):
        try:
            m = KafkaMeasurement.model_validate(json.loads(raw))
        except (ValueError, ValidationError) as e:
            log.warning("skip bad message: %s", e)
            return
        try:
            if not self.repo.add(m, m.event_id):
                log.info("duplicate event %s", m.event_id)
        except Exception:
            log.exception("failed to save event %s", m.event_id)
