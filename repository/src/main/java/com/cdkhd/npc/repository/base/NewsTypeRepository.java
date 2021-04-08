package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.NewsType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NewsTypeRepository extends BaseRepository<NewsType> {
    List<NewsType> findByAreaAndTown(Integer areaCode,String townName);

    @Query(value = "select type from NewsType as type where type.sequence < ?1 order by type.sequence desc ")
    Page<NewsType> findBySequenceDesc(Integer sequence, Pageable page);

    @Query(value = "select type from NewsType as type where type.sequence > ?1 order by type.sequence asc ")
    Page<NewsType> findBySequenceAsc(Integer sequence, Pageable page);

    @Query(value = "select max(type.sequence) from NewsType type")
    Integer findMaxSequence();
}
