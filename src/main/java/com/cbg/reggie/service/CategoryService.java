package com.cbg.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cbg.reggie.domain.entity.Category;

public interface CategoryService extends IService<Category> {
    public void remove(Long id);
}
