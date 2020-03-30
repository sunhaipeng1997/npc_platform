package com.cdkhd.npc.repository;

import com.cdkhd.npc.entity.Systems;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.repository.base.SystemRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SystemRepositoryTest {
    @Autowired
    private SystemRepository systemRepository;

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
}
