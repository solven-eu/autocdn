# https://devcenter.heroku.com/articles/procfile#procfile-naming-and-location
# https://stackoverflow.com/questions/50428897/avoid-hard-coding-heroku-procfile-jar-name
# https://stackoverflow.com/questions/36751071/heroku-web-process-failed-to-bind-to-port-within-90-seconds-of-launch-tootall
web: java -Dserver.port=$PORT -jar target/autocdn-*.jar

# https://devcenter.heroku.com/articles/release-phase
release: ./release-tasks.sh