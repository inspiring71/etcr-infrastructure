#/bin/bash

# repos=(
# oracle/graal
# )
repos = ',' read -ra ARRAY <<< "$repositories"


for repo in "${repos[@]}"
do
    # echo "dropdb $repo."
    # PGPASSWORD=$db_pass dropdb -h $db_host -p $db_port -U $db_user -W $repo
    echo "createdb $repo."
    PGPASSWORD=$db_pass createdb -h $db_host -p $db_port -U $db_user -W $repo
done