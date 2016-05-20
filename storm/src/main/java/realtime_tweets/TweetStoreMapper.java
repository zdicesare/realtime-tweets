package realtime_tweets;

import org.apache.storm.redis.common.mapper.RedisStoreMapper;
import org.apache.storm.redis.common.mapper.RedisDataTypeDescription;
import backtype.storm.tuple.ITuple;

import java.util.HashMap;

public class TweetStoreMapper implements RedisStoreMapper {
  private RedisDataTypeDescription description;
  private final String hashKey = "roosevelt";

  public TweetStoreMapper() {
    description = new RedisDataTypeDescription(RedisDataTypeDescription.RedisDataType.HASH, hashKey);
  }

  @Override
  public RedisDataTypeDescription getDataTypeDescription() {
    return description;
  }

  @Override
  public String getKeyFromTuple(ITuple tuple) {
    return tuple.getValue(0).toString();
  }

  @Override
  public String getValueFromTuple(ITuple tuple) {
    HashMap<String, String> status = (HashMap<String, String>) tuple.getValue(1);
    return status.toString();
  }
}