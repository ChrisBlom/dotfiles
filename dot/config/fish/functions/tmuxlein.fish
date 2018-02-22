
function tmuxlein

	set SESSIONNAME "leinrepls"
	set WINDOWNAME (echo (pwd):(__fish_git_prompt) (date +"%D %T") )
	set COMMAND 'powerlein do clean, repl'

	echo $WINDOWNAME

	if tmux new -d -s $SESSIONNAME
		echo "Created session $SESSIONNAME"
	else
		echo "Session already exists"
	end

	echo "Running $COMMAND"
	tmux new-window -t $SESSIONNAME -n $WINDOWNAME  $COMMAND

end
