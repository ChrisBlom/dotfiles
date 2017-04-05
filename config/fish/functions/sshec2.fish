function sshec2 --description "sshec2 <instance-name> <commands>*"

	set profile $argv[1]
	set name $argv[2]

	set --erase argv[1]
	set --erase argv[1]

	for i in $argv
	 	set cmds "$cmds $i"
 	end

	set instance (ec2host $profile $name)

	echo "Command: $cmds Instance: $name = $instance"

	set-title "ssh "$name

	ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -t ec2-user@$instance $cmds
end
