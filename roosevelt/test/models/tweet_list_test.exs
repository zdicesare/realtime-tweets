defmodule Roosevelt.TweetListTest do
  require IEx
  use Roosevelt.ModelCase, async: false

  setup_all do
    {:ok,
     popular_tweet: Roosevelt.Fixtures.tweet_json(%{popularity: 10, id: 2}),
     unpopular_tweet: Roosevelt.Fixtures.tweet_json(%{popularity: 0, id: 3}),
     initial_state: [Poison.Parser.parse!(Roosevelt.Fixtures.tweet_json(%{popularity: 5, id: 1}))]}
  end

  test "updates internal list when receiving new tweets" do
     Roosevelt.Fixtures.publish_tweet %{popularity: 1, id: 5}
     # There's a race condition in that the assertion can execute before
     # the pubsub callback in TweetList. This sleep is a code smell, and
     # ideally Exredis should be mocked out of the tests entirely, anyways.
     :timer.sleep(1)
     assert Enum.member?(Enum.map(Roosevelt.TweetList.tweet_list, fn x -> x["id"] end), "5")
  end

  test "keeps tweets sorted descending by priority", context do
    {:noreply, state_with_two_tweets} = Roosevelt.TweetList.handle_cast(context[:popular_tweet], context[:initial_state])
    IEx.pry
    assert hd(state_with_two_tweets)["id"] == "2"

    {:noreply, state_with_three_tweets} = Roosevelt.TweetList.handle_cast(context[:unpopular_tweet], state_with_two_tweets)
    assert List.last(state_with_three_tweets)["id"] == "3"
  end
end
