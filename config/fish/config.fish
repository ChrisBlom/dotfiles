# set all env vars t:
for pair in (cat ~/.config/env_vars);
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

test -e {$HOME}/.iterm2_shell_integration.fish ; and source {$HOME}/.iterm2_shell_integration.fish


# switch (docker-machine status default)
# 	case Running
# 		eval (docker-machine env default)
# 	case '*'
# 		echo "docker-machine default is down"
# end

# Due to a bug of fish, we cannot use command substitution,
# so we use temporary file instead
if [ -z "$TMPDIR" ]
  set -g TMPDIR /tmp
end

function __fzf_escape
  while read item
    echo -n (echo -n "$item" | sed -E 's/([ "$~'\''([{<>})])/\\\\\\1/g')' '
  end
end

function fzf-file-widget
  set -q FZF_CTRL_T_COMMAND; or set -l FZF_CTRL_T_COMMAND "
  command find -L . \\( -path '*/\\.*' -o -fstype 'dev' -o -fstype 'proc' \\) -prune \
    -o -type f -print \
    -o -type d -print \
    -o -type l -print 2> /dev/null | sed 1d | cut -b3-"
  eval "$FZF_CTRL_T_COMMAND | "(__fzfcmd)" -m $FZF_CTRL_T_OPTS > $TMPDIR/fzf.result"
  and for i in (seq 20); commandline -i (cat $TMPDIR/fzf.result | __fzf_escape) 2> /dev/null; and break; sleep 0.1; end
  commandline -f repaint
  rm -f $TMPDIR/fzf.result
end

function fzf-history-widget
  history | eval (__fzfcmd) +s +m --tiebreak=index --toggle-sort=ctrl-r $FZF_CTRL_R_OPTS > $TMPDIR/fzf.result
  and commandline (cat $TMPDIR/fzf.result)
  commandline -f repaint
  rm -f $TMPDIR/fzf.result
end
function fzf-cd-widget
  set -q FZF_ALT_C_COMMAND; or set -l FZF_ALT_C_COMMAND "
  command find -L . \\( -path '*/\\.*' -o -fstype 'dev' -o -fstype 'proc' \\) -prune \
    -o -type d -print 2> /dev/null | sed 1d | cut -b3-"
  # Fish hangs if the command before pipe redirects (2> /dev/null)
  eval "$FZF_ALT_C_COMMAND | "(__fzfcmd)" +m $FZF_ALT_C_OPTS > $TMPDIR/fzf.result"
  [ (cat $TMPDIR/fzf.result | wc -l) -gt 0 ]
  and cd (cat $TMPDIR/fzf.result)
  commandline -f repaint
  rm -f $TMPDIR/fzf.result
end

function __fzfcmd
  set -q FZF_TMUX; or set FZF_TMUX 1
  if [ $FZF_TMUX -eq 1 ]
    if set -q FZF_TMUX_HEIGHT
      echo "fzf-tmux -d$FZF_TMUX_HEIGHT"
    else
      echo "fzf-tmux -d40%"
    end
  else
    echo "fzf"
  end
end

bind \ct fzf-file-widget
bind \cr fzf-history-widget
bind \ec fzf-cd-widget

if bind -M insert > /dev/null 2>&1
  bind -M insert \ct fzf-file-widget
  bind -M insert \cr fzf-history-widget
  bind -M insert \ec fzf-cd-widget
end

# Path to Oh My Fish install.
set -q XDG_DATA_HOME
  and set -gx OMF_PATH "$XDG_DATA_HOME/omf"
  or set -gx OMF_PATH "$HOME/.local/share/omf"

# Load Oh My Fish configuration.
source $OMF_PATH/init.fish

fzf-key-bindings

# OPAM configuration
# . /Users/chris/.opam/opam-init/init.fish > /dev/null 2> /dev/null or true


bash /usr/local/bin/virtualenvwrapper.sh
