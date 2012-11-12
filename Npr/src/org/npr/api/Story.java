// Copyright 2009 Google Inc.
// Copyright 2011 NPR
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.npr.api;

import android.content.Context;
import android.util.Log;

import org.apache.http.client.ClientProtocolException;
import org.npr.android.util.NodeUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class Story extends ApiElement {
  public static final String LOG_TAG = Story.class.getName();

  private final String link;
  private final String shortLink;
  private final String title;
  private final String subtitle;
  private final String shortTitle;
  private final String teaser;
  private final String miniTeaser;
  private final String slug;
  private final String storyDate;
  private final String pubDate;
  private final String lastModifiedDate;
  private final String keywords;
  private final String priorityKeywords;
  private final List<Byline> bylines;
  private final List<Thumbnail> thumbnails;
  private final List<Toenail> toenails;
  private final List<Organization> organizations;
  private final List<Audio> audios;
  private final Map<String, Image> images;
  private final List<RelatedLink> relatedLinks;
  private final List<PullQuote> pullQuotes;
  private final Text text;
  private final TextWithHtml textWithHtml;
  private final List<Parent> parents;
  private final Layout layout;

  public static class Thumbnail {
    private final String medium;

    public Thumbnail(String medium) {
      this.medium = medium;
    }

    public String getMedium() {
      return medium;
    }
  }

  public static class Toenail {
    @SuppressWarnings("unused")
    private final String medium;

    public Toenail(String medium) {
      this.medium = medium;
    }
  }

  public static class Organization {
    private final String id;
    private final String name;
    private final String website;

    public Organization(String id, String name, String website) {
      this.id = id;
      this.name = name;
      this.website = website;
    }

    public String getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    @SuppressWarnings("unused")
    public String getWebsite() {
      return website;
    }
  }

  public static class Audio {

    private final String id;
    private final String type;
    private final String duration;
    private final List<Format> formats;

    public static class Format {
      private final String mp3;
      private final String wm;
      private final String rm;

      public Format(String mp3, String wm, String rm) {
        this.mp3 = mp3;
        this.wm = wm;
        this.rm = rm;
      }

      public String getMp3() {
        return mp3;
      }

      @SuppressWarnings("unused")
      public String getWm() {
        return wm;
      }

      @SuppressWarnings("unused")
      public String getRm() {
        return rm;
      }
    }

    public Audio(String id, String primary, String duration, List<Format> formats) {
      this.id = id;
      this.type = primary;
      this.duration = duration;
      this.formats = formats;
    }

    public List<Format> getFormats() {
      return formats;
    }

    public String getId() {
      return id;
    }

    public String getType() {
      return type;
    }

    public String getDuration() {
      return duration;
    }
  }

  public static class Image {
    @SuppressWarnings("unused")
    private final String id;
    private final String type;
    @SuppressWarnings("unused")
    private final String width;
    private final String src;
    @SuppressWarnings("unused")
    private final String hasBorder;
    private final String caption;
    @SuppressWarnings("unused")
    private final String linkUrl;
    private final String producer;
    private final String provider;
    @SuppressWarnings("unused")
    private final String copyright;

    public Image(String id, String type, String width, String src, String hasBorder, 
                 String caption, String linkUrl, String producer, String provider,
                 String copyright) {
      this.id = id;
      this.type = type;
      this.width = width;
      this.src = src;
      this.hasBorder = hasBorder;
      this.caption = caption;
      this.linkUrl = linkUrl;
      this.producer = producer;
      this.provider = provider;
      this.copyright = copyright;
    }

    public String getSrc() {
      return src;
    }

    public String getCaption() {
      return caption;
    }
    
    public String getType() {
      return type;
    }

    public String getAttribution() {
      if (producer != null && producer.length() > 0) {
        if (provider != null && provider.length() > 0) {
          return producer + "/" + provider;
        } else {
          return producer;
        }
      } else if (provider != null && provider.length() > 0) {
        return provider;
      }
      return "";
    }
  }

  public static class RelatedLink {
    @SuppressWarnings("unused")
    private final String id;
    @SuppressWarnings("unused")
    private final String type;
    @SuppressWarnings("unused")
    private final String caption;
    @SuppressWarnings("unused")
    private final String link;

    public RelatedLink(String id, String type, String caption, String link) {
      this.id = id;
      this.type = type;
      this.caption = caption;
      this.link = link;
    }
  }

  public static class PullQuote {
    @SuppressWarnings("unused")
    private final String person;
    @SuppressWarnings("unused")
    private final String date;

    public PullQuote(String person, String date) {
      this.person = person;
      this.date = date;
    }
  }

  public static class Text {
    private final Map<Integer, String> paragraphs;

    public Text(Map<Integer, String> paragraphs) {
      this.paragraphs = paragraphs;
    }

    @SuppressWarnings("unused")
    public Map<Integer, String> getParagraphs() {
      return paragraphs;
    }
  }

  public static class TextWithHtml {
    private final Map<Integer, String> paragraphs;

    public TextWithHtml(Map<Integer, String> paragraphs) {
      this.paragraphs = paragraphs;
    }

    public Map<Integer, String> getParagraphs() {
      return paragraphs;
    }
  }

  public static class Byline {
    private final String name;
    private final String htmlLink;
    private final String apiLink;

    public Byline(String name, String htmlLink, String apiLink) {
      this.name = name;
      this.htmlLink = htmlLink;
      this.apiLink = apiLink;
    }

    public String getName() {
      return name;
    }

    @SuppressWarnings("unused")
    public String getHtmlLink() {
      return htmlLink;
    }

    @SuppressWarnings("unused")
    public String getApiLink() {
      return apiLink;
    }
  }

  public static class Parent {
    private final String id;
    private final String type;
    private final boolean isPrimary;
    private final String title;
    @SuppressWarnings("unused")
    private final String htmlLink;
    private final String apiLink;

    public Parent(String id, String type, boolean isPrimary, String title, String htmlLink,
                  String apiLink) {
      this.id = id;
      this.type = type;
      this.isPrimary = isPrimary;
      this.title = title;
      this.htmlLink = htmlLink;
      this.apiLink = apiLink;
    }

    public String getId() {
      return id;
    }

    public String getType() {
      return type;
    }

    public boolean isPrimary() {
      return isPrimary;
    }

    public String getTitle() {
      return title;
    }

    public String getApiLink() {
      return apiLink;
    }
  }
  
  public static class Layout {

    public enum Type {text, image}

    private final SortedMap<Integer, LayoutItem> items;

    public Layout(SortedMap<Integer, LayoutItem> items) {
      this.items = items;
    }

    public SortedMap<Integer, Layout.LayoutItem> getItems() {
      return items;
    }

    public static class LayoutItem {
  
      private final Type type;
      private final String itemId;
      
      public LayoutItem(Type type, String itemId) {
        this.type = type;
        this.itemId = itemId;
      }
      
      public Type getType() {
        return type;
      }
      
      public String getItemId() {
        return itemId;
      }
    }
  }

  public Story(String id, String link, String shortLink, String title, String subtitle,
               String shortTitle, String teaser, String miniTeaser, String slug,
               String storyDate, String pubDate, String lastModifiedDate, String keywords, 
               String priorityKeywords, List<Byline> bylines, List<Thumbnail> thumbnails, 
               List<Toenail> toenails, List<Organization> organizations, List<Audio> audios, 
               Map<String, Image> images, List<RelatedLink> relatedLinks, 
               List<PullQuote> pullQuotes, Text text, TextWithHtml textWithHtml, 
               List<Parent> parents, Layout layout) {
    super(id);
    this.link = link;
    this.shortLink = shortLink;
    this.title = title;
    this.subtitle = subtitle;
    this.shortTitle = shortTitle;
    this.teaser = teaser;
    this.miniTeaser = miniTeaser;
    this.slug = slug;
    this.storyDate = storyDate;
    this.pubDate = pubDate;
    this.lastModifiedDate = lastModifiedDate;
    this.keywords = keywords;
    this.priorityKeywords = priorityKeywords;
    this.bylines = bylines;
    this.thumbnails = thumbnails;
    this.toenails = toenails;
    this.organizations = organizations;
    this.audios = audios;
    this.images = images;
    this.relatedLinks = relatedLinks;
    this.pullQuotes = pullQuotes;
    this.text = text;
    this.textWithHtml = textWithHtml;
    this.parents = parents;
    this.layout = layout;
  }

  @SuppressWarnings("unused")
  public String getLink() {
    return link;
  }

  public String getShortLink() {
    return shortLink;
  }

  public String getTitle() {
    return title;
  }

  @SuppressWarnings("unused")
  public String getSubtitle() {
    return subtitle;
  }

  @SuppressWarnings("unused")
  public String getShortTitle() {
    return shortTitle;
  }

  public String getTeaser() {
    return teaser;
  }

  @SuppressWarnings("unused")
  public String getMiniTeaser() {
    return miniTeaser;
  }

  public String getDuration() {
    List<Audio> audios = getAudios();
    if (audios.size() == 0) {
      return null;
    } else {
      return audios.get(0).duration;
    }
  }

  public String getSlug() {
    return slug;
  }

  public String getStoryDate() {
    return storyDate == null ? pubDate : storyDate;
  }

  @SuppressWarnings("unused")
  public String getPubDate() {
    return pubDate;
  }

  @SuppressWarnings("unused")
  public String getLastModifiedDate() {
    return lastModifiedDate;
  }

  @SuppressWarnings("unused")
  public String getKeywords() {
    return keywords;
  }

  @SuppressWarnings("unused")
  public String getPriorityKeywords() {
    return priorityKeywords;
  }

  public List<Byline> getBylines() {
    return bylines;
  }

  public List<Thumbnail> getThumbnails() {
    return thumbnails;
  }

  @SuppressWarnings("unused")
  public List<Toenail> getToenails() {
    return toenails;
  }

  public List<Organization> getOrganizations() {
    return organizations;
  }

  public List<Audio> getAudios() {
    return audios;
  }

  public Map<String, Image> getImages() {
    return images;
  }

  @SuppressWarnings("unused")
  public List<RelatedLink> getRelatedLinks() {
    return relatedLinks;
  }

  @SuppressWarnings("unused")
  public List<PullQuote> getPullQuotes() {
    return pullQuotes;
  }

  public Text getText() {
    return text;
  }

  public TextWithHtml getTextWithHtml() {
    return textWithHtml;
  }

  @Override
  public String toString() {
    return title;
  }

  public List<Parent> getParents() {
    return parents;
  }
  
  public Layout getLayout() {
    return layout;
  }

  public static class StoryBuilder {
    private final String id;
    private String link;
    private String shortLink;
    private String title;
    private String subtitle;
    private String shortTitle;
    private String teaser;
    private String miniTeaser;
    private String slug;
    private String storyDate;
    private String pubDate;
    private String lastModifiedDate;
    private String keywords;
    private String priorityKeywords;
    private final List<Byline> bylines = new ArrayList<Byline>();
    private final List<Thumbnail> thumbnails = new ArrayList<Thumbnail>();
    private final List<Toenail> toenails = new ArrayList<Toenail>();
    private final List<Organization> organizations = new ArrayList<Organization>();
    private final List<Audio> audios = new ArrayList<Audio>();
    private final Map<String, Image> images = new HashMap<String, Image>();
    private final List<RelatedLink> relatedLinks = new ArrayList<RelatedLink>();
    private final List<PullQuote> pullQuotes = new ArrayList<PullQuote>();
    private Text text;
    private TextWithHtml textWithHtml;
    private final List<Parent> parents = new ArrayList<Parent>();
    private Layout layout;

    public StoryBuilder(String id) {
      this.id = id;
    }

    public StoryBuilder withLink(String link, String type) {
      if (type.equals("html")) {
        this.link = link;
      } else if (type.equals("api")) {
      } else if (type.equals("short")) {
        this.shortLink = link;
      }
      return this;
    }

    public StoryBuilder withTitle(String title) {
      this.title = title;
      return this;
    }

    @SuppressWarnings("unused")
    public StoryBuilder withSubtitle(String subtitle) {
      this.subtitle = subtitle;
      return this;
    }

    @SuppressWarnings("unused")
    public StoryBuilder withShortTitle(String shortTitle) {
      this.shortTitle = shortTitle;
      return this;
    }

    public StoryBuilder withTeaser(String teaser) {
      this.teaser = teaser;
      return this;
    }

    public StoryBuilder withMiniTeaser(String miniTeaser) {
      this.miniTeaser = miniTeaser;
      return this;
    }

    public StoryBuilder withSlug(String slug) {
      this.slug = slug;
      return this;
    }

    public StoryBuilder withStoryDate(String storyDate) {
      this.storyDate = storyDate;
      return this;
    }

    public StoryBuilder withPubDate(String pubDate) {
      this.pubDate = pubDate;
      return this;
    }

    @SuppressWarnings("unused")
    public StoryBuilder withLastModifiedDate(String lastModifiedDate) {
      this.lastModifiedDate = lastModifiedDate;
      return this;
    }

    @SuppressWarnings("unused")
    public StoryBuilder withKeywords(String keywords) {
      this.keywords = keywords;
      return this;
    }

    @SuppressWarnings("unused")
    public StoryBuilder withPriorityKeywords(String priorityKeywords) {
      this.priorityKeywords = priorityKeywords;
      return this;
    }

    public StoryBuilder withByline(Byline byline) {
      this.bylines.add(byline);
      return this;
    }

    @SuppressWarnings("unused")
    public StoryBuilder withThumbnail(Thumbnail thumbnail) {
      this.thumbnails.add(thumbnail);
      return this;
    }

    @SuppressWarnings("unused")
    public StoryBuilder withToenail(Toenail toenail) {
      this.toenails.add(toenail);
      return this;
    }

    public StoryBuilder withOrganization(Organization organization) {
      this.organizations.add(organization);
      return this;
    }

    public StoryBuilder withAudio(Audio audio) {
      this.audios.add(audio);
      return this;
    }

    public StoryBuilder withImage(String id, Image image) {
      this.images.put(id, image);
      return this;
    }

    @SuppressWarnings("unused")
    public StoryBuilder withRelatedLink(RelatedLink relatedLink) {
      this.relatedLinks.add(relatedLink);
      return this;
    }

    @SuppressWarnings("unused")
    public StoryBuilder withPullQuote(PullQuote pullQuote) {
      this.pullQuotes.add(pullQuote);
      return this;
    }

    public StoryBuilder withText(Text text) {
      this.text = text;
      return this;
    }

    public StoryBuilder withTextWithHtml(TextWithHtml textWithHtml) {
      this.textWithHtml = textWithHtml;
      return this;
    }

    public StoryBuilder withParent(Parent parent) {
      this.parents.add(parent);
      return this;
    }
    
    public StoryBuilder withLayout(Layout layout) {
      this.layout = layout;
      return this;
    }

    public Story build() {
      return new Story(id, link, shortLink, title, subtitle, shortTitle,
          teaser, miniTeaser, slug, storyDate, pubDate, lastModifiedDate,
          keywords, priorityKeywords, bylines, thumbnails, toenails,
          organizations, audios, images, relatedLinks, pullQuotes, text,
          textWithHtml, parents, layout);
    }
  }

  public static class StoryFactory {
    public static List<Story> parseStories(Node rootNode) {
      if (rootNode.getNodeName().equals("rss")) {
        return parseRssStoryList(rootNode);
      }

      return parseNprmlStoryList(rootNode);
    }

    private static List<Story> parseRssStoryList(Node rootNode) {
      List<Story> result = new ArrayList<Story>();

      for (Node channelNode : new IterableNodeList(rootNode.getChildNodes())) {
        if (channelNode.getNodeName().equals("channel")) {
          for (Node childNode :
              new IterableNodeList(channelNode.getChildNodes())) {
            Story story = createRssStory(childNode);
            if (story != null) {
              result.add(story);
            }
          }
        }
      }
      return result;
    }

    private static List<Story> parseNprmlStoryList(Node rootNode) {
      LinkedList<Story> result = new LinkedList<Story>();
      NodeList childNodes = rootNode.getChildNodes();
      for (Node node : new IterableNodeList(childNodes)) {
        if (node.getNodeName().equals("list")) {
          for (Node storyNode : new IterableNodeList(node.getChildNodes())) {
            Story story = createNprmlStory(storyNode);
            if (story != null) {
              result.add(story);
            }
          }
        }
      }
      return result;
    }


    private static Story createRssStory(Node node) {
      if (!node.getNodeName().equals("item") ||
          !node.hasChildNodes()) {
        return null;
      }

      StoryBuilder sb = new StoryBuilder(
          // Create an ID because podcast items don't have any
          Long.toHexString(new Date().getTime() * 1000 + (long) (Math.random()
              * 1000))
      );

      try {
        for (Node n : new IterableNodeList(node.getChildNodes())) {
          String nodeName = n.getNodeName();
          if (nodeName.equals("title")) {
            sb.withTitle(NodeUtils.getTextContent(n));
          } else if (nodeName.equals("link")) {
            sb.withLink(NodeUtils.getTextContent(n), "html");
          } else if (nodeName.equals("description")) {
            sb.withTeaser(NodeUtils.getTextContent(n));
          } else if (nodeName.equals("pubDate")) {
            sb.withPubDate(NodeUtils.getTextContent(n));
          } else if (nodeName.equals("enclosure")) {
            sb.withAudio(parsePodcastEnclosure(n));
          }
        }
      } catch (Exception e) {
        Log.e(LOG_TAG, "", e);
        return null;
      }

      return sb.build();
    }


    private static Story createNprmlStory(Node node) {
      if (!node.getNodeName().equals("story") ||
          !node.hasChildNodes()) {
        return null;
      }

      StoryBuilder sb = new StoryBuilder(node.getAttributes().getNamedItem(
          "id").getNodeValue());
      try {
        Log.d(LOG_TAG, "parsing story " + sb.id);
        for (Node n : new IterableNodeList(node.getChildNodes())) {
          String nodeName = n.getNodeName();
          Node nodeChild = n.getChildNodes().item(0);
          if (nodeChild == null) {
            continue;
          }
          if (nodeName.equals("title")) {
            sb.withTitle(NodeUtils.getTextContent(n));
          } else if (nodeName.equals("link")) {
            Attr typeAttr = (Attr) n.getAttributes().getNamedItem("type");
            sb.withLink(NodeUtils.getTextContent(n), typeAttr.getValue());
          } else if (nodeName.equals("teaser")) {
            sb.withTeaser(NodeUtils.getTextContent(n));
          } else if (nodeName.equals("miniTeaser")) {
            sb.withMiniTeaser(NodeUtils.getTextContent(n));
          } else if (nodeName.equals("slug")) {
            sb.withSlug(NodeUtils.getTextContent(n));
          } else if (nodeName.equals("storyDate")) {
            sb.withStoryDate(NodeUtils.getTextContent(n));
          } else if (nodeName.equals("pubDate")) {
            sb.withPubDate(NodeUtils.getTextContent(n));
          } else if (nodeName.equals("byline")) {
            sb.withByline(parseByline(n));
          } else if (nodeName.equals("textWithHtml")) {
            sb.withTextWithHtml(new TextWithHtml(parseParagraphs(n)));
          } else if (nodeName.equals("text")) {
            sb.withText(new Text(parseParagraphs(n)));
          } else if (nodeName.equals("audio")) {
            sb.withAudio(parseAudio(n));
          } else if (nodeName.equals("image")) {
            Image image = parseImage(n);
            sb.withImage(image.id, image);
          } else if (nodeName.equals("organization")) {
            sb.withOrganization(parseOrganization(n));
          } else if (nodeName.equals("parent")) {
            sb.withParent(parseParent(n));
          } else if (nodeName.equals("layout")) {
            sb.withLayout(new Layout(parseLayout(n)));
          }
        }
      } catch (Exception e) {
        Log.e(LOG_TAG, "", e);
        return null;
      }
      return sb.build();
    }
    
    private static SortedMap<Integer, Layout.LayoutItem> parseLayout(Node node) {

      SortedMap<Integer, Layout.LayoutItem> layout = new TreeMap<Integer, Layout.LayoutItem>();
      
      for (Node layoutChild : new IterableNodeList(node.getChildNodes())) {
        if (layoutChild.getNodeName().equals("storytext")) {
          for (Node layoutNode : new IterableNodeList(layoutChild.getChildNodes())) {
            String layoutType = layoutNode.getNodeName();
            if (layoutType.equals("text")) {
              Integer num = layout.size();
              Attr numAttr = (Attr) layoutNode.getAttributes().getNamedItem("num");
              if (numAttr != null) {
                try {
                  num = Integer.parseInt(numAttr.getValue());
                } catch (NumberFormatException e) {
                  // Leave as the last item if parse fails
                }
              }
              String paragraphNum;
              Attr paragraphNumAttr = 
                  (Attr) layoutNode.getAttributes().getNamedItem("paragraphNum");
              if (paragraphNumAttr != null) {
                paragraphNum = paragraphNumAttr.getValue();
              } else {
                paragraphNum = num.toString();
              }
              Layout.LayoutItem item = 
                  new Layout.LayoutItem(Layout.Type.text, paragraphNum);
              layout.put(num, item);
            } else if (layoutType.equals("image")) {
              Integer num = layout.size();
              Attr numAttr = (Attr) layoutNode.getAttributes().getNamedItem("num");
              if (numAttr != null) {
                try {
                  num = Integer.parseInt(numAttr.getValue());
                } catch (NumberFormatException e) {
                  // Leave as the last item if parse fails
                }
              }
              Attr refIdAttr = (Attr) layoutNode.getAttributes().getNamedItem("refId");
              if (refIdAttr != null) {
                Layout.LayoutItem item = 
                    new Layout.LayoutItem(Layout.Type.image, refIdAttr.getValue());
                layout.put(num, item);
              }
            }
          }
        }
      }
      return layout;
    }
    
    private static Parent parseParent(Node node) {
      String id = null, type = null, title = null, apiLink = null;
      boolean isPrimary = false;
      Attr idAttr = (Attr) node.getAttributes().getNamedItem("id");
      if (idAttr != null) {
        id = idAttr.getValue();
      }
      Attr typeAttr = (Attr) node.getAttributes().getNamedItem("type");
      if (typeAttr != null) {
        type = typeAttr.getValue();
        if (type.equals("primaryTopic")) {
          isPrimary = true;
        }
      }

      for (Node n : new IterableNodeList(node.getChildNodes())) {
        String nodeName = n.getNodeName();
        if (nodeName.equals("title")) {
          title = NodeUtils.getTextContent(n);
        } else if (nodeName.equals("link")) {
          Attr childTypeAttr = (Attr) n.getAttributes().getNamedItem("type");
          if (childTypeAttr != null && childTypeAttr.getValue().equals("api")) {
            apiLink = NodeUtils.getTextContent(n);
          }
        }
      }
      return new Parent(id, type, isPrimary, title, null, apiLink);
    }

    private static Organization parseOrganization(Node node) {
      String id = null, name = null, website = null;
      Attr idAttr = (Attr) node.getAttributes().getNamedItem("id");
      if (idAttr != null) {
        id = idAttr.getValue();
      }
      for (Node n : new IterableNodeList(node.getChildNodes())) {
        String nodeName = n.getNodeName();
        if (nodeName.equals("name")) {
          name = NodeUtils.getTextContent(n);
        } else if (nodeName.equals("website")) {
          website = NodeUtils.getTextContent(n);
        }
      }
      return new Organization(id, name, website);
    }

    private static Byline parseByline(Node node) {
      String name = null, htmlLink = null, apiLink = null;
      for (Node n : new IterableNodeList(node.getChildNodes())) {
        String nodeName = n.getNodeName();
        if (nodeName.equals("name")) {
          name = NodeUtils.getTextContent(n);
        } else if (nodeName.equals("link")) {
          Attr typeAttr = (Attr) n.getAttributes().getNamedItem("type");
          if (typeAttr != null) {
            String type = typeAttr.getValue();
            if (type.equals("api")) {
              apiLink = NodeUtils.getTextContent(n);
            } else if (type.equals("html")) {
              htmlLink = NodeUtils.getTextContent(n);
            }
          }
        }
      }
      return new Byline(name, htmlLink, apiLink);
    }

    private static Image parseImage(Node node) {
      // Attributes
      String id = null, type = null, width = null, src = null, hasBorder = null;
      // Sub-elements
      String caption = null, linkUrl = null, producer = null, provider = null, copyright = null;
      Attr idAttr = (Attr) node.getAttributes().getNamedItem("id");
      if (idAttr != null) {
        id = idAttr.getValue();
      }
      Attr typeAttr = (Attr) node.getAttributes().getNamedItem("type");
      if (typeAttr != null) {
        type = typeAttr.getValue();
      }

      for (Node n : new IterableNodeList(node.getChildNodes())) {
        if (n.getNodeName().equals("crop")) {
          Attr cropTypeAttr = (Attr) n.getAttributes().getNamedItem("type");
          if (cropTypeAttr != null && cropTypeAttr.getValue().equals("square")) {
            Attr srcAttr = (Attr) n.getAttributes().getNamedItem("src");
            if (srcAttr != null) {
              src = srcAttr.getValue();
              break;
            }
          }
        } else if (n.getNodeName().equals("caption")) {
          caption = NodeUtils.getTextContent(n);
        } else if (n.getNodeName().equals("producer")) {
          producer = NodeUtils.getTextContent(n);
        } else if (n.getNodeName().equals("provider")) {
          provider = NodeUtils.getTextContent(n);
        }
      }

      if (src == null) {
        Attr srcAttr = (Attr) node.getAttributes().getNamedItem("src");
        if (srcAttr != null) {
          src = srcAttr.getValue();
        }
      }

      try {
        src = src.replaceAll("&s=[0-9]+", "");
        src = src.concat("&s=13");
      } catch (NullPointerException e) {
        Log.e(LOG_TAG, "Error replacing size in story image parsing");
      }

      return new Image(id, type, width, src, hasBorder, caption, linkUrl, producer,
          provider, copyright);
    }

    private static Map<Integer, String> parseParagraphs(Node node) {
      // Presumably, the paragraphs will be in order. However, in the rare case
      // that they are not, we can order them according to the number attribute.
      SortedMap<Integer, String> paragraphMap = new TreeMap<Integer, String>();
      for (Node n : new IterableNodeList(node.getChildNodes())) {
        String nodeName = n.getNodeName();
        Node nodeChild = n.getChildNodes().item(0);
        if (nodeName.equals("paragraph")) {
          String paragraph = nodeChild == null ? "" : NodeUtils.getTextContent(n);
          Attr numAttr = (Attr) n.getAttributes().getNamedItem("num");
          if (numAttr != null) {
            int num = Integer.parseInt(numAttr.getValue());
            paragraphMap.put(num, paragraph);
          }
        }
      }
      return paragraphMap;
    }

    private static Audio parseAudio(Node node) {
      String id = null, primary = null, duration = null;
      List<Audio.Format> formats = new ArrayList<Audio.Format>();
      Attr typeAttr = (Attr) node.getAttributes().getNamedItem("type");
      if (typeAttr != null) {
        primary = typeAttr.getValue();
      }
      for (Node n : new IterableNodeList(node.getChildNodes())) {
        String nodeName = n.getNodeName();
        if (nodeName.equals("duration")) {
          duration = NodeUtils.getTextContent(n);
        } else if (nodeName.equals("format")) {
          formats.add(parseFormat(n));
        }
      }
      return new Audio(id, primary, duration, formats);
    }

    private static Audio parsePodcastEnclosure(Node node) {
      String url = null, duration = null;
      List<Audio.Format> formats = new ArrayList<Audio.Format>();
      Attr urlAttr = (Attr) node.getAttributes().getNamedItem("url");
      if (urlAttr != null) {
        url = urlAttr.getValue();
      }
      Attr durationAttr = (Attr) node.getAttributes().getNamedItem("duration");
      if (durationAttr != null) {
        duration = durationAttr.getValue();
      }
      Attr typeAttr = (Attr) node.getAttributes().getNamedItem("type");
      if (typeAttr != null && url != null) {
        if (typeAttr.getValue().equals("audio/mpeg")) {
          formats.add(new Audio.Format(url, null, null));
        }
      }
      return new Audio(null, "primary", duration, formats);
    }

    private static Audio.Format parseFormat(Node node) {
      String mp3 = null, wm = null, rm = null;

      for (Node n : new IterableNodeList(node.getChildNodes())) {
        String nodeName = n.getNodeName();
        if (nodeName.equals("mp3")) {
          mp3 = NodeUtils.getTextContent(n);
        } else if (nodeName.equals("wm")) {
          wm = NodeUtils.getTextContent(n);
        } else if (nodeName.equals("rm")) {
          rm = NodeUtils.getTextContent(n);
        }
      }
      return new Audio.Format(mp3, wm, rm);
    }

    public static Story downloadStory(String storyId, Context context) {
      Log.d(LOG_TAG, "downloading story: " + storyId);
      Map<String, String> params = new HashMap<String, String>();
      params.put(ApiConstants.PARAM_ID, storyId);
      String url =
          ApiConstants.instance().createUrl(ApiConstants.STORY_PATH, params);

      Node stories = null;
      try {
        stories = new Client(url, context).execute();
      } catch (ClientProtocolException e) {
        Log.e(LOG_TAG, "", e);
      } catch (IOException e) {
        Log.e(LOG_TAG, "", e);
      } catch (SAXException e) {
        Log.e(LOG_TAG, "", e);
      } catch (ParserConfigurationException e) {
        Log.e(LOG_TAG, "", e);
      }

      if (stories == null) {
        return null;
      }
      Log.d(LOG_TAG, "node " + stories.getNodeName() + " "
          + stories.getChildNodes().getLength());
      List<Story> result = parseStories(stories);
      return result.size() > 0 ? result.get(0) : null;
    }
  }

  public Audio getPlayable() {
    for (Audio a : getAudios()) {
      if (a.getType().equals("primary")) {
        return a;
      }
    }
    return null;
  }

  public String getPlayableUrl() {
    String url = null;
    Story.Audio a = getPlayable();
    if (a != null) {
      for (Story.Audio.Format f : a.getFormats()) {
        if ((url = f.getMp3()) != null) {
          break;
        }
      }
    }
    return url;
  }
}