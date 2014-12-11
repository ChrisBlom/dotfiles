function instance-dns
        set NAME "$argv[1]"
        # echo "looking for instance named $NAME"

        aws ec2 describe-instances \
        --filters "Name=tag:Name,Values=$NAME" "Name=instance-state-name,Values=running" \
        --output text --query 'Reservations[*].Instances[*].PublicDnsName'
end
