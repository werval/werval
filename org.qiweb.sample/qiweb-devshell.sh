#!/bin/sh
set -e

M2_REPO="${HOME}/.m2/repository/"
GRADLE_CACHE="${HOME}/.gradle/caches/artifacts-23/filestore/"

SLF4J="${M2_REPO}org/slf4j/slf4j-api/1.7.2/slf4j-api-1.7.2.jar"
LOGBACK="${M2_REPO}ch/qos/logback/logback-core/1.0.9/logback-core-1.0.9.jar:${M2_REPO}ch/qos/logback/logback-classic/1.0.9/logback-classic-1.0.9.jar"
GRADLE_TOOLING="${M2_REPO}org/gradle/gradle-tooling-api/1.4/gradle-tooling-api-1.4.jar"
NETTY="${GRADLE_CACHE}io.netty/netty-codec-http/4.0.0.Beta2/bundle/489657a84e172f474985cae95bd797360301cd6f/netty-codec-http-4.0.0.Beta2.jar:${GRADLE_CACHE}io.netty/netty-transport/4.0.0.Beta2/bundle/5939b79bbc78ab0964f2f9dd2e02886d15d9baac/netty-transport-4.0.0.Beta2.jar:${GRADLE_CACHE}io.netty/netty-common/4.0.0.Beta2/bundle/12a9ce0aec63b70c8e3171019834055a3e025617/netty-common-4.0.0.Beta2.jar:${GRADLE_CACHE}io.netty/netty-buffer/4.0.0.Beta2/bundle/485c20a3756ff00368a4b05ffe8214d2c1921c03/netty-buffer-4.0.0.Beta2.jar:${GRADLE_CACHE}io.netty/netty-codec/4.0.0.Beta2/bundle/5de1b8a0ac46de4983eabfd360b3757b3af0538e/netty-codec-4.0.0.Beta2.jar:${GRADLE_CACHE}io.netty/netty-transport/4.0.0.Beta2/bundle/5939b79bbc78ab0964f2f9dd2e02886d15d9baac/netty-transport-4.0.0.Beta2.jar:${GRADLE_CACHE}io.netty/netty-handler/4.0.0.Beta2/bundle/e61f91d79ca7d91b16a6f7af44d6c803f478d5e3/netty-handler-4.0.0.Beta2.jar"
JNOTIFY="${GRADLE_CACHE}net.contentobjects.jnotify/jnotify/0.94/jar/f12e865ad170b24074fe8fa4f41f255cb6139dcb/jnotify-0.94.jar"

QIWEB_API="../org.qiweb/org.qiweb.api/build/libs/org.qiweb.api-0.jar"
QIWEB_BOOTSTRAP="../org.qiweb/org.qiweb.bootstrap/build/libs/org.qiweb.bootstrap-0.jar"
QIWEB_RUNTIME="../org.qiweb/org.qiweb.runtime/build/libs/org.qiweb.runtime-0.jar"
QIWEB_DEVSHELL="../org.qiweb/org.qiweb.devshell/build/libs/org.qiweb.devshell-0.jar"


java -cp ${SLF4J}:${LOGBACK}:${GRADLE_TOOLING}:${NETTY}:${JNOTIFY}:$QIWEB_API:$QIWEB_BOOTSTRAP:$QIWEB_RUNTIME:$QIWEB_DEVSHELL org.qiweb.devshell.QiWebDevShell

# java -cp ${HOME}/.m2/repository/org/slf4j/slf4j-api/1.7.2/slf4j-api-1.7.2.jar:${HOME}/.m2/repository/ch/qos/logback/logback-core/1.0.9/logback-core-1.0.9.jar:${HOME}/.m2/repository/ch/qos/logback/logback-classic/1.0.9/logback-classic-1.0.9.jar:${HOME}/.m2/repository/org/gradle/gradle-tooling-api/1.4/gradle-tooling-api-1.4.jar:${HOME}/.gradle/caches/artifacts-23/filestore/net.contentobjects.jnotify/jnotify/0.94/jar/f12e865ad170b24074fe8fa4f41f255cb6139dcb/jnotify-0.94.jar:../org.qiweb/org.qiweb.api/build/libs/org.qiweb.api-0.jar:../org.qiweb/org.qiweb.bootstrap/build/libs/org.qiweb.bootstrap-0.jar:../org.qiweb/org.qiweb.runtime/build/libs/org.qiweb.runtime-0.jar:../org.qiweb.devshell/build/libs/org.qiweb.devshell-0.jar org.qiweb.devshell.QiWebDevShell


