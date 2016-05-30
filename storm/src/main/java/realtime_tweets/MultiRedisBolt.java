package realtime_tweets;

import org.apache.storm.redis.bolt.AbstractRedisBolt;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

public class MultiRedisBolt extends AbstractRedisBolt {
  /**
   * Handles the sending of a processed tweet to Redis, as well as grouping tweets into 100s to send
   * on a separate channel for bulk updating. Those batches are picked up by the RedisSpout.
   */

  int count;
  HashMap<String, HashMap<String, String>> tweet_list;

  public MultiRedisBolt(JedisPoolConfig config) {
    super(config);
    count = 0;
    tweet_list = new HashMap<String, HashMap<String, String>>();
  }

  @Override
  public void execute(Tuple tuple) {
    JedisCommands jedisCommand = null;
    count += 1;
    HashMap<String, String> tweet = (HashMap<String, String>) tuple.getValue(1);
    tweet_list.put(String.valueOf(tuple.getValue(0)), tweet);

    try {
      jedisCommand = getInstance();
      Jedis jedis = (Jedis) jedisCommand;
     jedis.publish("storm_tweet_feed", new ObjectMapper().writeValueAsString(tuple.getValue(1)));

     // TODO: Naive batching of tweets is done here, and then processed by RedisSpout. We publish batches of 100 tweets on a separate channel, to
     // take full advantage of the bulk lookup in Twitter's API, and to minimize our requests. However, we don't really distinguish between tweet's
     // that have been rechecked multiple times versus tweets that we have never checked again. As the pool of tweets grows, the flow of new tweets
     // slows and we spend more time checking the same tweets repeatedly. The batching logic should be improved, probably by taking better advantage of
     // Storm batching facilities
      if (count == 99) {
        jedis.publish("storm_tweet_batch", new ObjectMapper().writeValueAsString(tweet_list));
        count = 0;
        tweet_list = new HashMap<String, HashMap<String, String>>();
      }
      this.collector.ack(tuple);
    } catch (Exception e) {
      this.collector.reportError(e);
      this.collector.fail(tuple);
    }
    finally {
      returnInstance(jedisCommand);
    }

  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    // TODO Auto-generated method stub

  }
}
