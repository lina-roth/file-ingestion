
## Build
```
docker build -t file-ingestion-app-gradle .
```


## Run
```
docker compose up
```


# How It Works:

## Scheduled Directory Check:
The ScheduledExecutorService checks the directory every 1 minute (scheduler.scheduleAtFixedRate).
It uses a DirectoryStream to iterate over the files in the directory each time it checks. The checkDirectoryForFiles()
method looks for .txt files in the monitored folder and places them in the
fileQueue for processing.Moves files to ingestion-error folder if not a valid file.

## Queue Processing:
The processFilesFromQueue() method constantly checks the queue for new files. When a file is added to the queue,
it reads the file's content and simulates sending it to a processing queue (see below).
This is where we would want to hit the Orchestration API. It then moves the files to ingestion-completed

### Use Java Queue If:
- Your application is relatively simple and doesn't require high scalability.
- You're processing a manageable number of items and can afford to lose some data if the application crashes.
- You are running in a single instance and don't need the distributed capabilities.

### Use RabbitMQ If:
- You're dealing with a high volume of items that may require distributed processing across multiple instances.
- You need reliable message delivery and want to ensure that no items are lost.
- You want to decouple the processing logic from the production of messages for better maintainability.

## BlockingQueue:
We use a BlockingQueue to store file paths. This is thread-safe, so multiple threads can safely access the queue
for both ingestion and processing.

@author: Lina Roth
@date: 10/1/2024