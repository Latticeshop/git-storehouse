package com.cbg.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cbg.reggie.domain.dto.SetmealDto;
import com.cbg.reggie.domain.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    /**
     * 新增套餐，保存套餐和菜品的关联信息
     *
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     *
     * @param ids
     */
    public void deleteWithDish(List<Long> ids);
}
