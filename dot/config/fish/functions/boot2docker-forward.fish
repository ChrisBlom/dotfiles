function boot2docker-forward
        for port in $argv
                VBoxManage modifyvm "boot2docker-vm" --natpf1 "fw$port,tcp,127.0.0.1,$port,,$port"
                echo "$port -> $port"
         end
end
