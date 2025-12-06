package com.zx.cuicuisha.mapper;

import com.zx.cuicuisha.my.mapper.MyMapper;
import com.zx.cuicuisha.pojo.Fans;
import com.zx.cuicuisha.vo.FansVO;
import com.zx.cuicuisha.vo.VlogerVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface FansMapperCustom extends MyMapper<Fans> {

   public List<VlogerVO> queryMyFollows(@Param("paramMap") Map<String, Object> map);

   public List<FansVO> queryMyFans(@Param("paramMap") Map<String, Object> map);

}