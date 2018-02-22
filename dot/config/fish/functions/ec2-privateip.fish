function ec2-privateip
  set -x NAME "$argv[1]"
  for l in (aws ec2 describe-instances \
      --filters "Name=tag:Name,Values=$NAME" "Name=instance-state-name,Values=running" \
      --output text \
      --query 'Reservations[*].Instances[*].PrivateDnsName' | sort)
      echo $l | perl -p -e 's{\t+}{\n}g'
  end

end
