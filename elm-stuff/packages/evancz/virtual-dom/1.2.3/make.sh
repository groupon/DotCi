#!/bin/bash

browserify src/wrapper.js -o src/Native/VirtualDom.js
echo ";" >> src/Native/VirtualDom.js
