function sshec2-all --description "sshec2-all <instance-name> <commands>*"



	set name $argv[1]

	set --erase argv[1]

	for i in $argv
	 	set cmds "$cmds $i"
 	end

	echo "Cmd: $cmds"

	for i in (instance-dns $name)
		echo ""
		echo "== Instance: $i ==================================="
		ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -t ec2-user@$i $cmds
	end
end
