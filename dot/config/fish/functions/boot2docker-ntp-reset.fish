function boot2docker-ntp-reset
         boot2docker ssh -- sudo killall -9 ntpd
         boot2docker ssh -- sudo ntpclient -s -h pool.ntp.org
         boot2docker ssh -- sudo ntpd -p pool.ntp.org
end
