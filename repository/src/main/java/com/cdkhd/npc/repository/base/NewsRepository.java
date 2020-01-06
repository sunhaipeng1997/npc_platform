package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.News;
import com.cdkhd.npc.entity.NewsType;

import java.util.List;

public interface NewsRepository extends BaseRepository<News>{
    int countByNewsType(NewsType newsType);
}