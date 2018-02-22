function dm
	set activemachine (docker-machine active)
	echo "Active machine is: "$activemachine
	docker-machine $argv $activemachine

	if [ $argv = "start" ]
		eval (docker-machine env $activemachine)
	end
end
