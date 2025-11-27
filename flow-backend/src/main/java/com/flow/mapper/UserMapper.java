package com.flow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flow.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
}
