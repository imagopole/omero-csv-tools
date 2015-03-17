#!/bin/bash

##
# Drop test database and owner role
# Arguments: dbname username password
#
db=$1
user=$2

db_name=${db:-dbunit_db}
user_name=${user:-dbunit_role}

# Not necessary as of postgres 9.1
# sudo -i -u postgres droplang plpgsql -d "${db_name}"

# sudo -i -u postgres dropdb --if-exists "${db_name}"
sudo -i -u postgres psql -c \
"DROP DATABASE IF EXISTS ${db_name} ;"

# sudo -i -u postgres dropuser --if-exists "${user_name}"
sudo -i -u postgres psql -c \
"DROP ROLE IF EXISTS ${user_name} ;"
