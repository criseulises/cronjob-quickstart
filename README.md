
# Manual Completo para Implementar y Probar Cron Jobs en Kubernetes

Este documento detalla cómo instalar, configurar e implementar Cron Jobs en un entorno simulado de Kubernetes utilizando Minikube.

---

## Tabla de Contenidos
1. [Prerrequisitos](#prerrequisitos)
2. [Instalación de Minikube](#instalación-de-minikube)
    - [En macOS](#en-macos)
    - [En Windows](#en-windows)
3. [Inicialización de Kubernetes](#inicialización-de-kubernetes)
4. [Creación de la Aplicación](#creación-de-la-aplicación)
5. [Archivo YAML para el CronJob](#archivo-yaml-para-el-cronjob)
6. [Dockerfile para la Aplicación](#dockerfile-para-la-aplicación)
7. [Empaquetar la Aplicación](#empaquetar-la-aplicación)
8. [Crear la Imagen Docker en Minikube](#crear-la-imagen-docker-en-minikube)
9. [Aplicar el CronJob](#aplicar-el-cronjob)
10. [Verificar Ejecuciones del CronJob](#verificar-ejecuciones-del-cronjob)
11. [Comandos Útiles](#comandos-útiles)

---

## Prerrequisitos

Antes de comenzar, asegúrate de tener instalados los siguientes componentes:

- **Docker**: [Guía de instalación](https://docs.docker.com/get-docker/)
- **Minikube**: [Guía de instalación](https://minikube.sigs.k8s.io/docs/start/)

---

## Instalación de Minikube

### En macOS (Para ships M):
1. Descarga Minikube:
```bash
curl -LO https://github.com/kubernetes/minikube/releases/latest/download/minikube-darwin-arm64
```
2. Instálalo:
```bash
sudo install minikube-darwin-arm64 /usr/local/bin/minikube
```

### En Windows:
1. Descarga el ejecutable desde:  
   [Minikube para Windows](https://minikube.sigs.k8s.io/docs/start/?arch=%2Fwindows%2Fx86-64%2Fstable%2F.exe+download)

2. Sigue las instrucciones de instalación en el sitio oficial.

---

## Inicialización de Kubernetes

Inicia Minikube ejecutando:
```bash
minikube start
```

---

## Creación de la Aplicación

Crea una aplicación en **Java Quarkus** u otro lenguaje de tu elección. La aplicación debe ser capaz de iniciarse y detenerse por sí sola.

**Ejemplo de Código de la Aplicación**
```java
@QuarkusMain
public class Main {

    public static void main(String... args) {
        System.out.println("HELLO WORLD FROM CRONJOB");
        System.exit(0);
    }
}
```
---


## Dockerfile para la Aplicación

Crea un archivo `Dockerfile` con el siguiente contenido:

```dockerfile
FROM registry.access.redhat.com/ubi8/openjdk-21:1.20

COPY --chown=185 target/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 target/quarkus-app/*.jar /deployments/
COPY --chown=185 target/quarkus-app/app/ /deployments/app/
COPY --chown=185 target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
```

---


## Archivo YAML para el CronJob

Crea un archivo `cronjob.yaml` con el siguiente contenido:
- Para definir el cron: https://crontab.guru/ 
```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: cronjob-quickstart
spec:
  schedule: "* * * * *" # Se ejecuta cada minuto
  jobTemplate:
    spec:
      template:
        spec:
          containers:
            - name: cronjob-quickstart-container
              image: cronjob-quickstart:latest # Nombre de tu imagen Docker
              imagePullPolicy: IfNotPresent    # Usa la imagen local si está disponible
              command: ["java"]               # Ejecuta el comando java
              args: ["-jar", "/deployments/quarkus-run.jar"] # Ruta al archivo JAR
          restartPolicy: OnFailure             # Reinicia el contenedor solo si falla
```

Otras opciones importante, aplicar variables de entornos y ConfigMaps:

```yaml
...
              envFrom:
                - configMapRef:
                    name: app-config
              env:
                - name: ENV_PROPERTY
                  value: "VALUE PROPERTY"
...
```
---

## Empaquetar la Aplicación

Empaqueta tu aplicación ejecutando:
```bash
./mvnw clean package
```

---
## Crear la Imagen Docker en Minikube

1. Cambia al entorno Docker de Minikube:
   ```bash
   eval $(minikube docker-env)
   ```

2. Crea la imagen Docker:
   ```bash
   docker build -t cronjob-quickstart:latest .
   ```

3. Verifica que la imagen se haya creado correctamente:
   ```bash
   docker images
   ```

---

## Aplicar el CronJob

1. Aplica el CronJob en Kubernetes:
   ```bash
   kubectl apply -f cronjob.yaml
   ```

2. Verifica que el CronJob se haya creado correctamente:
   ```bash
   kubectl get cronjob
   ```

---

## Verificar Ejecuciones del CronJob

1. Espera un minuto (o el tiempo designado) para que el CronJob se ejecute.
2. Para ver los trabajos ejecutados:
   ```bash
   kubectl get jobs
   ```
3. Para ver los logs de cada ejecución:
   ```bash
   kubectl get pods
   kubectl logs <nombre-del-pod>
   # Ejemplo: cronjob-quickstart-28947144-zw6g8
   ```

---

## Comandos Útiles

- **Iniciar Minikube**: `minikube start`
- **Cambiar al entorno Docker de Minikube**: `eval $(minikube docker-env)`
- **Construir imagen Docker**: `docker build -t <nombre-imagen>:latest .`
- **Aplicar un archivo YAML**: `kubectl apply -f <archivo.yaml>`
- **Verificar CronJobs**: `kubectl get cronjob`
- **Verificar trabajos ejecutados**: `kubectl get jobs`
- **Eliminar CronJob**: `kubectl delete cronjob <nombre-cronjob>`
- **Correr la aplicación**: `docker run -it --rm <nombre-imagen> sh`

- **Verificar pods y logs**:
  ```bash
  kubectl get pods
  kubectl logs <nombre-del-pod>
  ```

---

¡Con este manual, deberías poder implementar y probar tus Cron Jobs sin problemas! 🚀
