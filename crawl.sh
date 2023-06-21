#!/bin/bash

userr=rebels
# token=$token
logdir=logs
propfile=$userr.properties

# repos=(
# oracle/graal
# )
set -f                      # avoid globbing (expansion of *).
repos=(${repositories//,/ })
# = ',' read -ra ARRAY <<< "$repositories"

mkdir $logdir/$userr
for repo in "${repos[@]}"
do
    echo "spring.main.web-application-type=none" > $propfile
    echo "scraping.repository=$repo" >> $propfile
    echo "scraping.auth=token $token" >> $propfile
    echo "scraping.export.all=false" >> $propfile
    echo "scraping.export.classified=false" >> $propfile
    echo "spring.jpa.hibernate.ddl-auto=update" >> $propfile
    echo "spring.datasource.url=jdbc:postgresql://$db_host:$db_port/$repo" >> $propfile
    echo "spring.datasource.username=$db_user">> $propfile
    echo "spring.datasource.password=$db_pass" >> $propfile
    java -jar build/libs/prscraper-1.0.1.jar --spring.config.location=$propfile #> $logdir/$userr/${repo/\//-}.txt
done
