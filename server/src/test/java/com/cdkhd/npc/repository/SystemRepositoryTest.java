package com.cdkhd.npc.repository;

import com.cdkhd.npc.entity.Systems;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.repository.base.SystemRepository;
import com.cdkhd.npc.repository.member_house.SuggestionRepository;
import com.cdkhd.npc.vo.CountVo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SystemRepositoryTest {
    @Autowired
    private SystemRepository systemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SuggestionRepository suggestionRepository;

    @Test
    public void testSaveAndFlush() {
        Systems systems = new Systems();
        systems.setName("test");
        systems.setEnabled(true);
        //测试可知，调用saveAndFlush方法后会改变参数（systems对象）的值
        Systems s = systemRepository.saveAndFlush(systems);

        System.out.println(systems.getId());
        System.out.println(s.getId());
    }

    @Test
    public void testPasswordEncoder() {
        System.out.println("123456的密文：" + passwordEncoder.encode("123456"));
        System.out.println(passwordEncoder.matches("123456", "$2a$10$UrA03T9zVXczQjZl6sqImOdNt0WG8JW09gqwTW/9DL4YwOV6rDQQO"));
        System.out.println(passwordEncoder.matches("123456", "$2a$10$fd8Q/qsMGKgP85Ik5DmO/OYjSFp4AkXOn4gPM9Ofhv1/xhW3TiYBS"));
    }

    @Test
    public void testCount() {
        List<CountVo> countVos = suggestionRepository.countByArea(1L);
        System.out.println(countVos);
    }
}
