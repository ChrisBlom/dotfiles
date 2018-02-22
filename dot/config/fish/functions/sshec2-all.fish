function sshec2-all --description "sshec2-all <instance-name> <commands>*"



	set profile $argv[1]
	set name $argv[2]

	set --erase argv[1]
	set --erase argv[1]

	for i in $argv
	 	set cmds "$cmds $i"
 	end

	echo "Running cmd: <$cmds> on "(count cmds)" instances"

	set instances (ec2hosts $profile $name)

	set j 1

	# count hostnames and trim result
	set n (echo $instances | wc -w | xargs)

	for i in $instances
		set_color "red"
		echo "=== ($j/$n) [$i] ==="
		set_color normal
		ssh -q -o ConnectTimeout=10 -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -t ec2-user@$i $cmds
		set j (math "$j+1")
	end
end
