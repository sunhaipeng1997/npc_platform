package com.cdkhd.npc.config;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.Area;
import com.cdkhd.npc.entity.Town;
import com.cdkhd.npc.repository.base.AreaRepository;
import com.cdkhd.npc.repository.base.TownRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class UserConfig {

    private AreaRepository areaRepository;
    private TownRepository townRepository;

    @Autowired
    public UserConfig(AreaRepository areaRepository, TownRepository townRepository) {
        this.areaRepository = areaRepository;
        this.townRepository = townRepository;
    }

    //开发阶段模拟一个UserDetailsImpl对象
    @Bean
    public UserDetailsImpl npcUser() {
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_NPC");
        Area area = areaRepository.findByUid("9ad12abd2dd811ea8f3f0242ac170005");
        Town town = townRepository.findByUid("ce9028c82dd811ea8f3f0242ac170005");

        return new UserDetailsImpl("751806ea2d4211ea8f3f0242ac170005", "ll", "password", roles,area,town,(byte)1);
    }
}
