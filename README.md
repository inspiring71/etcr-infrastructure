# Github PR Scraper

The repo is built based on the *etcr-infrastructure*, a web crawler for collecting github pull request data. (forked from [rheum/etcr-infrastructure](https://github.com/rheum/etcr-infrastructure)).

I've made some small changes to make etcr run stably and faster.
* Fixed: add try/except statement to keep the scraper from crashing.
* Fixed: Shorten the sleep time. Sleep when the remaining request number is less than 60.
* Added: add script to crawl lots of repos with one execution.
* <span style="color:red">Unfixed bug</span>: PSQL error when meeting *0x00* in crawled text. The crawler will fail in such cases.
I thought [this](https://stackoverflow.com/questions/1347646/postgres-error-on-insert-error-invalid-byte-sequence-for-encoding-utf8-0x0) is a solution. However, it seems my fix (which can be seen from the git commit history) does not work.
```
o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 22021
o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: invalid byte sequence for encoding "UTF8": 0x00
...
Caused by: org.postgresql.util.PSQLException: ERROR: invalid byte sequence for encoding "UTF8": 0x00
```


## Getting Started
### Install Postgres
ETCR crawl Pull Request (PR) data and store them in a Postgres database. So you need build the [Postgres](https://www.postgresql.org/download/) environment first.

### Collect Repos
Collect reposiroty names to crawl. Refer to [TopRepos](https://github.com/Lizhmq/TopRepos).

### Run the Scraper
Write repository names in createdbs.sh and execute:
```
bash createdbs.sh
bash crawl.sh
```

Remember to change the *PASSWORD* in createdbs.sh to your own psql password and to fill in the *userr*, [*token*](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token), and *repos* in crawl.sh.


## Update Scrapper
If you want to modify the code of the scraper, you can fork the repo and make your changes. Change the source code in `src` directory and run build again.
```
./gradle build
```
New `jar` package will be created in `build/libs` directory.


## Saved Data
The crawled database is dumped and compressed into a zip file. (will be uploaded later.)
To load our database, install psql (PostgreSQL) 14.1.

Follow `dumpdbs.sh` to dump and load database.

Reference:
```
Dump: https://www.postgresqltutorial.com/postgresql-backup-database/
Load: https://www.postgresqltutorial.com/postgresql-restore-database/
```



## Database Schema
The schema will be generated automatically for you.

**pull_request**

column|type|nullable|description
---|---|---|---
id|BIGINT|NO|postgres id
gh_number|BIGINT|NO|github id uniquely accross issues and pull requests
title|TEXT|NO|pull request title
state|VARCHAR|NO|can be `open` or `closed`
author|TEXT|NO|author's github nickname
created_at|TIMESTAMPZ|NO|pull request creation timestamp

**commit**

column|type|nullable|description
---|---|---|---
id|BIGINT|NO|postgres id
message|TEXT|NO|commit message
pull_request_id|BIGINT|NO|foreign key referencing a pull request
hash|VARCHAR|NO|git hash (sha)
hash_parent|VARCHAR|YES|git hash of parent commit (sha)
tree|VARCHAR|NO|hash of git tree of commit
author|TEXT|NO|author's github nickname
created_at|TIMESTAMPZ|NO|commit creation timestamp

**comment**

column|type|nullable|description
---|---|---|---
id|BIGINT|NO|postgres id
message|TEXT|NO|comment body message
gh_id|BIGINT|NO|github id of comment
gh_reply_id|BIGINT|YES|github id of parent comment
pull_request_id|BIGINT|NO|foreign key referencing a pull request
commit_id|BIGINT|YES|foreign key referencing the corresponding commit
commit_fallback_id|BIGINT|YES|foreign key referencing the last commit of PR
hunk_diff|TEXT|YES|referenced hunk diff
hunk_file|TEXT|YES|file path of hunk diff
author|TEXT|NO|author's github nickname
created_at|TIMESTAMPZ|NO|comment creation timestamp

**git_file**

column|type|nullable|description
---|---|---|---
id|BIGINT|NO|postgres id
file_path|TEXT|NO|file path
file_content|TEXT|NO|file content
commit_id|BIGINT|NO|foreign key referencing the corresponding commit

**scraping_status**

column|type|nullable|description
---|---|---|---
key|VARCHAR|NO|status key
value|VARCHAR|NO|status value

Possible entries for **scraping_status**:

key|value|description
---|---|---
STAGE|`PULL_REQUESTS`, `COMMITS`, `COMMENTS`, `FILES`, `DONE`|the server will pickup the stage value and start at that stage
