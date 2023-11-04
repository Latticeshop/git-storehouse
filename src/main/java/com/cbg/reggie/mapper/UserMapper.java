package com.cbg.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cbg.reggie.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
