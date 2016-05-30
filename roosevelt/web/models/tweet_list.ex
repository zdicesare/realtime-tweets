defmodule Roosevelt.TweetList do
  use GenServer

  def start_link(initial_state) do
    GenServer.start_link __MODULE__, initial_state, name: __MODULE__
  end

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

  def handle_cast(tweet, state) do
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

  defp delete_if_needed(list, tweet) do
    case Enum.find_index(list, fn x -> x["id"] == tweet["id"] end) do
      nil ->
        {list, nil}
      x ->
        {List.delete_at(list, x), x}
    end
  end

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
