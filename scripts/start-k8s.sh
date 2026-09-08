#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

KIND_CLUSTER_NAME="${KIND_CLUSTER_NAME:-c4h-learn-k8s}"
IMAGE_NAME="${IMAGE_NAME:-springboot-api:java25}"
NAMESPACE="${NAMESPACE:-default}"
DEPLOYMENT_NAME="${DEPLOYMENT_NAME:-springboot-deployment}"
SERVICE_NAME="${SERVICE_NAME:-springboot-service}"
LOCAL_PORT="${LOCAL_PORT:-9001}"
SERVICE_PORT="${SERVICE_PORT:-9001}"
WAIT_TIMEOUT="${WAIT_TIMEOUT:-180s}"
PORT_FORWARD_MODE="${PORT_FORWARD_MODE:-foreground}" # foreground | background | none
DRY_RUN="${DRY_RUN:-false}"

PID_DIR="${ROOT_DIR}/.tmp"
PID_FILE="${PID_DIR}/k8s-port-forward.pid"
LOG_FILE="${PID_DIR}/k8s-port-forward.log"

run_cmd() {
  if [[ "${DRY_RUN}" == "true" ]]; then
    echo "[dry-run] $*"
    return 0
  fi
  "$@"
}

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "ERROR: '$1' is not installed or not on PATH."
    exit 1
  fi
}

echo "==> Validating required tools"
require_command ./gradlew
require_command docker
require_command kind
require_command kubectl

if [[ ! -f "k8s/.env.secret" ]]; then
  echo "ERROR: Missing k8s/.env.secret"
  echo "Create it with: cp k8s/.env.secret.example k8s/.env.secret"
  exit 1
fi

echo "==> Building application jar"
run_cmd ./gradlew clean bootJar

echo "==> Building Docker image ${IMAGE_NAME}"
run_cmd docker build -t "${IMAGE_NAME}" .

echo "==> Loading image into kind cluster ${KIND_CLUSTER_NAME}"
run_cmd kind load docker-image "${IMAGE_NAME}" --name "${KIND_CLUSTER_NAME}"

echo "==> Applying Kubernetes manifests via kustomize"
run_cmd kubectl apply -k k8s/

echo "==> Waiting for rollout of deployment/${DEPLOYMENT_NAME}"
run_cmd kubectl -n "${NAMESPACE}" rollout status deployment/"${DEPLOYMENT_NAME}" --timeout="${WAIT_TIMEOUT}"

echo "==> Current pods"
run_cmd kubectl -n "${NAMESPACE}" get pods -l app=springboot-api -o wide

case "${PORT_FORWARD_MODE}" in
  foreground)
    echo "==> Starting foreground port-forward ${LOCAL_PORT}:${SERVICE_PORT}"
    echo "Press Ctrl+C to stop port-forward."
    run_cmd kubectl -n "${NAMESPACE}" port-forward svc/"${SERVICE_NAME}" "${LOCAL_PORT}:${SERVICE_PORT}"
    ;;
  background)
    mkdir -p "${PID_DIR}"
    if [[ -f "${PID_FILE}" ]] && kill -0 "$(cat "${PID_FILE}")" 2>/dev/null; then
      echo "==> Port-forward already running with PID $(cat "${PID_FILE}")"
      exit 0
    fi

    if [[ "${DRY_RUN}" == "true" ]]; then
      echo "[dry-run] kubectl -n ${NAMESPACE} port-forward svc/${SERVICE_NAME} ${LOCAL_PORT}:${SERVICE_PORT} > ${LOG_FILE} 2>&1 &"
      echo "[dry-run] echo <pid> > ${PID_FILE}"
      exit 0
    fi

    kubectl -n "${NAMESPACE}" port-forward svc/"${SERVICE_NAME}" "${LOCAL_PORT}:${SERVICE_PORT}" >"${LOG_FILE}" 2>&1 &
    PF_PID=$!
    echo "${PF_PID}" > "${PID_FILE}"
    echo "==> Port-forward started in background (PID ${PF_PID})"
    echo "    Log file: ${LOG_FILE}"
    ;;
  none)
    echo "==> Skipping port-forward"
    ;;
  *)
    echo "ERROR: PORT_FORWARD_MODE must be one of: foreground, background, none"
    exit 1
    ;;
esac

