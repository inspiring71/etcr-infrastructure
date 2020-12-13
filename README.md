# Github PR Scraper
Get useful information about pull requests of a repository

## Getting Started

Using JAR:
1. Add `application.properties` file next to the jar:
```
# scraping config
scraping.repository=elastic/elasticsearch
scraping.auth=token e8c931efc42c74c2e4e027a2a82cc2cf2a58246b

# database config
spring.datasource.url=jdbc:postgresql://localhost:5432/pr
spring.datasource.username=myuser
spring.datasource.password=123456
```

2. Run JAR:

Run: `java -jar prscraper-1.0.0.jar`

With custom path to properties file: `java -jar prscraper-1.0.0.jar --spring.config.location=<path to application.properties>`

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
