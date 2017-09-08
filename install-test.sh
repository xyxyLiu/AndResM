#!/bin/sh

./gradlew clean :andresm:assemble && ./gradlew :andresm:uploadArchives && clear && ./gradlew --stacktrace :app:clean :app:aR