package com.cdkhd.npc.config;

import com.cdkhd.npc.repository.base.AreaRepository;
import com.cdkhd.npc.repository.base.TownRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

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
//    @Bean
//    public UserDetailsImpl npcUser() {
//        Set<String> roles = new HashSet<>();
//        roles.add(AccountRoleEnum.NPC_MEMBER.getName());
//        Area area = areaRepository.findByUid("7727372632898398487");
//        Town town = townRepository.findByUid("4da21c49ebba4a13a0c9d93fc4161523");
//
//        return new UserDetailsImpl("178267378892", "liyang", "123456", roles, area, town, (byte) 2);
//    }
}
