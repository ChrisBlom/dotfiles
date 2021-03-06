function ec2hosts
	set profile "$argv[1]"

	# expand abbreviations
	if [ "$profile" = "p" ]
		set profile "production"
	end

	if [ "$profile" = "s" ]
		set profile "staging"
	end

	set PATTERN "$profile-$argv[2]"

	set --erase argv[1]
	set --erase argv[1]
	for i in $argv
	 	set PATTERN "$PATTERN*$i"
 	end
	echo PATTERN $PATTERN >&2
	# return hostnames of all instances of which the Name tag matches the pattern
	set res (aws --profile $profile --region us-east-1 ec2 describe-instances \
	--filters "Name=tag:Name,Values=$PATTERN" "Name=instance-state-name,Values=running" \
	--output json \
	--query 'Reservations[].Instances[].{Hostname:PublicDnsName,Name:[Tags[?Key==`Name`].Value][0][0]}')

	echo "--- Matched: -------------------------------------------------------------------" >&2
	echo $res | jq '.[].Name' -r | sort | uniq -c  >&2
	echo "--------------------------------------------------------------------------------" >&2


	echo $res | jq '.[].Hostname' -r


end
