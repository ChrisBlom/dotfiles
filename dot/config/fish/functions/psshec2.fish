function psshec2 --description "psshec2 <profile> <instance-name> <commands>*"

	set profile $argv[1]
	set name $argv[2]

	set --erase argv[1]
	set --erase argv[1]

	set hosts (ec2hosts $profile $name)

	rm -f /tmp/pssh/*

	pssh -t 100000000 -H "$hosts" -l ec2-user -p 10  -O UserKnownHostsFile=/dev/null -O StrictHostKeyChecking=no -o /tmp/pssh $argv

	for f in (ls /tmp/pssh)
		echo "=== " $f " ==="
		cat /tmp/pssh/$f
	end

end
