function sshec2 --description "sshec2 <instance-name> <commands>*"



	set name $argv[1]

	set --erase argv[1]

	for i in $argv
		echo $i
	 	set cmds "$cmds $i"
 	end


	set instance (instance-dns $name | head -n 1)

	echo "Command: $cmds"

	ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -t ec2-user@$instance $cmds
end
