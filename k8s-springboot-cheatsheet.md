# 📝 THE DEFINITIVE LOCAL KUBECTL & SPRING BOOT CHEAT SHEET

## 1. Project Directory Architecture
```text
my-springboot-app/
├── Dockerfile            # Java 25 Native Compiler Matrix
├── k8s/
│   ├── app-config.yaml         # Deployment/Service/Endpoints manifests
│   ├── kustomization.yaml      # Kustomize entry point
│   ├── .env.secret.example     # Secret template (safe to commit)
│   └── .env.secret             # Real credentials (gitignored)
├── src/                  # Java/Kotlin Source Core
├── build.gradle          # Build automation driver
└── gradlew               # Executable Wrapper
```

## 2. Infrastructure Source Files

### `Dockerfile` (Root Workspace Folder)
```dockerfile
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY build/libs/*-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### `k8s/app-config.yaml`
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: springboot-deployment
  labels:
    app: springboot-api
spec:
  replicas: 2
  selector:
    matchLabels:
      app: springboot-api
  template:
    metadata:
      labels:
        app: springboot-api
    spec:
      containers:
      - name: springboot-container
        image: springboot-api:java25
        imagePullPolicy: Never
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "local"
        - name: SPRING_DATASOURCE_URL
          value: "jdbc:postgresql://172.23.0.1:55432/c4h_local?currentSchema=to_do_list"
        - name: AWS_REGION
          value: "us-east-1"
        - name: AWS_DEFAULT_REGION
          value: "us-east-1"
        - name: SPRING_DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: DB_USERNAME
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: DB_PASSWORD
        - name: SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT
          value: "org.hibernate.dialect.PostgreSQLDialect"
        - name: SPRING_JPA_DATABASE_PLATFORM
          value: "org.hibernate.dialect.PostgreSQLDialect"
---
apiVersion: v1
kind: Service
metadata:
  name: springboot-service
spec:
  selector:
    app: springboot-api
  ports:
    - protocol: TCP
      port: 9001
      targetPort: 8080
  type: ClusterIP
```

### `k8s/kustomization.yaml`
```yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

resources:
  - app-config.yaml

secretGenerator:
  - name: db-credentials
    envs:
      - .env.secret
```

---

## 3. Daily Command Terminal Checklist

### One-Time Secret File Setup
```bash
cp k8s/.env.secret.example k8s/.env.secret
```

Edit `k8s/.env.secret` and set real values:
```dotenv
DB_USERNAME=postgres
DB_PASSWORD=your-real-password
```

### Build & Deploy Sequence Loop
```bash
# Compile and build the native jar bundle
./gradlew clean bootJar

# Build the isolated engine container image tag
docker build -t springboot-api:java25 .

# Force stream the container artifact directly into cluster cache
kind load docker-image springboot-api:java25 --name c4h-learn-k8s

# Synchronize manifests and generated Secret via kustomize
kubectl apply -k k8s/
```

### Clean Slate - Delete Existing Pods
```bash
# Delete all running pods to start fresh (deployment will auto-respawn them)
kubectl delete pods -l app=springboot-api

# Alternative: Delete entire deployment and reapply config
kubectl delete deployment springboot-deployment
kubectl apply -k k8s/

# Verify pods are gone before redeploying
kubectl get pods -l app=springboot-api
```

### Diagnostic Tracking Verification
```bash
# Preview rendered manifests (includes generated Secret)
kubectl kustomize k8s/

# Verify generated Secret name
kubectl get secret | grep db-credentials

# Verify observability pods and services
kubectl get pods -l app=prometheus
kubectl get pods -l app=grafana
kubectl get svc prometheus-service grafana-service

# Track target engine container rollout loops
kubectl get pods -w

# Capture death-trace logs from crashed container runtimes
kubectl logs deployment/springboot-deployment --previous --tail=50

# Track active logs in real-time execution mode
kubectl logs -l app=springboot-api --tail=50 -f
```

### Observability Dashboards (Prometheus + Grafana)
```bash
# Verify Spring Prometheus endpoint is exposed
kubectl port-forward svc/springboot-service 9001:9001
curl -s http://localhost:9001/actuator/prometheus | head

# Open Prometheus UI
kubectl port-forward svc/prometheus-service 9090:9090
# then browse http://localhost:9090

# Open Grafana UI
kubectl port-forward svc/grafana-service 3000:3000
# then browse http://localhost:3000
```

Grafana login defaults (local only):
- user: `admin`
- password: `admin`

Dashboard is auto-provisioned:
- `Spring Boot Overview`

### Direct Network Expose Interface (Port Forwarding)
```bash
# Bridge cluster container network layer to Linux loopback (container port 8080)
kubectl port-forward deployment/springboot-deployment 8080:8080

# Port forward the service on port 9001 (recommended way)
kubectl port-forward svc/springboot-service 9001:9001

# Execute manual client ping checks (From a clean secondary shell)
curl -i http://localhost:8080/actuator/health

# Or via the service port
curl -i http://localhost:9001/actuator/health
```

### Real-Time Infrastructure Scaling
```bash
# Scale execution capacity dynamically from 2 to 5 containers
kubectl scale deployment/springboot-deployment --replicas=5

# Monitor high-availability cluster resource layouts
kubectl get deployment,service,pods -o wide
```
