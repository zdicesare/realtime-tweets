defmodule Roosevelt.Fixtures do

  def publish_tweet(options) do
    Exredis.Api.publish "storm_tweet_feed", tweet_json(options)
  end

  def tweet_json(%{popularity: popularity, id: id}) do
    """
    {\"popularity\":\"#{popularity}\",\"html\":\"<blockquote class=\\\"twitter-tweet\\\" data-conversation=\\\"none\\\" data-cards=\\\"hidden\\\" data-width=\\\"220\\\"><p lang=\\\"en\\\" dir=\\\"ltr\\\">Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse enim nibh, tincidunt at pretium et, congue et mi. Etiam at dolor non leo imperdiet rutrum nec a quam.</p>&mdash; jack (@jack) <a href=\\\"https://twitter.com/jack/status/20\\\">April 8, 2016</a></blockquote>\\n\",\"id\":\"#{id}\"}
    """
  end
end
