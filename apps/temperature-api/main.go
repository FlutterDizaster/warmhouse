package main

import (
	"encoding/json"
	"log"
	"math"
	"math/rand"
	"net/http"
	"os"
	"strings"
	"time"
)

type temperature struct {
	Value       float64   `json:"value"`
	Unit        string    `json:"unit"`
	Timestamp   time.Time `json:"timestamp"`
	Location    string    `json:"location"`
	Status      string    `json:"status"`
	SensorID    string    `json:"sensor_id"`
	SensorType  string    `json:"sensor_type"`
	Description string    `json:"description"`
}

var locationBySensor = map[string]string{
	"1": "Living Room",
	"2": "Bedroom",
	"3": "Kitchen",
}

func sensorByLocation(location string) string {
	for id, loc := range locationBySensor {
		if loc == location {
			return id
		}
	}
	return "0"
}

func main() {
	port := os.Getenv("PORT")
	if port == "" {
		port = "8081"
	}

	mux := http.NewServeMux()
	mux.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) {
		writeJSON(w, map[string]string{"status": "ok"})
	})
	// монолит ходит сюда двумя способами: /temperature?location=Kitchen и /temperature/3
	mux.HandleFunc("/temperature", handleTemperature)
	mux.HandleFunc("/temperature/", handleTemperature)

	log.Printf("temperature-api listening on :%s", port)
	log.Fatal(http.ListenAndServe(":"+port, mux))
}

func handleTemperature(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		w.WriteHeader(http.StatusMethodNotAllowed)
		return
	}

	location := r.URL.Query().Get("location")
	sensorID := strings.Trim(strings.TrimPrefix(r.URL.Path, "/temperature"), "/")

	if location == "" {
		location = locationBySensor[sensorID]
		if location == "" {
			location = "Unknown"
		}
	}
	if sensorID == "" {
		sensorID = sensorByLocation(location)
	}

	writeJSON(w, temperature{
		Value:       math.Round((18+rand.Float64()*8)*10) / 10,
		Unit:        "°C",
		Timestamp:   time.Now().UTC(),
		Location:    location,
		Status:      "active",
		SensorID:    sensorID,
		SensorType:  "temperature",
		Description: "Temperature sensor in " + location,
	})
}

func writeJSON(w http.ResponseWriter, v any) {
	w.Header().Set("Content-Type", "application/json")
	if err := json.NewEncoder(w).Encode(v); err != nil {
		log.Printf("write response: %v", err)
	}
}
