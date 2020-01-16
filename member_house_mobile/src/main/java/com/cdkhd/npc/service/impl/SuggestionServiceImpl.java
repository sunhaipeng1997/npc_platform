package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.SuggestionAddDto;
import com.cdkhd.npc.entity.dto.SuggestionAuditDto;
import com.cdkhd.npc.entity.dto.SuggestionPageDto;
import com.cdkhd.npc.entity.vo.SuggestionVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.NpcMemberRepository;
import com.cdkhd.npc.repository.member_house.SuggestionBusinessRepository;
import com.cdkhd.npc.repository.member_house.SuggestionImageRepository;
import com.cdkhd.npc.repository.member_house.SuggestionReplyRepository;
import com.cdkhd.npc.repository.member_house.SuggestionRepository;
import com.cdkhd.npc.service.SuggestionService;
import com.cdkhd.npc.util.ImageUploadUtil;
import com.cdkhd.npc.util.SysUtil;
import com.cdkhd.npc.utils.NpcMemberUtil;
import com.cdkhd.npc.vo.CommonVo;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.data.NumbericRenderData;
import com.deepoove.poi.data.TextRenderData;
import com.deepoove.poi.data.builder.StyleBuilder;
import com.deepoove.poi.data.style.Style;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.Predicate;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SuggestionServiceImpl implements SuggestionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SuggestionServiceImpl.class);

    private final SuggestionRepository suggestionRepository;

    private final AccountRepository accountRepository;

    private final SuggestionImageRepository suggestionImageRepository;

