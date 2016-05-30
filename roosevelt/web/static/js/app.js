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

    let remove_tweet = function(msg) {
      $(".tweet-container[data-id=" + msg.tweet.id + "]").remove()
      $(".tweet-container:eq(" + (displayable - 1) + ")").removeClass("tweet-hide").addClass("tweet-show");
    }

    let update_list = function(msg) {
      if(msg.deletion_index != null) {
        remove_tweet(msg)
      }
      else {
        count++;
      }

      var visibility;
      if (msg.insertion_index >= displayable) {
        visibility = "tweet-hide"
      }
      else {
        visibility = "tweet-show"
      }

      $("#default-message").remove();
      var body = "<li class='tweet-container tweet-col " + visibility + "' data-id='" + msg.tweet.id + "' data-popularity='" + msg.tweet.popularity + "'>" + msg.tweet.html + "</li>";
      if (msg.insertion_index == 0) {
        $("#tweet-collection").prepend(body)
      }
      else {
        $("#tweet-collection > li:nth-child(" + (msg.insertion_index) + ")").after(body)
      }

      if ($(".tweet-container").length == 50) {
        $(".tweet-col:last-child").remove()
      }
      twttr.widgets.load();
      $("#tweet-count").html("Currently tracking " + count + " tweets. ");
    }

    let show_more = function(event, limit) {
      event.preventDefault();
      displayable = limit;
      $("#tweet-collection > li:lt(" + (displayable) + ")").removeClass("tweet-hide").addClass("tweet-show");
      $("#tweet-collection > li:gt(" + (displayable - 1) + ")").removeClass("tweet-show").addClass("tweet-hide");
      $(".active").removeClass("active");
      $("#show-" + limit).parent().addClass("active");
    }

    var displayable = 10;
    var count = 0;

    $("#show-10").bind("click", function(event) {
      show_more(event, 10);
    });


    $("#show-25").bind("click", function(event) {
      show_more(event, 25);
    });

    $("#show-50").bind("click", function(event) {
      show_more(event, 50);
    });

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
    chan.on("new:backfill", update_list);
    chan.on("new:response", update_list);
  }

}
$( () => App.init() )
export default App
