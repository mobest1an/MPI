FROM debian:bookworm-slim
RUN apt-get update && apt-get install -y --no-install-recommends \
    tsung erlang-base erlang-crypto erlang-inets erlang-public-key erlang-ssl \
    gnuplot perl ca-certificates curl \
  && rm -rf /var/lib/apt/lists/*
RUN mkdir -p /root/.tsung
WORKDIR /work