FROM erlang:25-slim
ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    ca-certificates wget \
    make gcc autoconf automake libtool \
    libxml2-dev libexpat1-dev libssl-dev \
    perl libtemplate-perl libfile-spec-perl \
    libhtml-template-perl libgd-gd2-perl \
    gnuplot fontconfig \
    postgresql-client && \
    update-ca-certificates && \
    \
    wget https://github.com/processone/tsung/archive/refs/tags/v1.8.0.tar.gz && \
    tar -xzf v1.8.0.tar.gz && \
    cd tsung-1.8.0 && \
    ./configure && make && make install && \
    cd / && rm -rf tsung-1.8.0 v1.8.0.tar.gz && \
    \
    ln -s /usr/local/share/tsung/bin/tsung_stats.pl /usr/local/bin/tsung_stats.pl && \
    \
    apt-get purge -y gcc autoconf automake libtool && \
    apt-get autoremove -y && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    iproute2 net-tools iputils-ping \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /root/.tsung/log

WORKDIR /tsung
