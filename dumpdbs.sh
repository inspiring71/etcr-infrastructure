#/bin/bash

set -f                      # avoid globbing (expansion of *).
repos=(${repositories//,/ })

outp="../data_dump"
for repo in "${repos[@]}"
do
    echo "dump $repo."
    PGPASSWORD=$db_pass pg_dump -h $db_host -p $db_port -U db_user -w -F t $repo > $outp/${repo/\//-}.tar
    gzip $outp/${repo/\//-}.tar 
done

# unzip dbs.zip
# for repo in "${repos[@]}"
# do
#     echo "load $repo."
#     gunzip $repo.tar.gz
#     PGPASSWORD=$db_pass pg_restore -h $db_host -p $db_port -U db_user --dbname=$repo --no-owner --verbose $repo.tar
# done