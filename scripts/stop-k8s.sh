#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

NAMESPACE="${NAMESPACE:-default}"
DEPLOYMENT_NAME="${DEPLOYMENT_NAME:-springboot-deployment}"
DELETE_TIMEOUT="${DELETE_TIMEOUT:-120s}"
DRY_RUN="${DRY_RUN:-false}"

PID_FILE="${ROOT_DIR}/.tmp/k8s-port-forward.pid"

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

if [[ -f "${PID_FILE}" ]]; then
  PF_PID="$(cat "${PID_FILE}")"
  if [[ -n "${PF_PID}" ]] && kill -0 "${PF_PID}" 2>/dev/null; then
    echo "==> Stopping background port-forward (PID ${PF_PID})"
    if [[ "${DRY_RUN}" == "true" ]]; then
      echo "[dry-run] kill ${PF_PID}"
      echo "[dry-run] rm -f ${PID_FILE}"
    else
      kill "${PF_PID}"
      rm -f "${PID_FILE}"
    fi
  else
    echo "==> Removing stale port-forward pid file"
    run_cmd rm -f "${PID_FILE}"
  fi
else
  echo "==> No background port-forward pid file found"
fi

echo "==> Deleting resources from kustomization"
run_cmd kubectl -n "${NAMESPACE}" delete -k k8s/ --ignore-not-found=true

echo "==> Waiting for deployment deletion"
if [[ "${DRY_RUN}" == "true" ]]; then
  echo "[dry-run] kubectl -n ${NAMESPACE} wait --for=delete deployment/${DEPLOYMENT_NAME} --timeout=${DELETE_TIMEOUT}"
else
  kubectl -n "${NAMESPACE}" wait --for=delete deployment/"${DEPLOYMENT_NAME}" --timeout="${DELETE_TIMEOUT}" || true
fi

echo "==> Remaining resources"
run_cmd kubectl -n "${NAMESPACE}" get deployment,service,pods -o wide

