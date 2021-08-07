package com.zhiwei.crawler;

import com.alibaba.fastjson.JSON;
import com.zhiwei.pojo.News;
import com.zhiwei.service.NewsService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author 张乙凡
 * @version 1.0
 * @date 2021/8/6
 * Desc 持续爬虫：定时器+自动重试
 *
 * springboot定时任务
 * 1、fixedDelay控制方法执行的间隔时间，是以上一次方法执行完开始算起，如上一次方法执行阻塞住了，那么直到上一次执行完，并间隔给定的时间后，执行下一次。
 * 2、fixedRate是按照一定的速率执行，是从上一次方法执行开始的时间算起，如果上一次方法阻塞住了，下一次也是不会执行，
 * 但是在阻塞这段时间内累计应该执行的次数，当不再阻塞时，一下子把这些全部执行掉，而后再按照固定速率继续执行。
 * 3、cron表达式可以定制化执行任务，但是执行的方式是与fixedDelay相近的，也是会按照上一次方法结束时间开始算起。
 * 4、initialDelay如：@Scheduled(initialDelay = 10000,fixedRate = 15000,
 * 这个定时器就是在上一个的基础上加了一个initialDelay = 10000。意思就是在容器启动后,延迟10秒后再执行一次定时器,以后每15秒再执行一次该定时器。
 *
 *
 */

@Component//表明一个类会作为组件类，并告知Spring要为这个类创建bean，@Component
//（@Controller、@Service、@Repository）通常是通过类路径扫描来自动侦测以及自动装配到Spring容器中
public class Cnjdz {

    @Autowired
    private NewsService newsService;


    //@Scheduled(initialDelay = 1000, fixedDelay = 10000)//启动1s后,每隔10s执行一次
    @Scheduled(initialDelay = 1000,fixedDelay = 1000 * 60 * 60 * 24)//启动1s后,每隔24小时执行一次
    //@Scheduled(cron = "0 0 9 * * ?")//每天9点定时执行
    //@Scheduled(cron = "0 0 9 ? * 2,3,4,5,6")//每个工作日9点定时执行
    //cron后面跟的是corntab表达式,语法不用去记,了解即可
    //秒 分 小时 日期 月份 星期 年份

    @Retryable(value= {Exception.class},maxAttempts = 3)//重试
    //public List<News> crawler() throws IOException{
    public void crawler() throws IOException {
        ArrayList list = new ArrayList();
        //Jsoup获得链接
        Document doc = Jsoup.connect("http://www.cnjdz.net/?b=0").timeout(30000).get();

        //从首页选择要爬取的新闻频道的链接
        //document.querySelector("#nav > div > ul > li:nth-child(2) > a")
        Elements elements = doc.select("#nav > div > ul > li > a");
        if (!elements.isEmpty() && Objects.nonNull(elements)) {
            for (Element element : elements) {
                //频道名
                String Channel = element.text();
                System.out.println("频道名字是：" + Channel);

                //link
                String link = element.attr("href");
                System.out.println("链接是：->" + link);


                //循环获得已知所有分页的链接
                Boolean f = true;
                String nextUrl = link;
                int i = 1;

                //获得url的前边部分，用来判断url是否需要拼接
                //String linkstr = nextUrl.substring(0,21);

                while (f) {

                    News news = new News();

                    //选择分页中的数据
                    Document doc1 = Jsoup.connect(nextUrl)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36")
                            .timeout(30000).get();
                    //document.querySelector("body > div > div.list > div.listleft > ul > div:nth-child(1) > div:nth-child(1) > li > a")
                    Elements elements1 = doc1.select("body > div > div > div > ul > div > div > li");
                    if (!elements1.isEmpty()) {
                        System.out.println("每页的信息数量" + elements.size());
                        for (Element element1 : elements1) {
                            //频道
                            System.out.println("频道名字是：" + Channel);


                            //标题
                            String title = element1.select("a").text();
                            System.out.println("title是：->" + title);

                            //时间
                            String time = element1.select("span").text();
                            System.out.println("time 是：->" + time);

                            //链接
                            String url = element1.select("a").attr("href");
                            System.out.println("url  是：->" + url);

                            news.setName(Channel);
                            news.setTitle(title);
                            news.setTime(time);
                            news.setURL(url);

                            list.add(news);

                            newsService.saveNews(news);
                        }
                    }

                    //为了防止在下一页为空的时候一直空循环重复无用功，设置最后一页
                    if (doc1.select("a.a1").isEmpty()) {
                        f = false;
                        System.out.println("没有下一页，结束循环，当前的页码为：" + i);
                    }

                    //判断下一页是正常的url还是不正常的url
                    // 需要拼接url
                    else {
                        //
                        String nextPageUrl = "http://www.cnjdz.net" + doc1.select("div.page-ctrl>a").last().attr("href");
                        if (nextUrl.equals(nextPageUrl)) {
                            f = false;
                        } else if (nextPageUrl.substring(0, 24).equals("http://www.cnjdz.nethttp")) {
                            //不拼接
                            System.out.println("此频道的url链接为完整的链接，不需要拼接");
                            nextPageUrl = doc1.select("div.page-ctrl>a").last().attr("href");
                            if (nextUrl.equals(nextPageUrl)) {
                                f = false;
                            } else {
                                nextUrl = nextPageUrl;
                            }
                        } else {
                            nextUrl = nextPageUrl;
                        }
                    }
                    //此段代码放在下一个代码块中速度会显著提升，但线程休眠就是爬完一个频道休眠一次
                    i++;
                    System.out.println("下一页链接是->->->->->->->->->-> " + nextUrl);

                }//休眠，避免ip被封
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(list);

                try (RestHighLevelClient restHighLevelClient = new RestHighLevelClient(
                        RestClient.builder(new HttpHost("localhost", 9200, "http")))) {
                    //List<News> lastList =
                    //批量插入数据
                    BulkRequest bulkRequest = new BulkRequest();
                    bulkRequest.timeout("10m");
                    //用crawler里写好的爬虫代码直接去爬
                    //List<> contents = new CrawlerHttp().crawler();

                    //起码要判断下集合里面有没有数据吧，在这里也卡了快一个小时，最主要的是添加es的代码块放哪里的问题，不能想当然的放，要明确代码执行流程
                    if (list != null && list.size() > 0) {
                        for (int k = 0; k < list.size(); k++) {
                            //获取列表中的值也就是爬到的数据
                            //System.out.println(JSON.toJSONString(list.get(k)));

                            bulkRequest.add(new IndexRequest("cnjdz_index")
                                    .source(JSON.toJSONString(list.get(k)), XContentType.JSON));
                        }
                        //
                        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
                    }
                }

            }
        }
    }
}
