// Copyright 2009 Google Inc.

package org.npr.api;

import android.content.Context;
import android.util.Log;

import org.apache.http.client.ClientProtocolException;
import org.npr.android.util.NodeUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public abstract class StoryGrouping extends ApiElement implements
    Comparable<StoryGrouping> {
  private static final String LOG_TAG = StoryGrouping.class.getName();
  
  private final String title;
  private final int storycounttoday;
  private final int storycountmonth;
  private final int storycountall;  
  private final String additionalInfo;

  public StoryGrouping(String id, String title, Integer storycounttoday,
      Integer storycountmonth, Integer storycountall, String additionalInfo) {
    super(id);
    this.title = title;
    this.storycounttoday = storycounttoday;
    this.storycountmonth = storycountmonth;
    this.storycountall = storycountall;
    this.additionalInfo = additionalInfo;
  }

  public String getTitle() {
    return title;
  }

  public String getAdditionalInfo() {
    return additionalInfo;
  }

  @Override
  public String toString() {
    return title;
  }

  @Override
  public int compareTo(StoryGrouping that) {
    int today, month, all;
    today = this.storycounttoday - that.storycounttoday;
    month = this.storycountmonth - that.storycountmonth;
    all = this.storycountall - that.storycountall;
    boolean equal = today == 0 && month == 0 && all == 0;
    if (today > 0) {
      return -1;
    } else {
      if (month > 0) {
        return -1;
      } else {
        if (all > 0) {
          return -1;
        }
      }
    }
    return equal ? 0 : 1;
  }

  public static class StoryGroupingBuilder<T extends StoryGrouping> {
    protected final Class<T> klass;
    protected final String id;
    protected String title;
    protected final int storycounttoday;
    protected final int storycountmonth;
    protected final int storycountall;  
    protected String additionalInfo;

    public StoryGroupingBuilder(Class<T> c, String id, int storycounttoday,
        int storycountmonth, int storycountall) {
      this.klass = c;
      this.id = id;
      this.storycounttoday = storycounttoday;
      this.storycountmonth = storycountmonth;
      this.storycountall = storycountall;
    }

    public StoryGroupingBuilder<T> withTitle(String title) {
      this.title = title;
      return this;
    }

    public StoryGroupingBuilder<T> withAdditionalInfo(String additionalInfo) {
      this.additionalInfo = additionalInfo;
      return this;
    }

    public T build() {
      try {
        Constructor<T> cons =
            klass.getConstructor(String.class, String.class, int.class,
                int.class, int.class, String.class);
        return cons.newInstance(id, title, storycounttoday, storycountmonth,
            storycountall, additionalInfo);
      } catch (Exception e) {
        Log.e(LOG_TAG, e.getMessage(), e);
      }
      return null;
    }
  }


  public static class StoryGroupingFactory<T extends StoryGrouping> {
    public StoryGroupingFactory(Class<T> klass, String groupingType) {
      this.klass = klass;
      this.groupingType = groupingType;
    }

    private final Class<T> klass;
    private final String groupingType;

    protected List<T> parseStoryGroupings(Class<T> c, Node rootNode) {
      LinkedList<T> result = new LinkedList<T>();
      TreeSet<T> resultSet = new TreeSet<T>();
      NodeList childNodes = rootNode.getChildNodes();
      for (Node node : new IterableNodeList(childNodes)) {
        T p = createStoryGrouping(c, node);
        if (p != null) {
          resultSet.add(p);
        }
      }
      for (T p : resultSet) {
        result.add(p);
      }
      return result;
    }

    @SuppressWarnings("unchecked")
    protected T createStoryGrouping(Class<T> c, Node node) {
      if (!node.getNodeName().equals("item") ||
          !node.hasChildNodes()) {
        return null;
      }
      String id;
      int storycounttoday, storycountmonth, storycountall;
      id = node.getAttributes().getNamedItem("id").getNodeValue();
      storycountall = Integer.parseInt(node.getAttributes().getNamedItem(
          "storycountall").getNodeValue());
      storycountmonth = Integer.parseInt(node.getAttributes().getNamedItem(
          "storycountmonth").getNodeValue());
      storycounttoday = Integer.parseInt(node.getAttributes().getNamedItem(
          "storycounttoday").getNodeValue());
      StoryGroupingBuilder<? extends StoryGrouping> sb =
        new StoryGroupingBuilder<T>(c, id, storycounttoday, storycountmonth,
              storycountall);
      for (Node n : new IterableNodeList(node.getChildNodes())) {
        String nodeName = n.getNodeName();
        if (nodeName.equals("title")) {
          sb.withTitle(NodeUtils.getTextContent(n));
        } else if (nodeName.equals("additionalInfo")) {
          sb.withAdditionalInfo(NodeUtils.getTextContent(n));
        }
      }
      return (T) sb.build();
    }

    public List<T> downloadStoryGroupings(int count, Context context) {
      Log.d(LOG_TAG, "downloading StoryGroupings");
      Map<String, String> params = new HashMap<String, String>();
      params.put(ApiConstants.PARAM_ID, groupingType);
      String url = ApiConstants.instance()
          .createUrl(ApiConstants.LIST_PATH, params);

      Node storyGroupings = null;
      try {
        storyGroupings = new Client(url, context).execute();
      } catch (ClientProtocolException e) {
        Log.e(LOG_TAG, "", e);
      } catch (IOException e) {
        Log.e(LOG_TAG, "", e);
      } catch (SAXException e) {
        Log.e(LOG_TAG, "", e);
      } catch (ParserConfigurationException e) {
        Log.e(LOG_TAG, "", e);
      }

      if (storyGroupings == null) {
        return new LinkedList<T>();
      }
      Log.d(LOG_TAG, "node " + storyGroupings.getNodeName() + " "
          + storyGroupings.getChildNodes().getLength());
      List<T> result = parseStoryGroupings(klass, storyGroupings);
      Log.d(LOG_TAG, "found StoryGroupings: " + result.size());
      return (count >= 0 && count < result.size()) ?
          result.subList(0, count) :
          result;
    }
  }
}
