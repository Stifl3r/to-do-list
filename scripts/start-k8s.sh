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
DRY_RUN="${DRY_RUN:-false}"
START_MODE="${START_MODE:-${1:-full}}" # app | observability | full
PORT_FORWARD_MODE="${PORT_FORWARD_MODE:-${2:-foreground}}" # foreground | background | none

# Observability forwards are used in observability and full modes.
GRAFANA_SERVICE_NAME="${GRAFANA_SERVICE_NAME:-grafana-service}"
GRAFANA_LOCAL_PORT="${GRAFANA_LOCAL_PORT:-3000}"
GRAFANA_SERVICE_PORT="${GRAFANA_SERVICE_PORT:-3000}"
PROMETHEUS_SERVICE_NAME="${PROMETHEUS_SERVICE_NAME:-prometheus-service}"
PROMETHEUS_LOCAL_PORT="${PROMETHEUS_LOCAL_PORT:-9090}"
PROMETHEUS_SERVICE_PORT="${PROMETHEUS_SERVICE_PORT:-9090}"

PID_DIR="${ROOT_DIR}/.tmp"
PID_FILE="${PID_DIR}/k8s-port-forward.pid"
LOG_FILE="${PID_DIR}/k8s-port-forward.log"
PIDS_FILE="${PID_DIR}/k8s-port-forward.pids"

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

is_pid_running() {
  local pid="$1"
  [[ -n "${pid}" ]] && kill -0 "${pid}" 2>/dev/null
}

start_pf_bg() {
  local name="$1"
  local service="$2"
  local local_port="$3"
  local service_port="$4"
  local log_file="$5"

  kubectl -n "${NAMESPACE}" port-forward svc/"${service}" "${local_port}:${service_port}" >"${log_file}" 2>&1 &
  local pf_pid=$!
  echo "${name}:${pf_pid}" >> "${PIDS_FILE}"
  echo "==> ${name} port-forward started in background (PID ${pf_pid}) ${local_port}:${service_port}"
}

cleanup_pf() {
  if [[ -f "${PIDS_FILE}" ]]; then
    while IFS=: read -r _name pid; do
      if is_pid_running "${pid}"; then
        kill "${pid}" 2>/dev/null || true
      fi
    done < "${PIDS_FILE}"
    rm -f "${PIDS_FILE}"
  fi
}

echo "==> Validating required tools"
require_command kubectl
require_command kind

if [[ "${START_MODE}" != "app" && "${START_MODE}" != "observability" && "${START_MODE}" != "full" ]]; then
  echo "ERROR: START_MODE must be 'app', 'observability', or 'full'"
  echo "Usage: ./scripts/start-k8s.sh [app|observability|full] [foreground|background|none]"
  exit 1
fi

if [[ "${PORT_FORWARD_MODE}" != "foreground" && "${PORT_FORWARD_MODE}" != "background" && "${PORT_FORWARD_MODE}" != "none" ]]; then
  echo "ERROR: PORT_FORWARD_MODE must be one of: foreground, background, none"
  echo "Usage: ./scripts/start-k8s.sh [app|observability|full] [foreground|background|none]"
  exit 1
fi

if [[ "${START_MODE}" == "app" || "${START_MODE}" == "full" ]]; then
  if [[ ! -f "k8s/.env.secret" ]]; then
    echo "ERROR: Missing k8s/.env.secret"
    echo "Create it with: cp k8s/.env.secret.example k8s/.env.secret"
    exit 1
  fi

  require_command ./gradlew
  require_command docker

  echo "==> Building application jar"
  run_cmd ./gradlew clean bootJar

  echo "==> Building Docker image ${IMAGE_NAME}"
  run_cmd docker build -t "${IMAGE_NAME}" .

  echo "==> Loading image into kind cluster ${KIND_CLUSTER_NAME}"
  run_cmd kind load docker-image "${IMAGE_NAME}" --name "${KIND_CLUSTER_NAME}"
fi

