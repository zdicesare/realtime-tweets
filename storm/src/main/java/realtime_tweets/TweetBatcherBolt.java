package realtime_tweets;

import java.util.HashMap;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

public class TweetBatcherBolt extends BaseRichBolt {
  OutputCollector _collector;
  int count;
  HashMap<String, HashMap<String, String>> tweet_list;

  @Override
  public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
    _collector = collector;
    count = 0;
    tweet_list = new HashMap<String, HashMap<String, String>>();
  }

  @Override
  public void execute(Tuple tuple) {
    count += 1;

  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declare(new Fields("id", "stripped_tweet", "count", "tweet_list"));
  }
}
