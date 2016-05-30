package realtime_tweets;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;

public class RedisSpout extends BaseRichSpout {
  SpoutOutputCollector _collector;
  JedisPool pool;
  LinkedBlockingQueue<String> queue = null;

  public RedisSpout() {
  }

  class ListenerThread extends Thread {
    LinkedBlockingQueue<String> queue;
    JedisPool pool;
    String pattern;

    public ListenerThread(LinkedBlockingQueue<String> queue, JedisPool pool, String pattern) {
      this.queue = queue;
      this.pool = pool;
      this.pattern = pattern;
    }

    public void run() {

      JedisPubSub listener = new JedisPubSub() {

        @Override
        public void onMessage(String channel, String message) {
          queue.offer(message);
        }

        @Override
        public void onPMessage(String pattern, String channel, String message) {
          queue.offer(message);
        }

        @Override
        public void onPSubscribe(String channel, int subscribedChannels) {
          // TODO Auto-generated method stub

        }

        @Override
        public void onPUnsubscribe(String channel, int subscribedChannels) {
          // TODO Auto-generated method stub

        }

        @Override
        public void onSubscribe(String channel, int subscribedChannels) {
          // TODO Auto-generated method stub

        }

        @Override
        public void onUnsubscribe(String channel, int subscribedChannels) {
          // TODO Auto-generated method stub

        }
      };

      Jedis jedis = pool.getResource();
      try {
        jedis.subscribe(listener, "storm_tweet_batch");
      } finally {
        pool.returnResource(jedis);
      }
    }
  };

  @Override
  public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
    _collector = collector;
    queue = new LinkedBlockingQueue<String>(1000);
    pool = new JedisPool(new JedisPoolConfig(), "127.0.0.1", 6379);

    ListenerThread listener = new ListenerThread(queue, pool, "");
    listener.start();
  }

  @Override
  public void nextTuple() {
    String tweet_list = queue.poll();
    if (tweet_list == null) {
      Utils.sleep(50);
    }
    else {
       Utils.sleep(30000);
      _collector.emit(new Values(tweet_list));
    }
  }

  @Override
  public void close() {
    pool.destroy();
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declare(new Fields("tweet_list"));
  }
}
