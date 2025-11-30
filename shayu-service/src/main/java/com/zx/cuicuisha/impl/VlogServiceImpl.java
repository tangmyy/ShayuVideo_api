package com.zx.cuicuisha.impl;

import com.zx.cuicuisha.bo.VlogBO;
import com.zx.cuicuisha.enums.YesOrNo;
import com.zx.cuicuisha.mapper.VlogMapper;
import com.zx.cuicuisha.mapper.VlogMapperCustom;
import com.zx.cuicuisha.pojo.Vlog;
import com.zx.cuicuisha.service.VlogService;
import com.zx.cuicuisha.vo.IndexVlogVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VlogServiceImpl implements VlogService {

   @Autowired
   private VlogMapper vlogMapper;

   @Autowired
   private VlogMapperCustom vlogMapperCustom;

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
   public List<IndexVlogVO> getIndexVlogList(String search) {
      Map<String, Object> map = new HashMap<>();
      if (StringUtils.isNoneBlank(search)){
         map.put("search", search);
      }
      List<IndexVlogVO> list = vlogMapperCustom.getIndexVlogList(map);

      return list;
   }


}
