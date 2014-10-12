#/bin/zsh


## Deploy the Doge application to Cloud Foundry 

cf d z-axis

# setup the MongoLabs Mongo service
cf cs mongolab sandbox scaling-mongo

# push the application and defer to the manifest.yml to handle the rest
cf push

# make sure that - if we're assinging random-word based URIs - we don't accrue unused routes.
#cf delete-orphaned-routes -f

# list all the apps 
#cf apps
