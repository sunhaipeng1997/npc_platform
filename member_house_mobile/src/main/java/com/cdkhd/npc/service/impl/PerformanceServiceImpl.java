package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.AddPerformanceDto;
import com.cdkhd.npc.enums.Level;
import com.cdkhd.npc.enums.Status;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.member_house.NpcMemberRepository;
import com.cdkhd.npc.repository.member_house.PerformanceImageRepository;
import com.cdkhd.npc.repository.member_house.PerformanceRepository;
import com.cdkhd.npc.repository.member_house.PerformanceTypeRepository;
import com.cdkhd.npc.service.PerformanceService;
import com.cdkhd.npc.service.SystemSettingService;
import com.cdkhd.npc.util.SysUtil;
import com.cdkhd.npc.utils.ImageUploadUtil;
import com.cdkhd.npc.utils.NpcMemberUtil;
import com.cdkhd.npc.vo.CommonVo;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class PerformanceServiceImpl implements PerformanceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceServiceImpl.class);

    private PerformanceRepository performanceRepository;

    private PerformanceTypeRepository performanceTypeRepository;

    private SystemSettingService systemSettingService;

    private NpcMemberRepository npcMemberRepository;

    private AccountRepository accountRepository;

    private PerformanceImageRepository performanceImageRepository;

    @Autowired
    public PerformanceServiceImpl(PerformanceRepository performanceRepository, PerformanceTypeRepository performanceTypeRepository, SystemSettingService systemSettingService, NpcMemberRepository npcMemberRepository, AccountRepository accountRepository, PerformanceImageRepository performanceImageRepository) {
        this.performanceRepository = performanceRepository;
        this.performanceTypeRepository = performanceTypeRepository;
        this.systemSettingService = systemSettingService;
        this.npcMemberRepository = npcMemberRepository;
        this.accountRepository = accountRepository;
        this.performanceImageRepository = performanceImageRepository;
    }

    /**
     * 履职类型列表
     * @param userDetails
     * @return
     */
    @Override
    public RespBody performanceTypeList(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        List<PerformanceType> performanceTypeList = performanceTypeRepository.findByStatus(Status.ENABLED.getValue());
        List<CommonVo> types = performanceTypeList.stream().map(type -> CommonVo.convert(type.getUid(),type.getName())).collect(Collectors.toList());
        body.setData(types);
        return body;
    }

    /**
     * 添加或修改履职
     * @param userDetails
     * @return
     */
    @Override
    public RespBody addOrUpdatePerformance(UserDetailsImpl userDetails, AddPerformanceDto addPerformanceDto) {
        RespBody body = new RespBody();

        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(addPerformanceDto.getLevel(), account.getNpcMembers());

        //当前用户是否为工作在当前区镇的代表
        if (npcMember == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("您不是代表该区/镇的代表，无法添加履职");
            return body;
        }
        Performance performance;
        //验证履职uid参数是否传过来了
        if (StringUtils.isEmpty(addPerformanceDto.getUid())) {
            //没有uid表示添加履职
            //查询是否是的第一次提交
            performance = performanceRepository.findByTransUid(addPerformanceDto.getTransUid());
        }else{
            //有uid表示修改履职
            //查询是否是的第一次提交
            performance = performanceRepository.findByUidAndTransUid(addPerformanceDto.getUid(), addPerformanceDto.getTransUid());

        }
        if (performance == null) {//如果是第一次提交，就保存基本信息
            performance = new Performance();
            performance.setLevel(addPerformanceDto.getLevel());
            performance.setArea(npcMember.getArea());
            performance.setTown(npcMember.getTown());
            performance.setNpcMember(npcMember);
            //设置完了基本信息后，给相应的审核人员推送消息
            if (addPerformanceDto.getLevel().equals(Level.TOWN.getValue())){
                //如果是在镇上履职，那么查询镇上的审核人员
                //首先判断端当前用户的角色是普通代表还是小组审核人员还是总审核人员
                Set<String> keyword = Sets.newHashSet();//权限的集合
//                SystemSetting systemSetting =

                //判断当前代表的权限
//                if (keyword.contains("小组履职审核权限") )
            }else{
                //如果是在区上履职，那么查询区上的审核人员

            }
        }
        performance.setPerformanceType(performanceTypeRepository.findByUid(addPerformanceDto.getPerformanceType()));
        performance.setTitle(addPerformanceDto.getTitle());
        performance.setWorkAt(addPerformanceDto.getWorkAt());
        performance.setContent(addPerformanceDto.getContent());
        performanceRepository.saveAndFlush(performance);

        if (addPerformanceDto.getImage() != null) {//有附件，就保存附件信息
            this.saveCover(addPerformanceDto.getImage(),performance);
        }

        return body;
    }

    public void saveCover(MultipartFile cover,Performance performance){
        //保存图片到文件系统
        String url = ImageUploadUtil.saveImage("experienceImage", SysUtil.uid(), cover);
        if (url.equals("error")) {
            LOGGER.error("保存图片到文件系统失败");
        }
        //保存图片到数据库
        PerformanceImage performanceImage = new PerformanceImage();
        performanceImage.setUrl(url);
        performanceImage.setPerformance(performance);
        performanceImageRepository.saveAndFlush(performanceImage);
    }
}
