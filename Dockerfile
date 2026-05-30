FROM maven:3.9-eclipse-temurin-21

ENV DEBIAN_FRONTEND=noninteractive

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        libgl1 \
        libopenal1 \
        libx11-6 \
        libxrandr2 \
        libxcursor1 \
        libxi6 \
        libxinerama1 \
        libsm6 \
        libice6 \
        libsndfile1 \
        libudev1 \
    && rm -rf /var/lib/apt/lists/*

COPY src src
COPY assets assets
COPY lib lib
COPY pom.xml pom.xml

RUN mvn -q -DskipTests package

CMD ["java", "-cp", "target/classes:lib/jsfml.jar", "main.Main"]
