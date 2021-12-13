#/bin/bash

repos=(
GitTools/GitVersion
)


for repo in "${repos[@]}"
do
    PASSWORD=passward createdb $repo
done