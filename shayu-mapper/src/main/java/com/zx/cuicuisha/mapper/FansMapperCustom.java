package com.zx.cuicuisha.mapper;

import com.zx.cuicuisha.my.mapper.MyMapper;
import com.zx.cuicuisha.pojo.Fans;
import com.zx.cuicuisha.vo.FansAdminVO;
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

   /**
    * 查询所有粉丝关系（管理端使用）
    * @param map 查询参数，如果包含myId则查询指定用户的粉丝，否则查询所有
    * @return 粉丝关系列表
    */
   public List<FansAdminVO> queryAllFansRelations(@Param("paramMap") Map<String, Object> map);

}