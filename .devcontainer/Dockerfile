FROM mcr.microsoft.com/devcontainers/java:17

USER root

RUN apt-get update && apt-get install -y \
    mysql-client \
    redis-tools \
    && rm -rf /var/lib/apt/lists/*

USER vscode
