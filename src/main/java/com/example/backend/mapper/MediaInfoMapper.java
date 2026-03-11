package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.backend.entity.MediaInfo;
import com.example.backend.util.C;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;

/**
 * 索引：
 * - (parentType, parentId, isCover, type)
 * - (parentType, parentId, type, createTime)
 */
@Mapper
public interface MediaInfoMapper extends IBaseMapper<MediaInfo> {

    /**
     * 查询指定类型的 id 和文件名
     * - 索引：(parentType, parentId)
     * - 结果类型：[MediaInfo]
     *
     * @param parentType 关联类型
     * @param parentId   关联 id
     */
    default LambdaQueryWrapper<MediaInfo> queryIdAndFilename(String parentType, Long parentId) {
        return lambdaQuery()
                .eq(MediaInfo::getParentType, parentType)
                .eq(MediaInfo::getParentId, parentId)
                .select(MediaInfo::getId, MediaInfo::getFilename);
    }

    /**
     * 查找封面
     * - 索引：(parentType, parentId, isCover, type)
     * - 结果类型：[MediaInfo]
     *
     * @param parentType 关联类型
     * @param parentId   关联 id
     */
    default LambdaQueryWrapper<MediaInfo> queryCover(String parentType, Long parentId) {
        return lambdaQuery()
                .eq(MediaInfo::getParentType, parentType)
                .eq(MediaInfo::getParentId, parentId)
                .eq(MediaInfo::getIsCover, true)
                .eq(MediaInfo::getType, C.MEDIA_TYPE_IMAGE);
    }

    /**
     * 查找封面文件名
     * - 索引：(parentType, parentId, isCover, type)
     * - 结果类型：[MediaInfo(filename)]
     *
     * @param parentType 关联类型
     * @param parentId   关联 id
     */
    default LambdaQueryWrapper<MediaInfo> queryCoverFilename(String parentType, Long parentId) {
        return queryCover(parentType, parentId).select(MediaInfo::getFilename);
    }

    /**
     * 批量查找封面地址
     * - 索引：(parentType, parentId, isCover, type)
     * - 结果类型：[MediaInfo(parentId, filename)]
     *
     * @param parentType 关联类型
     * @param parentId   关联 id
     */
    default LambdaQueryWrapper<MediaInfo> queryCoverFilenames(String parentType, Collection<Long> parentId) {
        return lambdaQuery()
                .eq(MediaInfo::getParentType, parentType)
                .in(MediaInfo::getParentId, parentId)
                .eq(MediaInfo::getIsCover, true)
                .eq(MediaInfo::getType, C.MEDIA_TYPE_IMAGE)
                .select(MediaInfo::getParentId, MediaInfo::getFilename);
    }

    /**
     * 查找封面，排除指定图片
     * - 索引：(parentType, parentId, isCover, type)
     * - 结果类型：[MediaInfo]
     *
     * @param parentType    关联类型
     * @param parentId      关联 id
     * @param exceptImageId 排除的图片 id
     * @return [MediaInfo]
     */
    default LambdaQueryWrapper<MediaInfo> queryCover(String parentType, Long parentId, Long exceptImageId) {
        return lambdaQuery()
                .eq(MediaInfo::getParentType, parentType)
                .eq(MediaInfo::getParentId, parentId)
                .eq(MediaInfo::getIsCover, true)
                .eq(MediaInfo::getType, C.MEDIA_TYPE_IMAGE)
                .ne(MediaInfo::getId, exceptImageId);
    }

    /**
     * 清除封面
     * - 索引：(parentType, parentId, isCover, type)
     * - 结果类型：[MediaInfo]
     *
     * @param parentType 关联类型
     * @param parentId   关联 id
     */
    default LambdaUpdateWrapper<MediaInfo> clearCover(String parentType, Long parentId) {
        return lambdaUpdate()
                .eq(MediaInfo::getParentType, parentType)
                .eq(MediaInfo::getParentId, parentId)
                .eq(MediaInfo::getIsCover, true)
                .eq(MediaInfo::getType, C.MEDIA_TYPE_IMAGE)
                .set(MediaInfo::getIsCover, false);
    }

    /**
     * 查询最新图片
     * - 索引：(parentType, parentId, type, createTime)
     * - 结果类型：[MediaInfo(id)]
     *
     * @param parentType 关联类型
     * @param parentId   关联 id
     */
    default LambdaQueryWrapper<MediaInfo> queryLatestImageId(String parentType, Long parentId) {
        return lambdaQuery()
                .eq(MediaInfo::getParentType, parentType)
                .eq(MediaInfo::getParentId, parentId)
                .eq(MediaInfo::getType, C.MEDIA_TYPE_IMAGE)
                .orderByDesc(MediaInfo::getCreateTime)
                .select(MediaInfo::getId);
    }

    /**
     * 查询最新图片，排除指定图片
     * - 索引：(parentType, parentId, type, createTime)
     * - 结果类型：[MediaInfo(id)]
     *
     * @param parentType    关联类型
     * @param parentId      关联 id
     * @param exceptImageId 排除的图片 id
     */
    default LambdaQueryWrapper<MediaInfo> queryLatestImageId(String parentType, Long parentId, Long exceptImageId) {
        return lambdaQuery()
                .eq(MediaInfo::getParentType, parentType)
                .eq(MediaInfo::getParentId, parentId)
                .eq(MediaInfo::getType, C.MEDIA_TYPE_IMAGE)
                .orderByDesc(MediaInfo::getCreateTime)
                .ne(MediaInfo::getId, exceptImageId)
                .select(MediaInfo::getId);
    }
}
