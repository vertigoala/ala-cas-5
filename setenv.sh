#!/bin/sh
#
# We are testing ngrok as a self-service dynamic https endpoint
# NGROK_URL is ngrok https endpoint (ex: https://c6666564.ngrok.io)
# SERVERNAME is a manager host IP address
#
#export DOCKER_HOST=tcp://$SERVERNAME:2375
#export DOCKER_HOST=ssh://ubuntu@$SERVERNAME
#export ALA_URL=http://$SERVERNAME # traefik port
export CAS_SERVER=crapula.serveo.net
export CAS_DOMAIN=serveo.net
export ALA_URL=https://$CAS_SERVER # traefik port
sed "s#\$ALA_URL#$ALA_URL#g" ./config/application-template.yml | \
 sed "s#\$CAS_DOMAIN#$CAS_DOMAIN#g" | \
 sed "s#\$CAS_SERVER#$CAS_SERVER#g" > ./config/application.yml
sed "s#\$ALA_URL#$ALA_URL#g" ./config/userdetails-config-template.properties > ./config/userdetails-config.properties
sed "s#\$ALA_URL#$ALA_URL#g" ./config/management-template.properties > ./config/management.properties
