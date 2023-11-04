package com.cbg.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cbg.reggie.domain.dto.DishDto;
import com.cbg.reggie.domain.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
    //新增菜品，增加口味数据，需要两张表dish,dish_flavor
    public void saveWithFlavor(DishDto dishDto);

    //根据id查询菜品信息和口味信息
    public DishDto getByIdWithFlavor(Long id);

    public List<DishDto> getByIdsWithFlavor(List<Long> ids);

    public void updateWithFlavor(DishDto dishDto);
}
