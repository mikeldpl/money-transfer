#!/bin/bash

docker build -t mikeldpl/moneytransfer . &&
docker build -t mikeldpl/moneytransfer-native -f Dockerfile.native .