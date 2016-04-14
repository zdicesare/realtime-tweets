// Brunch automatically concatenates all files in your
// watched paths. Those paths can be configured at
// config.paths.watched in "brunch-config.js".
//
// However, those files will only be executed if
// explicitly imported. The only exception are files
// in vendor, which are never wrapped in imports and
// therefore are always executed.

// Import dependencies
//
// If you no longer want to use a dependency, remember
// to also remove its path from "config.paths.watched".
//import "phoenix_html"

// Import local files
//
// Local files can be imported directly using relative
// paths "./socket" or full ones "web/static/js/socket".

// import socket from "./socket"
//

import {Socket, LongPoller} from "phoenix"

class App {
  static init(){
    let socket = new Socket("/socket", {
      logger: ((kind, msg, data) => { console.log(`${kind}: ${msg}`, data) })
     })

    let default_msg = function(msg) {
      $("#tweet-collection").append(msg.html);
    }

    let backfill = function(tweet) {
      $("#default-message").remove();
      var body = "<li class='tweet-container tweet-col " + visibility + "' data-popularity='" + tweet.popularity + "'>" + tweet.html + "</li>";
      $("#tweet-collection").append(body);
      twttr.widgets.load();
    }

    let update = function(msg) {
      $("#default-message").remove();
      var body = "<li class='tweet-container tweet-col " + visibility + " data-popularity='" + msg.tweet.popularity + "'>" + msg.tweet.html + "</li>";
      if (msg.index == 0) {
        $("#tweet-collection").prepend(body);
      }
      else {
        $("#tweet-collection > li:nth-child(" + (msg.index) + ")").after(body);
      }
      if ($(".tweet-container").length == 50) {
        $(".tweet-col:last-child").remove();
      }
      twttr.widgets.load();
    }

    socket.connect({})
    socket.onOpen( ev => console.log("OPEN", ev) )
    socket.onError( ev => console.log("ERROR", ev) )
    socket.onClose( e => console.log("CLOSE", e))

    var chan = socket.channel("redis:listen", {})
    chan.join().receive("ignore", () => console.log("auth error"))
               .receive("ok", () => console.log("join ok"))
               .after(10000, () => console.log("Connection interruption"))
    chan.onError(e => console.log("something went wrong", e))
    chan.onClose(e => console.log("channel closed", e))
    chan.on("new:default", default_msg);
    chan.on("new:backfill", backfill);
    chan.on("new:response", update);
  }

}
$( () => App.init() )
export default App
