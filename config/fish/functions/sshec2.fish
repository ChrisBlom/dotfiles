function sshec2
	set name $argv[1]
#	set -q argv[2]; and set cmd "$argv[2-1]"
	echo cmd=$cmd
	set instance (instance-dns $name | head -n 1)


	#if [ cmd = "" ]
		ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no ec2-user@$instance
#	else
#		ssh -t -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no ec2-user@$instance "$cmd"
#	end
end
