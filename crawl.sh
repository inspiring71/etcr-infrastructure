lang=csharp
token=[gh_token]
logdir=logs
propfile=$lang.properties

repos=(
GitTools/GitVersion
)


for repo in "${repos[@]}"
do
    echo -e "spring.main.web-application-type=none\n\n# scraping config" > $propfile
    echo -e "scraping.repository=$repos" >> $propfile
    echo -e "scraping.auth=token $token\nscraping.export.all=false\nscraping.export.classified=false" >> $propfile
    echo -e "# database config\nspring.jpa.hibernate.ddl-auto=update\n" >> $propfile
    echo -e "spring.datasource.url=jdbc:postgresql://localhost:5432/$repo" >> $propfile
    echo -e "spring.datasource.username=FAREAST.v-zhuoli1\nspring.datasource.password=passward" >> $propfile
    java -jar build/libs/prscraper-1.0.1.jar --spring.config.location=$propfile > $logdir/$lang_${repo/\//-}.txt
done
