function into-instances
        set instances (instance-dns "$argv[1]")
#        echo "Sending $argv2 to "
        for x in $instances
                echo "openening $x";
                echo (tmux new-window -n "$x" "ssh-nocheck ec2-user@$x")
        end
end
