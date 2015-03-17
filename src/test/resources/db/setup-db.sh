#!/bin/bash

##
# Create test database and owner role
# Arguments: dbname username password
#
db=$1
user=$2
pwd=$3

db_name=${db:-dbunit_db}
user_name=${user:-dbunit_role}
user_pwd=${pwd:-dbunit_pwd}

# sudo -i -u postgres createuser -P -D -R -S "${user_name}"
sudo -i -u postgres psql -c \
"CREATE ROLE ${user_name} WITH LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE ENCRYPTED PASSWORD '${user_pwd}' ;"

# sudo -i -u postgres createdb -O "${user_name}" "${db_name}"
sudo -i -u postgres psql -c \
"CREATE DATABASE ${db_name} WITH OWNER ${user_name} ;"

# Not necessary as of postgres 9.1
# sudo -i -u postgres createlang plpgsql ${db_name}
