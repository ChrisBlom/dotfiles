function bssh --description "bssh <env> name"

	set environ $argv[1]
	set name $environ"-"$argv[2]

	set --erase argv[1]
	set --erase argv[1]

	for i in $argv
	 	set cmds "$cmds $i"
 	end

	set instance (ec2-privateip $name | head -n 1)

	echo "asdas"  (ec2-privateip $name | head -n 1)

	set bastion bastion.{$environ}.adgoji.com
	echo "name" $name
 	echo "Bastion "$bastion
	echo "instance "$instance

	ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -t ec2-user@$bastion ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no ec2-user@$instance $cmds
end
