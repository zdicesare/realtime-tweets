defmodule Roosevelt.TweetList do
  use GenServer

  def start_link(initial_state) do
    GenServer.start_link __MODULE__, initial_state, name: __MODULE__
  end

  def init(initial_state) do
    {:ok, client_sub} = Exredis.Sub.start_link
    pid = Kernel.self
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
    result = Enum.find_index(state, fn x -> x["popularity"] < parsed_tweet["popularity"] end)
    index = case result do
              nil ->
                0
              _ ->
                result
            end
    new_state = List.insert_at(state, index, parsed_tweet) |> Enum.take(50)
    Roosevelt.Endpoint.broadcast "redis:listen", "new:response", %{tweet: parsed_tweet, index: index}
    {:noreply, new_state}
  end

  defp parse_json(json), do: Poison.Parser.parse! json
end
