package com.cinread.ebook.browser;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

import java.util.Date;


public class Bookmark extends DataSupport {
  @Column(unique = true)
  private long id;

  @Column(unique = true, defaultValue = "unknown")
  private String name;// 书名作为唯一标识

  private String showName;

  private int begin;

  private int end;

  private String path;

  private Date createDate;

  private Date modifyDate;

  private float percent;

  public float getPercent() {
    return percent;
  }

  public void setPercent(float percent) {
    this.percent = percent;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public int getBegin() {
    return begin;
  }

  public void setBegin(int begin) {
    this.begin = begin;
  }

  public Date getCreateDate() {
    return createDate;
  }

  public void setCreateDate(Date createDate) {
    this.createDate = createDate;
  }

  public int getEnd() {
    return end;
  }

  public void setEnd(int end) {
    this.end = end;
  }

  public Date getModifyDate() {
    return modifyDate;
  }

  public void setModifyDate(Date modifyDate) {
    this.modifyDate = modifyDate;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getShowName() {
    return showName;
  }

  public void setShowName(String showName) {
    this.showName = showName;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public static String createShowName(String name) {
    String temp = name;
    if (temp.endsWith(".txt")) {
      temp = temp.substring(0, temp.length() - 4);
    }

    if(!temp.startsWith("《")) {
      temp = "《" + temp;
    }

    if(!temp.endsWith("》")) {
      temp = temp + "》";
    }

    return temp;
  }

}
