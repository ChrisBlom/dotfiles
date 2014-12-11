function sshec2
  ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no ec2-user@(instance-dns $argv[1] | head -n 1)
end