if [[ "${START_MODE}" == "app" ]]; then
  echo "==> Applying application-only resources"
  echo "==> Syncing db-credentials secret from k8s/.env.secret"
  if [[ "${DRY_RUN}" == "true" ]]; then
    echo "[dry-run] kubectl -n ${NAMESPACE} create secret generic db-credentials --from-env-file=k8s/.env.secret --dry-run=client -o yaml | kubectl -n ${NAMESPACE} apply -f -"
  else
    kubectl -n "${NAMESPACE}" create secret generic db-credentials --from-env-file=k8s/.env.secret --dry-run=client -o yaml | kubectl -n "${NAMESPACE}" apply -f -
  fi
  run_cmd kubectl -n "${NAMESPACE}" apply -f k8s/app-config.yaml
elif [[ "${START_MODE}" == "observability" ]]; then
  echo "==> Applying observability-only resources"
  run_cmd kubectl -n "${NAMESPACE}" apply -f k8s/observability-prometheus.yaml
  run_cmd kubectl -n "${NAMESPACE}" apply -f k8s/observability-grafana.yaml
else
  echo "==> Applying full Kubernetes stack via kustomize"
  run_cmd kubectl -n "${NAMESPACE}" apply -k k8s/
fi

if [[ "${START_MODE}" == "app" || "${START_MODE}" == "full" ]]; then
  echo "==> Waiting for rollout of deployment/${DEPLOYMENT_NAME}"
  run_cmd kubectl -n "${NAMESPACE}" rollout status deployment/"${DEPLOYMENT_NAME}" --timeout="${WAIT_TIMEOUT}"
fi

if [[ "${START_MODE}" == "observability" || "${START_MODE}" == "full" ]]; then
  echo "==> Waiting for rollout of deployment/prometheus"
  run_cmd kubectl -n "${NAMESPACE}" rollout status deployment/prometheus --timeout="${WAIT_TIMEOUT}"
  echo "==> Waiting for rollout of deployment/grafana"
  run_cmd kubectl -n "${NAMESPACE}" rollout status deployment/grafana --timeout="${WAIT_TIMEOUT}"
fi

echo "==> Current pods"
if [[ "${START_MODE}" == "app" ]]; then
  run_cmd kubectl -n "${NAMESPACE}" get pods -l app=springboot-api -o wide
elif [[ "${START_MODE}" == "observability" ]]; then
  run_cmd kubectl -n "${NAMESPACE}" get pods -l 'app in (prometheus,grafana)' -o wide
else
  run_cmd kubectl -n "${NAMESPACE}" get pods -l 'app in (springboot-api,prometheus,grafana)' -o wide
fi

