#!/bin/bash

shopt -s globstar

head -n 5 backend/auth/auth-api/build/reports/pmd/main.xml > report.xml

for file in backend/**/build/reports/pmd/main.xml
do
    tail -n +6 "$file" | sed "$ d" >> report.xml
done

tail -n 1 backend/auth/auth-api/build/reports/pmd/main.xml >> report.xml
