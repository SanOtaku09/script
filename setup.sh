#!/bin/bash
path="${BASH_SOURCE[0]}"
path="${path:0:${#path}-8}"
if [ "$path" == "./" ] ; then
    path="$PWD/"  
fi
is_already_setup=$( grep $path $HOME/.bashrc )
if [ "$is_already_setup" == "" ] ; then
    echo "Path Doesn't Exist"
    echo "export PATH=\$PATH:$path" >> "$HOME/.bashrc"
    echo "Done"
    echo "make script executable"
    chmod -R +x $path
    echo "Done"
    exit
else
    echo "Path has already been added"
fi
