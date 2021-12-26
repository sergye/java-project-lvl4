setup:
	gradle wrapper --gradle-version 7.0

clean:
	./gradlew clean

build:
	./gradlew clean build

start:
	APP_ENV=development ./build/install/app/bin/app

install:
	./gradlew install

start-dist:
	APP_ENV=production ./build/install/app/bin/app

generate-migrations:
	./gradlew generateMigrations

lint:
	./gradlew checkstyleMain checkstyleTest

test:
	./gradlew test

report:
	./gradlew jacocoTestReport

check-updates:
	./gradlew dependencyUpdates

.PHONY: build