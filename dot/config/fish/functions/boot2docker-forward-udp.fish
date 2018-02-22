function boot2docker-forward-udp
        for port in $argv
                VBoxManage modifyvm "boot2docker-vm" --natpf1 "fw-udp-$port,udp,127.0.0.1,$port,,$port"
                echo "$port -> $port"
         end
end
