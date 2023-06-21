#/bin/bash

# repos=(
# oracle/graal
# )
set -f                      # avoid globbing (expansion of *).
repos=(${repositories//,/ })

for repo in "${repos[@]}"
do
    # echo "dropdb $repo."
    # PGPASSWORD=$db_pass dropdb -h $db_host -p $db_port -U $db_user $repo
    echo "createdb $repo."
    PGPASSWORD=$db_pass createdb -h $db_host -p $db_port -U db_user $repo
done