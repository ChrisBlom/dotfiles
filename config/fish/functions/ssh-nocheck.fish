function ssh-nocheck
  ssh -t -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $argv
end
