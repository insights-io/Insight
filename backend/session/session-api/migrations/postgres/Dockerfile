FROM flyway/flyway:6.5.1

WORKDIR /usr/app

COPY scripts /usr/app/scripts
COPY sql /usr/app/sql

ENTRYPOINT ["/usr/app/scripts/migrate.sh"]