package com.zhiwei;


import com.alibaba.fastjson.JSON;
import com.zhiwei.pojo.News;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.annotation.EnableRetry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@SpringBootTest
class CnjdzApplicationTests {

    @Test
    void contextLoads() {
    }

//    @Autowired
//    private RetryService retryService;
//    @Test
//    public void sendRetry() {
//        retryService.retry(0);
//    }

    /**
        1、使用了@Retryable的方法不能在本类被调用，不然重试机制不会生效(动态代理)，然后在其它类使用@Autowired注入或者@Bean去实例才能生效。
        2 、要触发@Recover方法，重试几次失败后 调用
        3 、非幂等情况下慎用
        4 、使用了@Retryable的方法里面不能使用try...catch包裹，要在方法上抛出异常，不然不会触发。
     *
     * @Retryable的参数说明： •value：抛出指定异常才会重试
     * •include：和value一样，默认为空，当exclude也为空时，默认所以异常
     * •exclude：指定不处理的异常
     * •maxAttempts：最大重试次数，默认3次
     * •backoff：重试等待策略，默认使用@Backoff，@Backoff的value默认为1000L，我们设置为2000L；multiplier（指定延迟倍数）默认为0，表示固定暂停1秒后进行重试，如果把multiplier设置为1.5，则第一次重试为2秒，第二次为3秒，第三次为4.5秒。
     */
    //@Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 2000L, multiplier = 1.5))

    @Test
    void testSave() throws IOException {
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

                            //newsService.saveNews(news);
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

                RestHighLevelClient restHighLevelClient = new RestHighLevelClient(
                        RestClient.builder(new HttpHost("localhost",9200,"http")));
                //批量插入数据
                BulkRequest request = new BulkRequest();
                //用crawler里写好的爬虫代码直接去爬
                //List<> contents = new CrawlerHttp().crawler();
                //把查询到的数据放到es
                BulkRequest bulkRequest = new BulkRequest();
                bulkRequest.timeout("2m");//两分钟的超时

                for (int k = 0; k < list.size(); k++) {
                    //获取列表中的值也就是爬到的数据
                    System.out.println(JSON.toJSONString(list.get(k)));
                    bulkRequest.add(new IndexRequest("cnjdz_index")
                            .source(JSON.toJSONString(list.get(k)), XContentType.JSON));

                }

                BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
                //return bulkResponse.hasFailures();

            }
            }
        }
    }

