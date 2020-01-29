FROM flyway/flyway:6.1.0

WORKDIR /usr/app

COPY scripts /usr/app/scripts
COPY sql /usr/app/sql

ENTRYPOINT ["/usr/app/scripts/migrate.sh"]