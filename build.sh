#!/bin/bash

mvn package
mv target/naive-bayes-0.0.1-SNAPSHOT-shaded.jar ./nb.jar