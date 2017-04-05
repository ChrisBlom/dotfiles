function powerlein
	set-title (basename (PWD))
	lein with-profile +power $argv
end
