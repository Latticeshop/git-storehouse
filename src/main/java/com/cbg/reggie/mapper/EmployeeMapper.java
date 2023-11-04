package com.cbg.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cbg.reggie.domain.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