case "${PORT_FORWARD_MODE}" in
  foreground)
    if [[ "${DRY_RUN}" == "true" ]]; then
      if [[ "${START_MODE}" == "app" || "${START_MODE}" == "full" ]]; then
        echo "[dry-run] kubectl -n ${NAMESPACE} port-forward svc/${SERVICE_NAME} ${LOCAL_PORT}:${SERVICE_PORT}"
      fi
      if [[ "${START_MODE}" == "observability" || "${START_MODE}" == "full" ]]; then
        echo "[dry-run] kubectl -n ${NAMESPACE} port-forward svc/${PROMETHEUS_SERVICE_NAME} ${PROMETHEUS_LOCAL_PORT}:${PROMETHEUS_SERVICE_PORT}"
        echo "[dry-run] kubectl -n ${NAMESPACE} port-forward svc/${GRAFANA_SERVICE_NAME} ${GRAFANA_LOCAL_PORT}:${GRAFANA_SERVICE_PORT}"
      fi
      exit 0
    fi

    mkdir -p "${PID_DIR}"
    rm -f "${PIDS_FILE}"
    trap cleanup_pf EXIT INT TERM

    if [[ "${START_MODE}" == "observability" || "${START_MODE}" == "full" ]]; then
      start_pf_bg "prometheus" "${PROMETHEUS_SERVICE_NAME}" "${PROMETHEUS_LOCAL_PORT}" "${PROMETHEUS_SERVICE_PORT}" "${PID_DIR}/k8s-port-forward-prometheus.log"
      start_pf_bg "grafana" "${GRAFANA_SERVICE_NAME}" "${GRAFANA_LOCAL_PORT}" "${GRAFANA_SERVICE_PORT}" "${PID_DIR}/k8s-port-forward-grafana.log"
    fi

    if [[ "${START_MODE}" == "app" || "${START_MODE}" == "full" ]]; then
      echo "==> Starting foreground port-forward ${LOCAL_PORT}:${SERVICE_PORT}"
      echo "Press Ctrl+C to stop port-forward(s)."
      kubectl -n "${NAMESPACE}" port-forward svc/"${SERVICE_NAME}" "${LOCAL_PORT}:${SERVICE_PORT}"
    else
      echo "==> Observability forwards started in background."
      echo "Press Ctrl+C to stop port-forward(s)."
      # Keep foreground mode alive until interrupted.
      while true; do sleep 3600; done
    fi
    ;;
  background)
    mkdir -p "${PID_DIR}"

    if [[ -f "${PIDS_FILE}" ]]; then
      # Remove stale entries but keep active ones.
      tmp_file="${PIDS_FILE}.tmp"
      : > "${tmp_file}"
      while IFS=: read -r name pid; do
        if is_pid_running "${pid}"; then
          echo "${name}:${pid}" >> "${tmp_file}"
        fi
      done < "${PIDS_FILE}"
      mv "${tmp_file}" "${PIDS_FILE}"
    fi

    if [[ "${START_MODE}" != "observability" ]] && [[ -f "${PID_FILE}" ]] && is_pid_running "$(cat "${PID_FILE}")"; then
      echo "==> App port-forward already running with PID $(cat "${PID_FILE}")"
      exit 0
    fi

    if [[ "${DRY_RUN}" == "true" ]]; then
      if [[ "${START_MODE}" == "app" || "${START_MODE}" == "full" ]]; then
        echo "[dry-run] kubectl -n ${NAMESPACE} port-forward svc/${SERVICE_NAME} ${LOCAL_PORT}:${SERVICE_PORT} > ${LOG_FILE} 2>&1 &"
        echo "[dry-run] echo app:<pid> > ${PIDS_FILE}"
      fi
      if [[ "${START_MODE}" == "observability" || "${START_MODE}" == "full" ]]; then
        echo "[dry-run] kubectl -n ${NAMESPACE} port-forward svc/${PROMETHEUS_SERVICE_NAME} ${PROMETHEUS_LOCAL_PORT}:${PROMETHEUS_SERVICE_PORT} > ${PID_DIR}/k8s-port-forward-prometheus.log 2>&1 &"
        echo "[dry-run] kubectl -n ${NAMESPACE} port-forward svc/${GRAFANA_SERVICE_NAME} ${GRAFANA_LOCAL_PORT}:${GRAFANA_SERVICE_PORT} > ${PID_DIR}/k8s-port-forward-grafana.log 2>&1 &"
      fi
      if [[ "${START_MODE}" == "app" || "${START_MODE}" == "full" ]]; then
        echo "[dry-run] echo <app-pid> > ${PID_FILE}"
      fi
      exit 0
    fi

    rm -f "${PIDS_FILE}"
    if [[ "${START_MODE}" == "app" || "${START_MODE}" == "full" ]]; then
      kubectl -n "${NAMESPACE}" port-forward svc/"${SERVICE_NAME}" "${LOCAL_PORT}:${SERVICE_PORT}" >"${LOG_FILE}" 2>&1 &
      APP_PF_PID=$!
      echo "${APP_PF_PID}" > "${PID_FILE}"
      echo "app:${APP_PF_PID}" >> "${PIDS_FILE}"
      echo "==> App port-forward started in background (PID ${APP_PF_PID})"
      echo "    Log file: ${LOG_FILE}"
    fi

    if [[ "${START_MODE}" == "observability" || "${START_MODE}" == "full" ]]; then
      start_pf_bg "prometheus" "${PROMETHEUS_SERVICE_NAME}" "${PROMETHEUS_LOCAL_PORT}" "${PROMETHEUS_SERVICE_PORT}" "${PID_DIR}/k8s-port-forward-prometheus.log"
      start_pf_bg "grafana" "${GRAFANA_SERVICE_NAME}" "${GRAFANA_LOCAL_PORT}" "${GRAFANA_SERVICE_PORT}" "${PID_DIR}/k8s-port-forward-grafana.log"
    fi
    ;;
  none)
    echo "==> Skipping port-forward"
    ;;
esac

