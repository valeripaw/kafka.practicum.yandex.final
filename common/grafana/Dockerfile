FROM grafana/grafana

USER root

RUN mkdir -p /etc/grafana/provisioning/datasources
RUN mkdir -p /etc/grafana/provisioning/dashboards
RUN mkdir -p /var/lib/grafana/dashboards

COPY datasource.yml /etc/grafana/provisioning/datasources/
COPY dashboards.yml /etc/grafana/provisioning/dashboards/
COPY dashboards /var/lib/grafana/dashboards

USER grafana
