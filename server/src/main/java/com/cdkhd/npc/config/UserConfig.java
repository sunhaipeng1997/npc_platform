package com.cdkhd.npc.config;

        import com.cdkhd.npc.component.UserDetailsImpl;
        import com.cdkhd.npc.entity.Area;
        import com.cdkhd.npc.enums.AccountRoleEnum;
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
        roles.add(AccountRoleEnum.NPC_MEMBER.getName());
        Area area = areaRepository.findByUid("7727372632898398487");
        //Town town = townRepository.findByUid("0be1568713f34a8a82f0479c07462273");

        return new UserDetailsImpl("7167137817287vxcvzxvz", "liyang", "123456", roles, area, null, (byte) 2);
    }
}
