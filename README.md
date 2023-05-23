# Tsa GBIF Tools

Tools for pulling taxonomy from GBIF

## Production
* Make sure the relevant environment variables are set in .env and passed in docker-compose.yml
* Call `bash runAll.sh`on the host, probably from a cron job. The service will exit when the database update is done.

## Development
Do development on the host.

Make sure a database is running on the host at port 3306,
e.g. by starting tsa-django-frontend with `docker-compose up -d`.

The first time, copy `.env.example`to `.env`and set the required values.
After that, you'll need to read the environment variables in the `.env` file before starting up sbt:
```
export $(xargs <.env)
sbt run
```

**Note that the app is compiled when building the Dockerfile**,
so when testing the Docker setup, changes to the code 
will take effect in the container after rebuilding the
image (even if the code directory is mounted on the container).

## Run the tests
Make sure a database is running on the host at port 3306,
e.g. by starting tsa-django-frontend with `docker-compose up -d`.

```
export $(xargs <.env)
sbt test
```

## Build the image
The image is built in 2 stages. You only need to push the second stage.

```
docker build -t docker-registry.naturkundemuseum.berlin/tsa/tsa-gbif-tool:latest .
docker push docker-registry.naturkundemuseum.berlin/tsa/tsa-gbif-tool:latest
```

Then on the server:

```
cd /local tsa
docker pull docker-registry.naturkundemuseum.berlin/tsa/tsa-gbif-tool:latest
docker-compose up -d
```

## API
Deletes all data from GBIF check columns in System table

```curl -v --request DELETE localhost:9000/cleanup```

Gets all species actually used in Main, mark these as used in the System table.

```curl -v --request PUT localhost:9000/markAllUsed```

Gets a list of species used in the Main table.
For each species, calls the GBIF API, get its status, usage key and evaluate the response.
Updates the columns GBIF_check, GBIF_response, GBIF_usage_key in the system table.

```curl -v --request PUT localhost:9000/matchAll```

Matches a species name with the GBIF taxonomic backbone.
REPLACE SPACES BY UNDERSCORES, like so: *Puma_concolor*.
Updates the columns GBIF_check, GBIF_response, GBIF_usage_key in the system table.

```curl -v --request PUT localhost:9000/match/Puma_concolor```

Makes a list of species used in the Main table

```curl -v localhost:9000/list```
