FROM ubuntu:latest

MAINTAINER "Daniel Vargas" <dlvargas@stanford.edu>

# Install build tools
RUN apt-get update
RUN apt-get -y install git subversion ant gettext openjdk-8-jdk-headless maven locales

# Set LANG (needed for msginit -- called by lockss-daemon build.xml)
ENV LANG en_US.UTF-8
RUN locale-gen ${LANG}

# Add LAAWS Poller source
ADD . /laaws-poller

# Build LOCKSS daemon JARs
WORKDIR /laaws-poller
RUN mvn clean package

# XXX Clean up 
RUN apt-get clean
RUN rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

CMD ["/bin/sh", "/laaws-poller/buildAndRun", "-Dswarm.http.port=8888", "-Djava.net.preferIPv4Stack=true"]
