package realtime_tweets;

import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import backtype.storm.topology.OutputFieldsDeclarer;

import java.util.Map;
import java.util.HashMap;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;

public class TweetParserBolt extends BaseRichBolt {
  OutputCollector _collector;
  HashMap<String, String> stripped_status;

  @Override
  public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
    _collector = collector;
  }

  @Override
  public void execute(Tuple tuple) {
    Utils.sleep(500);
    stripped_status = new HashMap<String, String>();
    Status status = (Status) tuple.getValue(0);
    if(!status.isRetweet()) {
      try {
        ConfigurationBuilder cb = new ConfigurationBuilder()
          .setOAuthConsumerKey("")
          .setOAuthConsumerSecret("")
          .setOAuthAccessToken("")
          .setOAuthAccessTokenSecret("");
        Twitter twitter = new TwitterFactory(cb.build()).getInstance();


        OEmbedRequest request = new OEmbedRequest(status.getId(), "");
        request.HideMedia(true);
        request.HideThread(true);
        request.omitScript(true);
        request.MaxWidth(550);
        stripped_status.put("html", twitter.getOEmbed(request).getHtml());
        stripped_status.put("id", String.valueOf(status.getId()));
        //LocalDateTime start = LocalDateTime.ofInstant(Instant.ofEpochMilli(status.getCreatedAt().getTime()), ZoneId.systemDefault());
        //LocalDateTime end = LocalDateTime.now();
        //LocalDateTime end = (LocalDateTime) tuple.getValue(1);
        //Duration d;
        //d = Duration.between(start, end);
        //System.out.println(d.getSeconds());
        stripped_status.put("popularity", String.valueOf(status.getRetweetCount() + status.getFavoriteCount()));
        _collector.emit(tuple, new Values(status.getId(), stripped_status));

      } catch (TwitterException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      finally {
        _collector.ack(tuple);
      }
     }
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declare(new Fields("id", "stripped_tweet"));
  }

}