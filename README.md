This is a toy project on doing real time processing of tweets with Apache Storm and Phoenix. Storm is used to connect to Twitter's streaming API and obtain and track new tweets about some topic, while Phoenix uses web sockets to
present a list of the most popular tweets we've seen so far to the user. So, in real time, we see the most popular content rise to the top out of a deluge of tweets, and we can see the discussion change with time, as older tweets lose
their ranking amid the firehose of tweets.

Some more documentation on each component is given in each readme.

![Initial tweets populating](https://i.imgur.com/ZiOVDby.png)
![Popular tweets rising](https://i.imgur.com/rbjVaJD.png)
