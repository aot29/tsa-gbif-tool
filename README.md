# Tsa GBIF Tools

Tools for pulling taxonomy from GBIF

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

## Development
Do development on the host using `sbt run` etc. 

**Note that the app is compiled when building the Dockerfile**,
so when testing the Docker setup, changes to the code 
will take effect in the container after rebuilding the
image (even if the code directory is mounted on the container).

## Run the tests
```
export $(xargs <.env)
sbt test
```
Make sure a database is running on the host at port 3306, 
e.g. by starting tsa-django-frontend with `docker-compose up -d`.

## Build the image
`docker build -t docker-registry.naturkundemuseum.berlin/tsa/tsa-gbif-tool:latest .`

