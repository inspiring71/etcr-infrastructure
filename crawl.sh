userr=
token=
logdir=logs
propfile=$userr.properties

repos=(

)

mkdir $logdir/$userr
for repo in "${repos[@]}"
do
    echo -e "spring.main.web-application-type=none\n\n# scraping config" > $propfile
    echo -e "scraping.repository=$repo" >> $propfile
    echo -e "scraping.auth=token $token\nscraping.export.all=false\nscraping.export.classified=false" >> $propfile
    echo -e "# database config\nspring.jpa.hibernate.ddl-auto=update\n" >> $propfile
    echo -e "spring.datasource.url=jdbc:postgresql://localhost:5432/$repo" >> $propfile
    echo -e "spring.datasource.username=FAREAST.v-zhuoli1\nspring.datasource.password=passward" >> $propfile
    java -jar build/libs/prscraper-1.0.1.jar --spring.config.location=$propfile > $logdir/$userr/${repo/\//-}.txt
done
