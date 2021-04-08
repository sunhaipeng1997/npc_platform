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
//    public MobileUserDetailsImpl npcUser() {
//        Set<String> roles = new HashSet<>();
//        roles.add(AccountRoleEnum.NPC_MEMBER.getName());
//        Area area = areaRepository.findByUid("11324weer343441");
//        Town town = townRepository.findByUid("8637a2ca2d7b4683827ec23856726fe0");
//
//        return new MobileUserDetailsImpl("hsbdhbhdbhdh13666627672637637311", "boshi", "123456", roles, area, town, (byte) 1);
//    }
}
