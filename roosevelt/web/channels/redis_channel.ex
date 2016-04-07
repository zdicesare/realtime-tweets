defmodule Roosevelt.RedisChannel do
  use Phoenix.Channel

  def join("redis:listen", _message, socket) do
    backfill socket
    {:ok, socket}
  end

  def backfill(socket) do
    tweet_list = Roosevelt.TweetList.tweet_list
    case tweet_list do
      [] ->
        send self, {:after_join, :empty}
      _ ->
        Enum.each tweet_list, fn tweet ->
          send self, {:after_join, tweet}
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
