# dexcom-proxy

The purpose of this project is to act as an oauth compliant proxy to the Dexcom Share API (which uses a session based token).

# Configure

You'll need to pass in three values to the app as `env` vars:

```bash
export CODES_RECURSIVE_SECRET= 
export CODES_RECURSIVE_CLIENT_ID= 
export CODES_RECURSIVE_CLIENT_SECRET= 
```

Populate those values and set them before running the app. They need to be unique - try a UUID.

# Building

Build with Gradle:

```bash
gradle assemble
```

Which produces a JAR in `build/libs`.

# Running

Run the JAR:

```bash
java -jar build/libs/[jar name].jar
```

# Docker

Build:

```bash
docker build -t dexcom-proxy .
```

Tag:

```bash
docker tag...
```

Push:

```bash
docker push...
```

# K8s

If you want to deploy on Kubernetes, check out the `k8s/` directory.  

First, create your secrets (base64 encode them) and populate `secrets.yaml`. Then deploy it:

```bash
kubectl apply -f k8s/secret.yaml
```

To deploy the app from the latest Docker image on Docker Hub, use the `app.yaml` file:

```bash
kubectl apply -f k8s/app.yaml
```

If you're using an NGINX ingress controller, see `ingress.yaml`.  If not, don't worry about it.

# Using The App

Authenticate in the browser once it's running:

`http://localhost:8080/auth?redirect_uri=http://some.where&&client_id=[YOUR CLIENT ID]`

Once it redirects, grab the `code` from the URL that it redirected to and make a cURL request for a token:

```bash
curl --location --request POST 'http://localhost:8080/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'code=[CODE]' \
--data-urlencode 'client_id=[YOUR CLIENT ID]' \
--data-urlencode 'client_secret=[YOUR CLIENT SECRET]' \
--data-urlencode 'grant_type=[something]' \
--data-urlencode 'state=[something]'
```

When you get a token, make a call to get readings:

```bash
curl --location --request GET 'http://localhost:8080/api/readings/[duration in minutes]/[max readings]' \
--header 'Authorization: Bearer eyJh...J8pM'
```

# Feedback

Is always welcomed. Leave it here on the GitHub.  

# Peace

Out.
