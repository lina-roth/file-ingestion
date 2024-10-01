
## Build
```
docker build -t file-ingestion-app-gradle .
```


## Run
```
docker run \
-v /Users/home/ingestion:/usr/src/app/ingestion \
-v /Users/home/ingestion-completed:/usr/src/app/ingestion-completed \
-v /Users/home/ingestion-error:/usr/src/app/ingestion-error \
 --name=file-ingestion-app \
file-ingestion-app-gradle
```

