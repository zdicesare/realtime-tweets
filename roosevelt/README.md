# Roosevelt
This Phoenix component subscribes to a Redis pubsub channel and is notified of new tweets, each of which contains a popularity value as well as the HTML to display. A GenServer maintains a list of 50 tweets sorted by popularity,
and broadcasts them via a web socket to each browser client. Some JavaScript code renders the sorted list and handles visually adding and removing tweets.

The GenServer for maintaining our list of tweets is in `web/models/tweet_list.ex`
Web sockets are created and handled in `web/channels/redis_channel.ex`
JS is in `web/static/js/app.js`

## Setup
Clone the repo, run `mix deps.get`, have Redis running, and then run `mix phoenix.server`. The web server runs on `localhost:4000`.

Tests can be run with `mix test`, but right now Postgres needs to be running locally due to how the default Phoenix test helpers use Ecto.

## Credit

Some helpful resources that were used in writing the Phoenix app:  
http://www.chrismccord.com/blog/2014/05/06/how-to-build-an-elixir-chat-app-with-phoenix/  
https://quickleft.com/blog/creating-game-lobby-system-phoenix-websockets/
