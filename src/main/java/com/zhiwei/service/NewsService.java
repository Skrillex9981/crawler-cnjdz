package com.zhiwei.service;


import com.zhiwei.dao.NewsRepository;
import com.zhiwei.pojo.News;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Retryable
@Service
public class NewsService {

    @Autowired
    private NewsRepository newsRepository;


    /**
     * 保存新闻
     * @
     */
    public void saveNews(News news){
        newsRepository.save(news);
    }
}
