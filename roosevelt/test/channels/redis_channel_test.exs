defmodule Roosevelt.RedisChannelTest do
  use Roosevelt.ChannelCase

  setup do
    @endpoint = Roosevelt.Endpoint
    {:ok, _, socket} =
      socket("user:id", %{})
      |> subscribe_and_join(Roosevelt.RedisChannel, "redis:listen", %{})
    {:ok, socket: socket}
  end

  test "receives a broadcast when new tweet is added" do
    Roosevelt.Fixtures.publish_tweet(%{id: 4, popularity: 1})
    assert_broadcast "new:response", %{tweet: _, index: _}
  end
end
