package com.zx.cuicuisha.impl;

import com.github.pagehelper.PageHelper;
import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.bo.VlogBO;
import com.zx.cuicuisha.enums.YesOrNo;
import com.zx.cuicuisha.mapper.MyLikedVlogMapper;
import com.zx.cuicuisha.mapper.VlogMapper;
import com.zx.cuicuisha.mapper.VlogMapperCustom;
import com.zx.cuicuisha.pojo.MyLikedVlog;
import com.zx.cuicuisha.pojo.Vlog;
import com.zx.cuicuisha.service.VlogService;
import com.zx.cuicuisha.utils.PagedGridResult;
import com.zx.cuicuisha.vo.IndexVlogVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VlogServiceImpl extends BaseInfoProperties implements VlogService {

   @Autowired
   private VlogMapper vlogMapper;

   @Autowired
   private VlogMapperCustom vlogMapperCustom;

   @Autowired
   private MyLikedVlogMapper myLikedVlogMapper;

   @Autowired
   private Sid sid;

   @Transactional
   @Override
   public void createVlog(VlogBO vlogBO) {
      String vid = sid.nextShort();
      Vlog vlog = new Vlog();
      BeanUtils.copyProperties(vlogBO, vlog);
      vlog.setId(vid);
      vlog.setLikeCounts(0);
      vlog.setCommentsCounts(0);
      vlog.setIsPrivate(YesOrNo.NO.type);
      vlog.setCreatedTime(new Date());
      vlog.setUpdatedTime(new Date());
      vlogMapper.insert(vlog);

   }

   @Override
   public PagedGridResult getIndexVlogList(String search, Integer page, Integer pageSize) {
      // 开始分页
      PageHelper.startPage(page, pageSize);
      Map<String, Object> map = new HashMap<>();
      if (StringUtils.isNoneBlank(search)){
         map.put("search", search);
      }
      List<IndexVlogVO> list = vlogMapperCustom.getIndexVlogList(map);
      // return list;
      return setterPagedGrid(list, page);
   }

   @Override
   public IndexVlogVO getVlogDetailById(String vlogId) {
      Map<String, Object> map = new HashMap<>();
      map.put("vlogId", vlogId);
      List<IndexVlogVO> list = vlogMapperCustom.getVlogDetailById(map);
      if (list != null && !list.isEmpty()){
         IndexVlogVO vlogVO = list.get(0);
         return vlogVO;
      }

      return null;
   }

   @Transactional
   @Override
   public void changeToPrivateOrPublic(String userId, String vlogId, Integer yesOrNo) {
      Example example = new Example(Vlog.class);
      Example.Criteria criteria = example.createCriteria();
      criteria.andEqualTo("id", vlogId);
      criteria.andEqualTo("vlogerId", userId);
      Vlog pendingVlog = new Vlog();
      pendingVlog.setIsPrivate(yesOrNo);
      vlogMapper.updateByExampleSelective(pendingVlog, example);
   }

   @Override
   public PagedGridResult queryMyVlogList(String userId, Integer page, Integer pageSize, Integer yesOrNo) {
      Example example = new Example(Vlog.class);
      Example.Criteria criteria = example.createCriteria();
      criteria.andEqualTo("vlogerId", userId);
      criteria.andEqualTo("isPrivate", yesOrNo);
      PageHelper.startPage(page, pageSize);
      List<Vlog> list = vlogMapper.selectByExample(example);
      return setterPagedGrid(list, page);
   }

   @Transactional
   @Override
   public void userLikeVlog(String userId, String vlogId) {
      String lVid = sid.nextShort();
      MyLikedVlog likedVlog = new MyLikedVlog();
      likedVlog.setId(lVid);
      likedVlog.setVlogId(vlogId);
      likedVlog.setUserId(userId);
      myLikedVlogMapper.insert(likedVlog);
   }

}
