function b2d-forward-port
	set name $argv[1]
	set port $argv[2]
	VBoxManage modifyvm "boot2docker-vm" --natpf1 "$name,tcp,127.0.0.1,$port,,$port"
end
