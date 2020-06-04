package com.cdkhd.npc;

import com.cdkhd.npc.entity.Account;
import com.cdkhd.npc.entity.Area;
import com.cdkhd.npc.entity.Voter;
import com.cdkhd.npc.entity.dto.AccountPageDto;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.LoginUPRepository;
import com.cdkhd.npc.repository.base.VoterRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.criteria.Predicate;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ServerApplicationTests {

    @Autowired
    private LoginUPRepository loginUPRepository;

    @Autowired
    private VoterRepository voterRepository;

    @Autowired
    private AccountRepository accountRepository;

//    @Test
//    public void contextLoads() {
//        LoginUP loginUP = loginUPRepository.findByUsername("liyang");
//        Account account = loginUP.getAccount();
//        String uid = account.getUid();
//        LoginUP loginUP1 = account.getLoginUP();
//        String username = loginUP1.getUsername();
//        String password = loginUP1.getPassword();
//        Voter voter = account.getVoter();
//        Area area = voter.getArea();
//        Town town = voter.getTown();
//        UserDetailsImpl userDetails1 = new UserDetailsImpl(account.getUid(), account.getLoginUP().getUsername(), account.getLoginUP().getPassword(), Sets.newHashSet(roles), account.getVoter().getArea(), account.getVoter().getTown(), LevelEnum.TOWN.getValue());
//    }

    @Test
    public void testVoterAndArea(){
        Voter voter = voterRepository.findByUid("132324ewder1332");
        Area area = voter.getArea();
    }

    @Test
    public void testAccount(){
        AccountPageDto accountPageDto = new AccountPageDto();
        int begin = accountPageDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, accountPageDto.getSize(), Sort.Direction.fromString(accountPageDto.getDirection()), accountPageDto.getProperty());
        Page<Account> accountPage = accountRepository.findAll((Specification<Account>)(root, query, cb) -> {
//            List<Predicate> predicates = new ArrayList<>();
            Predicate predicate = root.isNotNull();
            predicate = cb.and(predicate, cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicate = cb.and(predicate, cb.equal(root.get("loginWay").as(Byte.class), (byte)2));
//            if (StringUtils.isNotEmpty(accountPageDto.getRealname())){
//                predicates.add(cb.like(root.get("voter").get("realname").as(String.class), "%" + accountPageDto.getRealname() + "%"));
//            }
//            if (StringUtils.isNotEmpty(accountPageDto.getMobile())){
//                predicates.add(cb.equal(root.get("voter").get("mobile").as(String.class), accountPageDto.getMobile()));
//            }
            return predicate;
        }, page);

//        Page<Village> villagePage = villageRepository.findAll((Specification<Village>) (root, query, cb) -> {
//            Predicate predicate = root.isNotNull();
//            if (StringUtils.isNotEmpty(villagePageDto.getName())) {
//                predicate = cb.and(predicate, cb.like(root.get("name").as(String.class), "%" + villagePageDto.getName() +"%"));
//            }
//            if (StringUtils.isNotEmpty(villagePageDto.getGroup())) {
//                if (GroupEnum.UNGROUPED.getValue().equals(villagePageDto.getGroup())){
//                    predicate = cb.and(predicate, cb.isNull(root.get("npcMemberGroup")));
//                }else {
//                    predicate = cb.and(predicate, cb.equal(root.get("npcMemberGroup").get("uid").as(String.class), villagePageDto.getGroup()));
//                }
//            }
//            predicate = cb.and(predicate, cb.equal(root.get("town").get("uid").as(String.class),userDetails.getTown().getUid()));  //只查询当前镇所属的村
//            return predicate;
//        }, page);
//        System.out.println(accountPage.getContent().toString());
    }
}
