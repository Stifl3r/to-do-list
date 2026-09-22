#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

NAMESPACE="${NAMESPACE:-default}"
DEPLOYMENT_NAME="${DEPLOYMENT_NAME:-springboot-deployment}"
DELETE_TIMEOUT="${DELETE_TIMEOUT:-120s}"
DRY_RUN="${DRY_RUN:-false}"
STOP_MODE="${STOP_MODE:-${1:-full}}" # app | observability | full

PID_FILE="${ROOT_DIR}/.tmp/k8s-port-forward.pid"
PIDS_FILE="${ROOT_DIR}/.tmp/k8s-port-forward.pids"

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
require_command kubectl

if [[ "${STOP_MODE}" != "app" && "${STOP_MODE}" != "observability" && "${STOP_MODE}" != "full" ]]; then
  echo "ERROR: STOP_MODE must be 'app', 'observability', or 'full'"
  echo "Usage: ./scripts/stop-k8s.sh [app|observability|full]"
  exit 1
fi

echo "==> Stopping background port-forward(s) for mode: ${STOP_MODE}"
if [[ -f "${PIDS_FILE}" ]]; then
  if [[ "${DRY_RUN}" == "true" ]]; then
    case "${STOP_MODE}" in
      app)
        echo "[dry-run] kill tracked app port-forward pid(s) from ${PIDS_FILE}"
        ;;
      observability)
        echo "[dry-run] kill tracked prometheus/grafana port-forward pid(s) from ${PIDS_FILE}"
        ;;
      full)
        echo "[dry-run] kill all tracked port-forward pid(s) from ${PIDS_FILE}"
        ;;
    esac
    echo "[dry-run] rewrite/remove ${PIDS_FILE} based on surviving processes"
  else
    tmp_file="${PIDS_FILE}.tmp"
    : > "${tmp_file}"
    while IFS=: read -r name pid; do
      [[ -z "${name}" || -z "${pid}" ]] && continue

      should_kill="false"
      case "${STOP_MODE}" in
        app)
          [[ "${name}" == "app" ]] && should_kill="true"
          ;;
        observability)
          [[ "${name}" == "prometheus" || "${name}" == "grafana" ]] && should_kill="true"
          ;;
        full)
          should_kill="true"
          ;;
      esac

      if [[ "${should_kill}" == "true" ]]; then
        if kill -0 "${pid}" 2>/dev/null; then
          echo "==> Stopping ${name} port-forward (PID ${pid})"
          kill "${pid}" 2>/dev/null || true
        fi
      else
        if kill -0 "${pid}" 2>/dev/null; then
          echo "${name}:${pid}" >> "${tmp_file}"
        fi
      fi
    done < "${PIDS_FILE}"

    if [[ -s "${tmp_file}" ]]; then
      mv "${tmp_file}" "${PIDS_FILE}"
    else
      rm -f "${tmp_file}" "${PIDS_FILE}"
    fi
  fi
else
  echo "==> No tracked multi-port-forward file found"
fi

# Backward-compatible cleanup for legacy app-only pid file.
if [[ "${STOP_MODE}" == "app" || "${STOP_MODE}" == "full" ]]; then
  if [[ -f "${PID_FILE}" ]]; then
    PF_PID="$(cat "${PID_FILE}")"
    if [[ -n "${PF_PID}" ]] && kill -0 "${PF_PID}" 2>/dev/null; then
      echo "==> Stopping legacy app background port-forward (PID ${PF_PID})"
      if [[ "${DRY_RUN}" == "true" ]]; then
        echo "[dry-run] kill ${PF_PID}"
      else
        kill "${PF_PID}" 2>/dev/null || true
      fi
    fi
    run_cmd rm -f "${PID_FILE}"
  fi
fi

if [[ "${STOP_MODE}" == "app" ]]; then
  echo "==> Stopping application deployment only"
  run_cmd kubectl -n "${NAMESPACE}" delete deployment "${DEPLOYMENT_NAME}" --ignore-not-found=true
elif [[ "${STOP_MODE}" == "observability" ]]; then
  echo "==> Stopping observability resources only"
  run_cmd kubectl -n "${NAMESPACE}" delete -f k8s/observability-prometheus.yaml --ignore-not-found=true
  run_cmd kubectl -n "${NAMESPACE}" delete -f k8s/observability-grafana.yaml --ignore-not-found=true
else
  echo "==> Deleting full stack from kustomization"
  run_cmd kubectl -n "${NAMESPACE}" delete -k k8s/ --ignore-not-found=true
fi

if [[ "${STOP_MODE}" == "app" || "${STOP_MODE}" == "full" ]]; then
  echo "==> Waiting for application deployment deletion"
  if [[ "${DRY_RUN}" == "true" ]]; then
    echo "[dry-run] kubectl -n ${NAMESPACE} wait --for=delete deployment/${DEPLOYMENT_NAME} --timeout=${DELETE_TIMEOUT}"
  else
    kubectl -n "${NAMESPACE}" wait --for=delete deployment/"${DEPLOYMENT_NAME}" --timeout="${DELETE_TIMEOUT}" || true
  fi
fi

if [[ "${STOP_MODE}" == "observability" || "${STOP_MODE}" == "full" ]]; then
  echo "==> Waiting for observability deployments deletion"
  if [[ "${DRY_RUN}" == "true" ]]; then
    echo "[dry-run] kubectl -n ${NAMESPACE} wait --for=delete deployment/prometheus --timeout=${DELETE_TIMEOUT}"
    echo "[dry-run] kubectl -n ${NAMESPACE} wait --for=delete deployment/grafana --timeout=${DELETE_TIMEOUT}"
  else
    kubectl -n "${NAMESPACE}" wait --for=delete deployment/prometheus --timeout="${DELETE_TIMEOUT}" || true
    kubectl -n "${NAMESPACE}" wait --for=delete deployment/grafana --timeout="${DELETE_TIMEOUT}" || true
  fi
fi

echo "==> Remaining resources"
run_cmd kubectl -n "${NAMESPACE}" get deployment,service,pods -o wide

