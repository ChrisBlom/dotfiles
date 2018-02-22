function tail-instances
        set -x file "$argv[2]"
        set -x tailcommand "less +F $file";
        for x in (instance-dns "$argv[1]") ;
                echo "opening $x, tailing $argv[2]: ssh-nocheck ec2-user@$x $tailcommand" ;
                tmux new-window -n "$x" "ssh-nocheck ec2-user@$x $tailcommand"
        end
end
