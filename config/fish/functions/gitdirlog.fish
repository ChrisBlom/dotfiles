function gitdirlog
	set -x FILES (git ls-tree --name-only HEAD .)
	set -x MAX 20

	for f in $FILES;
		set n (echo -n  $f | wc -c)
		if [ "$n" -gt "$MAX" ]
			set MAX $n
		end
	end

	for f in $FILES;
		set str (git log -1 --decorate --pretty=format:"%C(green)%cr%Creset" $f)
		set str2 (git log -1 --decorate --pretty=format:"%C(cyan)%h%Creset %s %C(yellow)(%cn)%Creset" $f)
		printf "%-20s -- %s \t %s\n" "$f" "$str" "$str2"
	end

end
