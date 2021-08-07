package com.zhiwei.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author 张乙凡
 * @version 1.0
 * @date 2021/8/6
 * 新闻实体类
 * 将网站的所有头部频道数据爬下来，然后让它可以自动的去找频道爬取，入库时采用url作为主键。
 * 采用URl作为主键
 *
 */

public class News {
    @Field("news")
    @Id//主键标识
    private String URL;//链接
    private String title;//标题
    private String time;//发布时间
    private String name;//name字段用来标识数据为哪一个频道的

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "News{" +
                "URL='" + URL + '\'' +
                ", title='" + title + '\'' +
                ", time='" + time + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
