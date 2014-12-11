# set all env vars :
for pair in (cat  ~/.config/env_vars);
        set var (echo $pair | awk '{print $1}')
        set value (echo $pair | awk '{print $2}')
        #echo "setting $var to $value"
        set -x $var $value
        launchctl setenv $var $value
end;

# Use 256 color terminal
set -x TERM xterm-256color
set TERM xterm-256color


function fish_title
  true
end

function docker-push
         set image $argv[1]
         set endpoint $argv[2]
         echo "pushing $image to $endpoint"
         docker tag "$image" "$endpoint/$image"
         docker push "$endpoint/$image"
end

function docker-pull
         set image $argv[1]
         set endpoint $argv[2]
         echo "pulling $image from $endpoint"
         docker pull "$endpoint/$image"
         docker tag "$endpoint/$image" "$image"
end

function docker-kill-all
         docker kill (docker ps -q)
end

function docker-rm-all
         docker rm (docker ps -a -q)
end

function docker-rmi
         set pattern $argv[1]
         docker rmi (docker ps | grep $pattern | awk '{print $3}')
end

function await --description 'try to run first argument until it succeed, with pauses of 5s in between'
         while not eval $argv[1] ; sleep 5 ; end
end

function tabnam
         set name $argv[1]
         printf "\e]1;$name\a"
end

function docker-clean
         docker rmi (docker images | tail -n+1 | grep '<none>' | awk '{print $3}')
end

function docker-viz
         docker images -v | dot -Tsvg > /tmp/images.svg ; open /tmp/images.svg
end


# https://github.com/fish-shell/fish-shell/issues/107
function fish_title
  true
end
