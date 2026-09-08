# to-do-list Rest API
To-do-list Rest API that will serve as the core domain service to multiple tools. Use cases for the rest api will be very simple and skeleton.

Integration between the following technologies will be added
1. TODO Rest Api 
   ###### Source of truth/Core Domain
   ###### Plan of action is to create a matured Rest API that has Pagination and Filtering, as well as correct use of Hateoas Capabilities.
2. MQ Integration 
   ###### Send emails to employees for overdue tickets
3. Batch Integration 
   ###### Determine tasks that are over due and send notification to email queue
4. Reverse Proxy
   ###### Implement reverse proxy for all the microservices in ecosystem
5. Lambdas
   ###### ( Not sure yet what I wanna show case here)
6. Scheduling
   ###### Implement the ability to schedule batch jobs
7. Reports
   ###### Create pdf reports for tickets turn around times
8. Cloud Storage
   ###### Store report files on cloud storage and allow ease of access through UI
9. Security
   ###### Implement authentication, authorization and service to service security
10. SPA
    ###### Presentation layer to wrap and interact with all the services
11. Metrics and tracking
    ###### With this ecosystem, we will need the ability to track requests across multiple services, evaluate performance and allow ease of debugging
12. Dockerization
13. K8


* Lastly UI(Do not count on this one :D )

## Local K8 start and stop scripts

The project includes two helper scripts:
- `scripts/start-k8s.sh [app|observability|full] [foreground|background|none]`
- `scripts/stop-k8s.sh [app|observability|full]`

### Start modes
- `app`: build app image, load into kind, deploy app resources only.
- `observability`: deploy Prometheus and Grafana only.
- `full`: deploy app + Prometheus + Grafana.

### Stop modes
- `app`: remove app deployment only.
- `observability`: remove Prometheus and Grafana resources only.
- `full`: remove everything in `k8s/kustomization.yaml`.

### Examples

```bash
./scripts/start-k8s.sh app foreground
./scripts/start-k8s.sh observability background
./scripts/start-k8s.sh full none
```

```bash
./scripts/stop-k8s.sh app
./scripts/stop-k8s.sh observability
./scripts/stop-k8s.sh full
```

When `start-k8s.sh` runs in `full` mode with port forwarding enabled, it forwards:
- App: `localhost:9001 -> springboot-service:9001`
- Prometheus: `localhost:9090 -> prometheus-service:9090`
- Grafana: `localhost:3000 -> grafana-service:3000`

