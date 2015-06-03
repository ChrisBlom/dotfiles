function mec2
	set name $argv[1]
	set inst (instance-dns $name | head -n 1)
	if [ $inst = "" ]
		exit 1
	end

	set -q argv[2]; and set cmd $argv[2-1]
	echo cmd=$cmd
	mosh --ssh="ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no" -p 60001 ec2-user@$inst $cmd
end
