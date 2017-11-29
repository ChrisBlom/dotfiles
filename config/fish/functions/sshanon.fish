function sshanon --description "sshec2 <instance-name> <commands>*"

	set name $argv[1]
	set --erase argv[1]

	ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -t ec2-user@$name $argv

end
