package realtime_tweets;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

public class PopularityBolt extends BaseRichBolt {
	/**
	 * Receives a batch of 100 JSONified tweets from RedisSpout, deserializes them, and then performs a bulk lookup via Twitter's
	 * API. It emits 1 tuple for each tweet, containing an updated popularity value that is computed from the number of retweets,
	 * favorites, and the age of the tweet. These are then picked up again by the MultiRedisBolt.
	 */
  OutputCollector _collector;

  @Override
  public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
    _collector = collector;
  }

  @Override
  public void execute(Tuple tuple) {

    try {
      Map<String, Map<String, String>> objects = new ObjectMapper().readValue((String) tuple.getValue(0), new TypeReference<Map<String, Map<String, String>>>(){});
      Set<String> keys = objects.keySet();
      Twitter twitter = new TwitterFactory().getInstance();
      long[] longs = keys.stream().mapToLong(i -> Long.valueOf(i)).toArray();
      ResponseList<Status> responses = twitter.lookup(longs);
      LocalDateTime end = LocalDateTime.now();

      for(Status status: responses) {
        HashMap<String, String> tweet = (HashMap<String, String>) objects.get(String.valueOf(status.getId()));
        LocalDateTime start = LocalDateTime.ofInstant(Instant.ofEpochMilli(status.getCreatedAt().getTime()), ZoneId.systemDefault());
        Duration d;
        d = Duration.between(start, end);

        double f = (((double) status.getRetweetCount() + (double) status.getFavoriteCount()) / d.getSeconds()) * 100;
        tweet.put("popularity",  String.valueOf(f));
        _collector.emit(tuple, new Values(status.getId(), tweet));
      }
      _collector.ack(tuple);
    }
    catch(Exception ex) {
      System.out.println("Problem deserializing JSON");
      ex.printStackTrace();
    }
    }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declare(new Fields("id", "stripped_tweet"));
  }
}
