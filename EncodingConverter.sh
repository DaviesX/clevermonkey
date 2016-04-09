#!/bin/bash

files=($(find . -name '*.java'))

for file in ${files[@]}; do
        echo "converting: $file"
        luit -encoding gbk -c < "$file" > "$file""_tmp"
        mv "$file""_tmp" "$file"
done
