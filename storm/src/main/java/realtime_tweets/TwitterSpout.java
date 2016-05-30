package realtime_tweets;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.concurrent.LinkedBlockingQueue;
import java.time.LocalDateTime;
import java.util.Map;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.task.TopologyContext;
import backtype.storm.utils.Utils;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

public class TwitterSpout extends BaseRichSpout {
	/**
	 * Connects to Twitter's streaming API, with a set of keywords to filter by
	 */
  SpoutOutputCollector _collector;
  LinkedBlockingQueue<Status> queue = null;
  TwitterStream _twitterStream;
  String[] _topics;
  public TwitterSpout(String[] topics) {
    _topics = topics;
  }

  @Override
  public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
    queue = new LinkedBlockingQueue<Status>(1000);
    _collector = collector;
    ConfigurationBuilder twitter_conf = new ConfigurationBuilder();
    twitter_conf.setDebugEnabled(true);
    _twitterStream = new TwitterStreamFactory(twitter_conf.build()).getInstance();
    StatusListener listener = new StatusListener() {
      @Override
      public void onStatus(Status status) {
        queue.offer(status);
      }

      @Override
      public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        System.out.println("Deletion notice");
      }

      @Override
      public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
        System.out.println("Limited notice");
      }

      @Override
      public void onScrubGeo(long userId, long upToStatusId) {
        System.out.println("Scrub geo");
      }

      @Override
      public void onStallWarning(StallWarning warning) {
        System.out.println("Got a warning");
      }

      @Override
      public void onException(Exception ex) {
        System.out.println("Got an exception");
      }
    };

    _twitterStream.addListener(listener);
    if(_topics.length > 0) {
      _twitterStream.filter(_topics);
    }
    else {
      _twitterStream.sample();
    }
  }

  @Override
  public void nextTuple() {
    Status result = queue.poll();
    if (result == null) {
      Utils.sleep(50);
    } else {
      _collector.emit(new Values(result, LocalDateTime.now()));
    }
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declare(new Fields("tweet", "arrival_time"));
  }

  @Override
  public void close() {
    _twitterStream.shutdown();
  }
}