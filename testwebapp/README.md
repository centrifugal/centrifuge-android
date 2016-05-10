# centrifuge-android test web application
This simple web application is intended to be used with
test android application ([`:app`](https://github.com/Centrifugal/centrifuge-android/tree/master/app))

### Usage
Build and run with this params:
````
-d --docker     Run centrifugo in docker container (no arg)
-a --address    Centrifugo's IP address without protocol  (e.g. 192.168.0.100)
-p --port       Port for webapp to listen
````

If you want to use [docker](https://www.docker.com) container, than following
environment variables must be set correctly:
````
DOCKER_HOST
DOCKER_TLS_VERIFY
DOCKER_CERT_PATH
````

After server startup you can make requests via HTTP API or Centrifugo web interface.
