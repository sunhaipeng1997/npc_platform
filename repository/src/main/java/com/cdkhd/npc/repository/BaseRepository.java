package com.cdkhd.npc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//使用NoRepositoryBean注解的意义：由于Spring Data JPA会自动为每个继承了JpaRepository接口的接口创建实例，
//所以继承了JpaRepository的接口上不需要添加@Repository注解。此外，在这里BaseRepository只是作为各个具体
//实体类Repository的父类接口，所以不需要创建实例，故使用NoRepositoryBean注解告诉Spring Data JPA不要为该
//接口创建实例。
@NoRepositoryBean
public interface BaseRepository<T> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {

    T findByUid(String uid);

    @Transactional
    int deleteByUid(String uid);

    @Transactional
    List<T> removeByUid(String uid);

//    T saveAll(Collection<T> collection);

}
