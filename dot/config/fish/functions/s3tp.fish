
function s3tp

	aws s3 ls --recursive $argv[1] | tee /tmp/ls.txt



	cat /tmp/ls.txt | awk '{ print $1 " " $2 }' | sort > /tmp/lss.txt

	set min (head -n 1 /tmp/lss.txt)
	set max (tail -n 1 /tmp/lss.txt)

	echo "Min: " $min
	echo "Max: " $max

	set mint (gdate --date=$min +%s)
	set maxt (gdate --date=$max +%s)

	set delta (math $maxt "-" $mint)

	echo -n "Timespan: "
	echo (gdate -u -d @$delta +"%T") " minutes , " $delta" seconds"


	set total (cat /tmp/ls.txt | awk '{ sum+=$3} END {print sum}')
	echo "Size: " $total " bytes, " (math $total / 1024 / 1024 )  "MiB"



	echo -n "MB/Sec:   "
	math $total / $delta / 1024 / 1024


	set objs (wc -l /tmp/ls.txt  | awk '{print $1}')
	echo -n "Msgs/sec:  "

	set objsize 25000

	awk (echo "BEGIN { print " $objsize " * " $objs "/" $delta "}")




end
