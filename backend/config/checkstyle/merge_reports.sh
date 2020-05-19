#!/bin/bash

shopt -s globstar

head -n 2 backend/auth/auth-api/build/reports/checkstyle/main.xml > report.xml

for file in backend/**/build/reports/checkstyle/main.xml
do
    tail -n +3 "$file" | sed "$ d" >> report.xml
done


tail -n 1 backend/auth/auth-api/build/reports/checkstyle/main.xml >> report.xml
