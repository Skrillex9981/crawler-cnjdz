package com.zhiwei.dao;


import com.zhiwei.pojo.News;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NewsRepository extends MongoRepository<News,String> {

}
