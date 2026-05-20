package com.redblade.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.redblade.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜单 Mapper
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
}