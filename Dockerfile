FROM openliberty/open-liberty:kernel-java8-openj9-ubi

COPY --chown=1001:0  src/main/liberty/config/ /config
COPY --chown=1001:0  target/*.war /config/dropins/

EXPOSE 9080

# Run the server script and start the defaultServer by default.
ENTRYPOINT ["/opt/ol/wlp/bin/server", "run"]
CMD ["defaultServer"]
