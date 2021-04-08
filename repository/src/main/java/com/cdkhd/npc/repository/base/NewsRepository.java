package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.News;
import com.cdkhd.npc.entity.NewsType;


public interface NewsRepository extends BaseRepository<News>{
    int countByNewsType(NewsType newsType);
    News findByTitle(String title);
}
