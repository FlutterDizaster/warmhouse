package events

import (
	"context"
	"encoding/json"
	"log"
	"strconv"
	"strings"
	"time"

	"github.com/google/uuid"
	"github.com/segmentio/kafka-go"

	"smarthome/models"
)

const (
	TopicMeasured         = "telemetry.measured"
	TopicSensorRegistered = "legacy.sensor.registered"
)

// Без KAFKA_BROKERS ничего не отправляем, монолит работает как раньше.
type Publisher struct {
	writer *kafka.Writer
}

func NewPublisher(brokers string) *Publisher {
	if brokers == "" {
		log.Println("KAFKA_BROKERS is empty, events are disabled")
		return &Publisher{}
	}
	return &Publisher{
		writer: &kafka.Writer{
			Addr:                   kafka.TCP(strings.Split(brokers, ",")...),
			Balancer:               &kafka.Hash{},
			Async:                  true,
			AllowAutoTopicCreation: true,
			BatchTimeout:           50 * time.Millisecond,
			Completion: func(messages []kafka.Message, err error) {
				if err != nil {
					log.Printf("failed to publish %d events: %v", len(messages), err)
				}
			},
		},
	}
}

// id устройства считаем из номера датчика, чтобы на время миграции не держать таблицу соответствий.
func DeviceID(sensorID int) string {
	return uuid.NewSHA1(uuid.NameSpaceOID, []byte("warmhouse/sensor/"+strconv.Itoa(sensorID))).String()
}

func (p *Publisher) SensorRegistered(s models.Sensor) {
	p.publish(TopicSensorRegistered, DeviceID(s.ID), map[string]any{
		"sensorId":     s.ID,
		"deviceId":     DeviceID(s.ID),
		"name":         s.Name,
		"type":         s.Type,
		"location":     s.Location,
		"unit":         s.Unit,
		"registeredAt": s.CreatedAt.UTC(),
	})
}

func (p *Publisher) Measured(sensorID int, value float64, unit string, at time.Time) {
	p.publish(TopicMeasured, DeviceID(sensorID), map[string]any{
		"eventId":    uuid.NewString(),
		"deviceId":   DeviceID(sensorID),
		"metric":     "temperature",
		"value":      value,
		"unit":       unit,
		"measuredAt": at.UTC(),
	})
}

func (p *Publisher) publish(topic, key string, payload any) {
	if p.writer == nil {
		return
	}
	body, err := json.Marshal(payload)
	if err != nil {
		log.Printf("marshal event for %s: %v", topic, err)
		return
	}
	err = p.writer.WriteMessages(context.Background(), kafka.Message{Topic: topic, Key: []byte(key), Value: body})
	if err != nil {
		log.Printf("publish to %s: %v", topic, err)
	}
}

func (p *Publisher) Close() {
	if p.writer != nil {
		p.writer.Close()
	}
}
