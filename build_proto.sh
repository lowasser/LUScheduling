#!/bin/sh

protoc --proto_path=src/ --java_out=src src/org/learningu/scheduling/proto/*
