import logging
import os
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse

from .api import router
from .consumer import MeasurementConsumer
from .models import ApiError
from .repository import MeasurementRepository

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s: %(message)s")
log = logging.getLogger("telemetry")

DATABASE_URL = os.getenv("DATABASE_URL", "postgresql://postgres:postgres@postgres:5432/telemetry")
KAFKA_BOOTSTRAP_SERVERS = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092")


@asynccontextmanager
async def lifespan(app: FastAPI):
    repo = MeasurementRepository(DATABASE_URL)
    repo.open()
    consumer = MeasurementConsumer(KAFKA_BOOTSTRAP_SERVERS, repo)
    consumer.start()
    app.state.repo = repo
    yield
    consumer.stop()
    repo.close()


app = FastAPI(title="Telemetry API", version="1.0.0", lifespan=lifespan)
app.include_router(router)


def error_response(status: int, code: str, message: str) -> JSONResponse:
    return JSONResponse(status_code=status, content={"code": code, "message": message})


@app.exception_handler(ApiError)
async def api_error_handler(request: Request, exc: ApiError):
    return error_response(exc.status, exc.code, exc.message)


@app.exception_handler(RequestValidationError)
async def validation_handler(request: Request, exc: RequestValidationError):
    first = exc.errors()[0]
    field = ".".join(str(p) for p in first["loc"] if p != "body")
    return error_response(400, "VALIDATION_ERROR", f"{field}: {first['msg']}")


@app.exception_handler(Exception)
async def unhandled_handler(request: Request, exc: Exception):
    log.exception("unhandled error")
    return error_response(500, "INTERNAL_ERROR", "Что-то пошло не так, попробуйте позже")


@app.get("/health")
def health():
    return {"status": "ok"}
