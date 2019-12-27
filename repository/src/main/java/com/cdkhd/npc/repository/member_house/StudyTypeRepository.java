package com.cdkhd.npc.repository.member_house;

import com.cdkhd.npc.entity.StudyType;
import com.cdkhd.npc.repository.base.BaseRepository;

import java.util.List;

public interface StudyTypeRepository extends BaseRepository<StudyType> {

    List<StudyType> findByStatusAndLevelAndTownUidOrderBySequenceAsc(Byte status, Byte level, String town);

    List<StudyType> findByStatusAndLevelAndAreaUidOrderBySequenceAsc(Byte status, Byte level, String area);

}
