defmodule Roosevelt.TweetList do
  # Maintains list of top 50 tweets sorted by popularity, and broadcasts new tweets along with their information (where it was inserted in the last, where it was first deleted from if its an updated tweet) to clients
  use GenServer

  def start_link(initial_state) do
    GenServer.start_link __MODULE__, initial_state, name: __MODULE__
  end

  # Subscribes to the Redis channel
  def init(initial_state) do
    {:ok, client_sub} = Exredis.Sub.start_link
    client_sub |> Exredis.Sub.subscribe "storm_tweet_feed", fn(msg) ->
      case msg do
        {:message, _, tweet, _} ->
          GenServer.cast(__MODULE__, tweet)
        {:subscribed, _, _} ->
      end
    end
    {:ok, initial_state}
  end

  def tweet_list, do: GenServer.call(__MODULE__, :tweet_list)

  def handle_call(:tweet_list, _from, state), do: {:reply, state, state}

  # Entry point for inserting/updating a tweet
  def handle_cast(tweet, state) do
    # TODO: We do a number of linear time searches of our list just to insert one tweet. We could use an auxiliary
    # structure like a map to store the ids of tweets, to not have to do a linear search of the list just to see if
    # we need to delete it. Alternatively, we could use a different underlying data structure than a list to keep track
    # of our state.
    parsed_tweet = parse_json tweet
    {deleted_state, deletion_index} = delete_if_needed state, parsed_tweet
    {inserted_state, insertion_index} = insert deleted_state, parsed_tweet
    Roosevelt.Endpoint.broadcast "redis:listen", "new:response", %{tweet: parsed_tweet, insertion_index: insertion_index, deletion_index: deletion_index}
    {:noreply, inserted_state}
  end

  defp parse_json(json), do: Poison.Parser.parse! json

  defp less_popular?(tweet_one, tweet_two) do
    {popularity_one, _} = Float.parse tweet_one["popularity"]
    {popularity_two, _} = Float.parse tweet_two["popularity"]
    popularity_one < popularity_two
  end

  # If we've seen the tweet before, first remove it from the list
  defp delete_if_needed(list, tweet) do
    case Enum.find_index(list, fn x -> x["id"] == tweet["id"] end) do
      nil ->
        {list, nil}
      x ->
        {List.delete_at(list, x), x}
    end
  end

  # Add the new tweet to the list, maintaining order
  defp insert(list, tweet) do
    insertion_index = case Enum.find_index(list, fn x -> less_popular? x, tweet end) do
                        nil ->
                          length list
                        result ->
                          result
                      end
    {List.insert_at(list, insertion_index, tweet) |> Enum.take(50), insertion_index}
  end
end
