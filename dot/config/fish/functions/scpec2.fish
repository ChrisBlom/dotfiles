function scpec2 --description "scpec2 <instance-name> <commands>*"

	set name $argv[1]
	set instance (instance-dns $name | head -n 1)

	echo "Command: $name = $instance , src = "$argv[2]" trg = " $argv[3]

	set-title "scp "$name
	scp -v -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -t ec2-user@$instance:$argv[2] $argv[3]



end
