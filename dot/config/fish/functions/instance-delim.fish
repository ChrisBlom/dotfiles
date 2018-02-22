function instance-delim
        instance-dns argv[1] | awk -v ORS=, "\'{ print $1:\"$[argv2]\" }\'"
end
