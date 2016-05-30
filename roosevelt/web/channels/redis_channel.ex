defmodule Roosevelt.RedisChannel do
  # Handles web sockets for browser clients
  use Phoenix.Channel

  def join("redis:listen", _message, socket) do
    backfill socket
    {:ok, socket}
  end

  # When a new browser client joins, give them our current state
  def backfill(socket) do
    tweet_list = Roosevelt.TweetList.tweet_list
    case tweet_list do
      [] ->
        send self, {:after_join, :empty}
      _ ->
        tweet_list |> Enum.with_index |> Enum.each fn {tweet, index} ->
          send self, {:after_join, %{tweet: tweet, insertion_index: index}}
        end
    end
  end

  def handle_info({:after_join, :empty}, socket) do
    push socket, "new:default", %{html: "<span id='default-message'>Looks like the real time Twitter topic feed isn't currently running.</span>"}
    {:noreply, socket}
  end

  def handle_info({:after_join, tweet}, socket) do
    push socket, "new:backfill", tweet
    {:noreply, socket}
  end


  def terminate(_, _socket) do
    :ok
  end
end
