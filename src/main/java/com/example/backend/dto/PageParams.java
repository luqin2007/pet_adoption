package com.example.backend.dto;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.util.StringUtils;
import lombok.Data;

import java.net.URLDecoder;

@Data
public class PageParams {

    private Integer page = 1;

    private Integer size = 10;

    private String sort = "id";

    private String order = null;

    public <T> Page<T> createPage() {
        Page<T> page = Page.of(this.page, size, true);
        String rSort = URLDecoder.decode(sort).trim();
        if (StringUtils.hasText(rSort)) {
            boolean asc = StringUtils.hasText(this.order)
                    ? this.order.trim().equalsIgnoreCase("asc")
                    // 所有时间相关的查询列均以 Time 结尾，默认降序
                    : !rSort.endsWith("Time");
            page.addOrder(new OrderItem()
                    .setColumn(rSort)
                    .setAsc(asc));
        }
        return page;
    }
}
