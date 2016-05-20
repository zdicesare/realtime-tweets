package realtime_tweets;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import org.apache.storm.redis.common.config.JedisPoolConfig;

public class TwitterTopology
{

  public static void main(String[] args) throws Exception {
    TopologyBuilder builder = new TopologyBuilder();
    JedisPoolConfig poolConfig = new JedisPoolConfig.Builder().setHost("127.0.0.1").setPort(6379).build();
    MultiRedisBolt storeBolt = new MultiRedisBolt(poolConfig);
    builder.setSpout("twitter_stream", new TwitterSpout(args), 1);
    builder.setSpout("redis_spout", new RedisSpout(), 1);
    builder.setBolt("parser", new TweetParserBolt(), 1).shuffleGrouping("twitter_stream");
    builder.setBolt("popularity_bolt", new PopularityBolt(), 1).shuffleGrouping("redis_spout");
    builder.setBolt("redis_bolt", storeBolt, 1).shuffleGrouping("parser").shuffleGrouping("popularity_bolt");
    Config conf = new Config();
   // conf.setDebug(true);

    LocalCluster cluster = new LocalCluster();
    cluster.submitTopology("test", conf, builder.createTopology());
    //Utils.sleep(20000);
    //cluster.killTopology("test");
    //cluster.shutdown();
  }
}