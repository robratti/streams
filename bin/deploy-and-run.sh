#!/bin/bash

# Exit immediately if any command fails
set -e

# Variables
APP_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)" # Resolve app root dynamically
KUBE_NAMESPACE="kafka"
KAFKA_SERVICE="broker-service"
KAFKA_BOOTSTRAP="broker-service:9092"
SPRING_BOOT_PORT=8080
APP_NAME="stream-table"

# PID of the spring boot process
SPRING_PID=0

# Function to create namespace if it doesn't exist
create_namespace() {
    if ! kubectl get namespace "$KUBE_NAMESPACE" &>/dev/null; then
        echo "Namespace $KUBE_NAMESPACE does not exist. Creating it..."
        kubectl create namespace "$KUBE_NAMESPACE"
    else
        echo "Namespace $KUBE_NAMESPACE already exists."
    fi
}

# Function for gracefully terminating processes
terminate_process() {
    local pid=$1
    local name=$2
    local timeout=${3:-30}  # Optional timeout, defaults to 30 seconds if not provided

    if ps -p "$pid" > /dev/null; then
        echo "Attempting to terminate $name (PID: $pid) gracefully..."
        kill "$pid"

        # Wait for the process to terminate, checking periodically
        local elapsed=0
        while ps -p "$pid" > /dev/null; do
            if [ "$elapsed" -ge "$timeout" ]; then
                echo "$name did not terminate within $timeout seconds. Forcing termination..."
                kill -9 "$pid"
                break
            fi
            echo "Waiting for $name (PID: $pid) to terminate..."
            sleep 1
            elapsed=$((elapsed + 1))
        done

        if ! ps -p "$pid" > /dev/null; then
            echo "$name (PID: $pid) terminated successfully."
        fi
    else
        echo "$name (PID: $pid) is not running."
    fi
}

# Function to clean up resources
cleanup() {
    echo "### Cleaning up resources..."

    # Kill the Spring Boot process if running
    terminate_process $SPRING_PID $APP_NAME 60

    # Kubernetes cleanup...
    # Kill the port forwarding process
    terminate_process "$PORT_FORWARD_PID" "9092-port-forwarding" 30
    echo "Deleting all Kubernetes deployments in namespace $KUBE_NAMESPACE..."
    kubectl delete all --all --namespace "$KUBE_NAMESPACE" || echo "No resources to delete."

    echo "Clearing target folder..."
    rm -rf "$APP_ROOT/target"

    echo "Cleanup complete."
}

# Trap for script interruption
trap cleanup EXIT SIGINT SIGTERM

# Function to wait for Spring Boot application to be ready
wait_for_spring_boot() {
    local url="http://localhost:$SPRING_BOOT_PORT/actuator/health"
    echo "### Waiting for Spring Boot application to be available at $url"
    until curl --silent --fail "$url" | jq -e '.status == "UP"' > /dev/null; do
        echo "Waiting for Spring Boot application..."
        sleep 2
    done
    echo "Spring Boot application is UP!"
}

echo "### Step 1: Build the application"
(cd "$APP_ROOT" && mvn clean install)

echo "### Step 2: Deploy Kafka Cluster in Kubernetes"

create_namespace
kubectl apply -k "$APP_ROOT/kubernetes/kafka"

echo "### Step 3: Wait for Kafka services to be ready"
kubectl wait --namespace $KUBE_NAMESPACE --for=condition=ready pod --all --timeout=120s

echo "### Step 4: Forward Kafka broker port to localhost"
kubectl port-forward --namespace $KUBE_NAMESPACE service/$KAFKA_SERVICE 9092:9092 &
PORT_FORWARD_PID=$! # Capture the PORT_FORWARD_PROCESS process PID
echo "Port 9092 forwarded with PID: $PORT_FORWARD_PID"

# Wait briefly to ensure port-forwarding is established
sleep 5

echo "### Step 5: Create Kafka topics"
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP --create --topic inventory --replication-factor 1 --partitions 3 || true
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP --create --topic order --replication-factor 1 --partitions 3 || true
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP --create --topic aggregated-inventory --replication-factor 1 || true

echo "### Step 6: Configure cleanup policy for 'aggregated-inventory'"
kafka-configs --bootstrap-server $KAFKA_BOOTSTRAP --entity-type topics --entity-name aggregated-inventory --alter --add-config cleanup.policy=compact

echo "### Step 7: Reset offsets for the application if it exists"
kafka-consumer-groups --bootstrap-server $KAFKA_BOOTSTRAP --list --all-groups | grep -q $APP_NAME && \
kafka-consumer-groups --bootstrap-server $KAFKA_BOOTSTRAP --reset-offsets --group $APP_NAME --topic inventory --to-earliest --execute || \
echo "No existing consumer group found for $APP_NAME, skipping offset reset."

echo "### Step 8: Run the Spring Boot application"
cd "$APP_ROOT" || exit 1
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local" &
SPRING_PID=$! # Capture the Spring Boot process PID

echo "Spring run with PID: $SPRING_PID"

# Wait for the Spring Boot application to be ready
wait_for_spring_boot

echo "### Step 9: Seed the application with initial values"
curl -X POST http://localhost:$SPRING_BOOT_PORT/batch \
    -H "Content-Type: application/json" \
    -d '{"size": 50}'

echo "### Deployment and initialization complete!"
wait $SPRING_PID
