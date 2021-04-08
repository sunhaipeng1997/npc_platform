package com.cdkhd.npc.repository.member_house;

import com.cdkhd.npc.entity.Study;
import com.cdkhd.npc.entity.StudyType;
import com.cdkhd.npc.repository.base.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.net.URL;

public interface StudyRepository extends BaseRepository<Study> {

    @Query(value = "select max(study.sequence) from Study as study where study.studyType.uid = ?1")
    Integer findMaxSequence(String type);

    @Query(value = "select study from Study as study where study.sequence < ?1 and study.studyType.uid = ?2 order by study.sequence desc ")
    Page<Study> findBySequenceDesc(Integer sequence, String type, Pageable page);

    @Query(value = "select study from Study as study where study.sequence > ?1 and study.studyType.uid = ?2 order by study.sequence asc ")
    Page<Study> findBySequenceAsc(Integer sequence, String type, Pageable page);

}