//    private final NpcMemberRepository npcMemberRepository;

    private final SuggestionBusinessRepository suggestionBusinessRepository;

    private final SuggestionReplyRepository suggestionReplyRepository;

    private final Environment env;

    @Autowired
    public SuggestionServiceImpl(SuggestionRepository suggestionRepository, AccountRepository accountRepository, SuggestionImageRepository suggestionImageRepository, NpcMemberRepository npcMemberRepository, SuggestionBusinessRepository suggestionBusinessRepository, SuggestionReplyRepository suggestionReplyRepository, Environment env) {
        this.suggestionRepository = suggestionRepository;
        this.accountRepository = accountRepository;
        this.suggestionImageRepository = suggestionImageRepository;
        this.suggestionBusinessRepository = suggestionBusinessRepository;
        this.suggestionReplyRepository = suggestionReplyRepository;
        this.env = env;
    }

    /**
     * 建议类型列表
     *
     * @param userDetails
     * @return
     */
    @Override
    public RespBody sugBusList(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        List<SuggestionBusiness> sb = org.apache.commons.compress.utils.Lists.newArrayList();
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            sb = suggestionBusinessRepository.findByLevelAndTownUidAndIsDelFalse(userDetails.getLevel(),userDetails.getTown().getUid());
        }else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){
            sb = suggestionBusinessRepository.findByLevelAndAreaUidAndIsDelFalse(userDetails.getLevel(),userDetails.getArea().getUid());
        }
        List<CommonVo> commonVos = sb.stream().map(sugBus -> CommonVo.convert(sugBus.getUid(), sugBus.getName())).collect(Collectors.toList());
        body.setData(commonVos);
        return body;
    }

    @Override
    public RespBody npcMemberSug(UserDetailsImpl userDetails, SuggestionPageDto dto) {
        RespBody<PageVo<SuggestionVo>> body = new RespBody<>();
        int begin = dto.getPage() - 1;
        Pageable page = PageRequest.of(begin, dto.getSize(), Sort.Direction.fromString(dto.getDirection()), dto.getProperty());
        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(userDetails.getLevel(), account.getNpcMembers());
        PageVo<SuggestionVo> vo = new PageVo<>(dto);
        if (npcMember != null) {
            Page<Suggestion> pageRes = suggestionRepository.findAll((Specification<Suggestion>) (root, query, cb) -> {
                Predicate predicate = cb.equal(root.get("raiser").get("uid").as(String.class), npcMember.getUid());
                if (dto.getStatus() == (byte)2){  //未审核
                    predicate = cb.equal(root.get("status").as(Byte.class), (byte)2);
                }else if (dto.getStatus() == (byte)3){  //已审核
                    predicate = cb.notEqual(root.get("status").as(Byte.class), (byte)2);
                }
                return predicate;
            }, page);
            vo.setContent(pageRes.stream().map(SuggestionVo::convert).collect(Collectors.toList()));
            vo.copy(pageRes);
        }
        body.setData(vo);
        return body;
    }

    @Override
    public RespBody addOrUpdateSuggestion(UserDetailsImpl userDetails, SuggestionAddDto dto) {
        RespBody body = new RespBody();

        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(dto.getLevel(), account.getNpcMembers());

        //判断当前用户是否为工作在当前区镇的代表
        if (npcMember == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("您不是该区/镇的代表，无法添加建议");
            return body;
        }

        Suggestion suggestion;
        //验证建议uid参数是否传过来了
        if (StringUtils.isEmpty(dto.getUid())) {
            //没有uid表示添加建议
            //查询是否是的第一次提交
            suggestion = suggestionRepository.findByTransUid(dto.getTransUid());
        } else {
            //有uid表示修改建议
            //查询是否是的第一次提交
            suggestion = suggestionRepository.findByUidAndTransUid(dto.getUid(), dto.getTransUid());

        }
        if (suggestion == null) { //如果是第一次提交，就保存基本信息
            suggestion = new Suggestion();
            suggestion.setLevel(dto.getLevel());
            suggestion.setArea(npcMember.getArea());
            suggestion.setTown(npcMember.getTown());
            suggestion.setRaiser(npcMember);
            suggestion.setStatus((byte) 2);  //建议状态改为“已提交待审核”

            //设置完基本信息后，给相应审核人员推送消息
//            List<NpcMember> auditors;
//
//            if (dto.getLevel().equals(LevelEnum.TOWN.getValue())) {
//                //如果是在镇上提建议，那么查询镇上的审核人员
//                //首先判断端当前用户的角色是普通代表还是小组审核人员还是总审核人员
//                Set<String> keyword = Sets.newHashSet();//权限的集合
//                SystemSetting systemSetting =

                //判断当前代表的权限
//                if (keyword.contains("小组履职审核权限") )
//            } else {
//                //如果是在区上提建议，那么查询区上的审核人员
//
//            }
        }
        suggestion.setTitle(dto.getTitle());
        suggestion.setContent(dto.getContent());
        suggestion.setRaiseTime(dto.getRaiseTime());
        suggestionRepository.saveAndFlush(suggestion);

        if (dto.getImages() != null) {  //有附件，保存附件信息
            this.saveCover(dto.getImages(), suggestion);
        }
        return body;
    }

    public void saveCover(MultipartFile cover, Suggestion suggestion) {
        //保存图片到文件系统
        String url = ImageUploadUtil.saveImage("suggestionImage", SysUtil.uid(), cover, 500, 500);
        if (url.equals("error")) {
            LOGGER.error("保存图片到文件系统失败");
        }
        //保存图片到数据库
        SuggestionImage suggestionImage = new SuggestionImage();
        suggestionImage.setUrl(url);
        suggestionImage.setSuggestion(suggestion);
        suggestionImageRepository.saveAndFlush(suggestionImage);
    }

    @Override
    public RespBody deleteSuggestion(String uid) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(uid)) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("请先选择一条建议");
        }
        Suggestion suggestion = suggestionRepository.findByUid(uid);
        suggestion.setDel(true);
        suggestionRepository.saveAndFlush(suggestion);
        return body;
    }

    @Override
    public String suggestionDetail(String uid) {
        String result = "成功";
        Suggestion suggestion = suggestionRepository.findByUid(uid);
        Map<String, Object> datas = new HashMap<>();
        datas.put("name", suggestion.getRaiser().getName());
        datas.put("mobile", suggestion.getRaiser().getMobile());
        datas.put("title", suggestion.getTitle());
        datas.put("content", suggestion.getContent());
        Set<SuggestionReply> replySet = suggestion.getReplies();
        if (CollectionUtils.isEmpty(replySet)) {
            datas.put("returnInfo", "暂无回复");
        } else {
            List<TextRenderData> renderDataList = Lists.newArrayList();
            Style style = StyleBuilder.newBuilder().buildFontSize(16).build();
            for (SuggestionReply suggestionReply : replySet) {
                suggestionReply.setView(1);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String repInfo = simpleDateFormat.format(suggestionReply.getCreateTime()) + " : " + suggestionReply.getReply();
                renderDataList.add(new TextRenderData(repInfo, style));
            }
            datas.put("returnInfo", new NumbericRenderData(NumbericRenderData.FMT_DECIMAL, style, renderDataList));
            suggestionReplyRepository.saveAll(replySet);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(suggestion.getRaiseTime());
        datas.put("year", calendar.get(Calendar.YEAR));
        datas.put("month", calendar.get(Calendar.MONTH) + 1);
        datas.put("day", calendar.get(Calendar.DATE));
        XWPFTemplate template = XWPFTemplate.compile("template/suggest_word_emplate.docx").render(datas);
        FileOutputStream out = null;
        String parentPath = String.format("static/public/suggest/%s", suggestion.getRaiser().getUid());
        File beforeFile = new File("template/suggest_word_emplate.docx");
        File bgFile = new File(parentPath, "suggestion.docx");
        try {
            if (!bgFile.exists()) {
                FileUtils.copyFile(beforeFile, bgFile);
            }
            result = bgFile.getPath().replace("static", "");
            out = new FileOutputStream(bgFile.getPath());
            template.write(out);
            out.flush();
        } catch (FileNotFoundException e) {
            result = "失败";
            e.printStackTrace();
        } catch (IOException e) {
            result = "失败";
            e.printStackTrace();
        } finally {
            try {
                out.close();
                template.close();
            } catch (IOException e) {
                result = "失败";
                e.printStackTrace();
            }
            return result;
        }
    }

    @Override
    public RespBody audit(UserDetailsImpl userDetails, SuggestionAuditDto suggestionAuditDto) {
        RespBody body = new RespBody();
        Suggestion suggestion = suggestionRepository.findByUid(suggestionAuditDto.getUid());
        if (suggestion == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("建议不存在");
            return body;
        }
        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(suggestionAuditDto.getLevel(), account.getNpcMembers());
        suggestion.setStatus((byte) 8);  //将建议状态设置成“自行办理”
        suggestion.setAuditReason(suggestionAuditDto.getReason());
        suggestion.setAuditTime(new Date());
        suggestion.setAuditor(npcMember);
        suggestionRepository.saveAndFlush(suggestion);
        return body;
    }

    @Override
    public RespBody suggestionRevoke(String uid) {
        RespBody body = new RespBody();
        Suggestion suggestion = suggestionRepository.findByUid(uid);
        if (suggestion == null) {
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("代表建议不存在");
            return body;
        }
        int timeout = 0;
        String time = env.getProperty("code.overdueTime");
        if (StringUtils.isNotBlank(time)) {
            timeout = Integer.parseInt(time);
        }
        Date expireAt = DateUtils.addMinutes(suggestion.getCreateTime(), timeout);
        int view = suggestion.getView();
        if (expireAt.before(new Date()) || view == 1) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("已超出撤回消息时间或该建议已被查看，无法撤回！！");
            return body;
        }
        suggestion.setCanOperate(true);
        suggestionRepository.saveAndFlush(suggestion);
        return body;
    }

    @Override
    public RespBody rank(UserDetailsImpl userDetails) {
        return null;
    }

    @Override
    public RespBody auditorSug(UserDetailsImpl userDetails, SuggestionPageDto dto) {
        RespBody<PageVo<SuggestionVo>> body = new RespBody<>();
        int begin = dto.getPage() - 1;
        Pageable page = PageRequest.of(begin, dto.getSize(), Sort.Direction.fromString(dto.getDirection()), dto.getProperty());
        PageVo<SuggestionVo> vo = new PageVo<>(dto);
        Page<Suggestion> pageRes = suggestionRepository.findAll((Specification<Suggestion>) (root, query, cb) -> {
            Predicate predicate = root.isNotNull();
//            predicate = cb.equal(root.get("raiser").get("level").as(Byte.class), userDetails.getLevel());
            predicate = cb.and(predicate, cb.equal(root.get("raiser").get("area").as(String.class), userDetails.getArea()));
            if (dto.getStatus() == (byte)2){  //未审核
                predicate = cb.equal(root.get("status").as(Byte.class), (byte)2);
            }else if (dto.getStatus() == (byte)3){  //已审核
                predicate = cb.notEqual(root.get("status").as(Byte.class), (byte)2);
            }
            return predicate;
        }, page);
        vo.setContent(pageRes.stream().map(SuggestionVo::convert).collect(Collectors.toList()));
        vo.copy(pageRes);
        body.setData(vo);
        return body;
    }
}