# syntax=docker/dockerfile:1.0

FROM mcr.microsoft.com/dotnet/sdk:8.0

RUN dotnet tool install --global dotnet-sonarscanner
RUN set -ex; \
  apt-get update; \
  apt-get install -y --no-install-recommends default-jre ; \
  rm -rf /var/lib/apt/lists/*

# Add global dotnet tools to PATH
ENV PATH="$PATH:/root/.dotnet/tools"

COPY ./docker_dotnetscan_entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

WORKDIR /src
ENTRYPOINT ["/entrypoint.sh"]
CMD ["-h"]
