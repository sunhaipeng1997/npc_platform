package com.cdkhd.npc.repository.member_house;

import com.cdkhd.npc.entity.SuggestionBusiness;
import org.apache.poi.ss.formula.functions.T;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@RunWith(SpringRunner.class)
class SuggestionBusinessRepositoryTest {

    @Autowired
    SuggestionBusinessRepository suggestionBusinessRepository;

    @org.junit.jupiter.api.Test
    void findBySequenceAndLevelAreaUidDesc() {
    }

    @org.junit.jupiter.api.Test
    void findBySequenceAndLevelTownUidDesc() {
    }

    @org.junit.jupiter.api.Test
    void findBySequenceAndLevelAreaUidAsc() {
    }

    @org.junit.jupiter.api.Test
    void findBySequenceAndLevelTownUidAsc() {
    }

    @org.junit.jupiter.api.Test
    void findMaxSequence() {
    }

    @org.junit.jupiter.api.Test
    void findByLevelAndTownUidAndStatusAndIsDelFalseOrderBySequenceAsc() {
    }

    @org.junit.jupiter.api.Test
    void findByLevelAndAreaUidAndStatusAndIsDelFalseOrderBySequenceAsc() {
    }

    @org.junit.jupiter.api.Test
    void findByNameAndLevelAndTownUidAndIsDelFalse() {
    }

    @org.junit.jupiter.api.Test
    void findByNameAndLevelAndAreaUidAndIsDelFalse() {
    }

    @org.junit.jupiter.api.Test
    void findByStatus() {
        Set<SuggestionBusiness> list = suggestionBusinessRepository.findByStatus((byte)1);
        for(SuggestionBusiness suggestionBusiness : list){
            System.out.println(suggestionBusiness.getName());

        }
        list.forEach(item -> System.out.println(item.getName()));
//        list.forEach(item->{
//            System.out.println(item.toString());
//        });
    }

    @org.junit.jupiter.api.Test
    void findByAreaUidAndLevel() {
    }

    @Test
    public void test(){

    }
}