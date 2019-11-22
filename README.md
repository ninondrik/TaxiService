# Taxi application

## Getting Started

I have created this application for diploma work at my college. It contains 
[microservice](https://github.com/anonlatte/taxiGrpcService), which has been written with Go and gRPC, 
[driver's application](https://github.com/anonlatte/TaxiService/tree/master/drivers_app),
[user's application](https://github.com/anonlatte/TaxiService/tree/master/customers_app)
and primitive [disptatcher's application](https://github.com/anonlatte/DispatcherApp) were written with Kotlin.
Desktop application (__dispactcher's app__) uses JavaFX12. 
MySQL was chosen as a database due to its comfortable data presentation.

### Prerequisites
- Docker
- Docker-Compose
- Google cloud account

### Service installing

1. First step for mobile application is the installation of the server side service. Docker and docker-compose must be installed to your PC.
2. Then, clone [service's repository](https://github.com/anonlatte/taxiGrpcService) and check the settings.

#### Basic Service's Settings
##### [Dockerfile environment settings](#docker-setup)
[Source file link](https://github.com/anonlatte/taxiGrpcService/blob/master/.env)
```
Paths to
    * mysql folder                 -   DB_PATH_HOST=./databases
    * source files                   -   APP_PATH_HOST=./src
    * main Dockerfile           -   APP_PATH_DOCKER=/go/src/golang-service
gRPC port number              -   GRPC_PORT=48695
MySQL 
    * database host              -   DB_HOST=db:3306
    * database username    -   DB_USER=root
    * user's password           -   DB_PASSWORD=157266
    * schema name              -   DB_SCHEMA=taxi
    * dump file                       -   DB_RESTORE_TARGET=./dumps/db_dump.sql
```
#### Deployment
Go to the main service folder which contains [docker-compose.yaml](https://github.com/anonlatte/taxiGrpcService/blob/master/docker-compose.yaml) and write these commands into the console.
__Use credentials which you wrote in the .env file.__
```
# docker-compose up
# cat db_dump.sql | docker exec -i mysql /usr/bin/mysql -u username --password=1234 schema
```

After all these manipulations, we can clone [mobile application's repository](https://github.com/anonlatte/TaxiService) which contains source files of user's and driver's application.

### Setting Up Mobile Application

1. Create ```gradle.properties``` in the project folder with next contains:
``` 
GOOGLE_MAPS_API_KEY=
API_VERSION=v1
ServerAddress=
ServerPort=
```
These applications are using Google maps API, so follow the [instruction](https://developers.google.com/maps/documentation/embed/get-api-key) to find out how to get the api key.

Api version is defined in the service's sources, server's address can be checked with ```ifconfig``` or ```ipconfig```, the port that you have defined in this [part](#docker-setup).
2. Build and run the app.
3. Then you can sign up as a driver and create an order as a user. 

## Media

You can check the screenshots of applications [here](). 

## Built With
- [gRPC]()
- [JavaFX12]()

## Authors

- [Proshunin German](https://www.linkedin.com/in/anonlatte/?locale=en_US)

See also the list of [contributors](https://github.com/anonlatte/TaxiService/graphs/contributors) who participated in this project.


## License

This project is licensed under GNU GENERAL PUBLIC LICENSE - see the [LICENSE](LICENSE) file for more details.
