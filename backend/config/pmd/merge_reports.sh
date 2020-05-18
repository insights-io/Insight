#!/bin/zsh

head -n 5 backend/auth/auth-api/build/reports/pmd/main.xml > report.xml
zsh -c 'for file in backend/**/build/reports/pmd/main.xml;do;cat "$file" | tail -n +6 | sed "$ d" >> report.xml;done'
tail -n 1 backend/auth/auth-api/build/reports/pmd/main.xml >> report.xml
