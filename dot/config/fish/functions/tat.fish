function tat
	if [ "$TMUX" = "" ]
		tmux at -t $argv; or tmux new -s $argv
	else
		tmux switch -t $argv
	end
end
