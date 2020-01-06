package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.NewsType;

import java.util.List;

public interface NewsTypeRepository extends BaseRepository<NewsType> {
    List<NewsType> findByAreaAndTown(Integer areaCode,String townName);
}
