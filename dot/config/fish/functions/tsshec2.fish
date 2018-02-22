function tsshec2

	set SESSIONNAME "sshec2"
	set WINDOWNAME (echo (pwd):(__fish_git_prompt) (date +"%D %T") )

	echo $WINDOWNAME

	if tmux new -d -s $SESSIONNAME
		echo "Created session $SESSIONNAME"
	else
		echo "Session already exists"
	end

	echo "Running $COMMAND"
	tmux new-window -t $SESSIONNAME -n $WINDOWNAME (echo sshec2 $argv)
end
