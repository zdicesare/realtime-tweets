# Storm

The Storm code handles the obtaining and tracking of tweets, filtered to some topic, from Twitter's API. First, we connect to Twitter's streaming API and processes the influx of new tweets. For each tweet,
we get the HTML to display it from Twitter's oEmbed API, and assign them an initial popularity value of 0. Then, each tweet is sent via Redis pubsub to the Phoenix component. Additionally, in batches of 100, we obtain updates in bulk from Twitter and update the popularity values, considering the number of retweets/favorites, as well as the age of the tweet. The tweet is sent again to Phoenix with the updated value.

## Setup

Dependencies are managed with Maven, otherwise only Redis needs to be running in the background with default settings. Additionally, a [twitter4j.properties](http://twitter4j.org/en/configuration.html) file must be present in the `storm` directory, containing your Twitter API credentials.

To filter Twitter's streaming API to a particular set of keywords, pass them as arguments to `main`.

## Credit

Super helpful guide on setting up Storm, Eclipse, and Maven together: https://github.com/mbonaci/mbo-storm/wiki/Storm-setup-in-Eclipse-with-Maven,-Git-and-GitHub
