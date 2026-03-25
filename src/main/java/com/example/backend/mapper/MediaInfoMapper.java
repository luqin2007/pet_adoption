package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.backend.entity.MediaFile;
import com.example.backend.util.C;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;

/**
 * 索引：
 * - (parentType, parentId, isCover, type)
 * - (parentType, parentId, type, createTime)
 */
@Mapper
public interface MediaInfoMapper extends IBaseMapper<MediaFile> {

    /**
     * 查询指定类型的 id 和文件名
     * - 索引：(parentType, parentId)
     * - 结果类型：[MediaInfo]
     *
     * @param parentType 关联类型
     * @param parentId   关联 id
     */
    default LambdaQueryWrapper<MediaFile> queryIdAndFilename(String parentType, Long parentId) {
        return lambdaQuery()
                .eq(MediaFile::getParentType, parentType)
                .eq(MediaFile::getParentId, parentId)
                .select(MediaFile::getId, MediaFile::getFilename);
    }

    /**
     * 查找封面
     * - 索引：(parentType, parentId, isCover, type)
     * - 结果类型：[MediaInfo]
     *
     * @param parentType 关联类型
     * @param parentId   关联 id
     */
    default LambdaQueryWrapper<MediaFile> queryCover(String parentType, Long parentId) {
        return lambdaQuery()
                .eq(MediaFile::getParentType, parentType)
                .eq(MediaFile::getParentId, parentId)
                .eq(MediaFile::getIsCover, true)
                .eq(MediaFile::getType, C.MEDIA_TYPE_IMAGE);
    }

    /**
     * 查找封面文件名
     * - 索引：(parentType, parentId, isCover, type)
     * - 结果类型：[MediaInfo(filename)]
     *
     * @param parentType 关联类型
     * @param parentId   关联 id
     */
    default LambdaQueryWrapper<MediaFile> queryCoverFilename(String parentType, Long parentId) {
        return queryCover(parentType, parentId).select(MediaFile::getFilename);
    }

    /**
     * 批量查找封面地址
     * - 索引：(parentType, parentId, isCover, type)
     * - 结果类型：[MediaInfo(parentId, filename)]
     *
     * @param parentType 关联类型
     * @param parentId   关联 id
     */
    default LambdaQueryWrapper<MediaFile> queryCoverFilenames(String parentType, Collection<Long> parentId) {
        return lambdaQuery()
                .eq(MediaFile::getParentType, parentType)
                .in(MediaFile::getParentId, parentId)
                .eq(MediaFile::getIsCover, true)
                .eq(MediaFile::getType, C.MEDIA_TYPE_IMAGE)
                .select(MediaFile::getParentId, MediaFile::getFilename);
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
    default LambdaQueryWrapper<MediaFile> queryCover(String parentType, Long parentId, Long exceptImageId) {
        return lambdaQuery()
                .eq(MediaFile::getParentType, parentType)
                .eq(MediaFile::getParentId, parentId)
                .eq(MediaFile::getIsCover, true)
                .eq(MediaFile::getType, C.MEDIA_TYPE_IMAGE)
                .ne(MediaFile::getId, exceptImageId);
    }

    /**
     * 清除封面
     * - 索引：(parentType, parentId, isCover, type)
     * - 结果类型：[MediaInfo]
     *
     * @param parentType 关联类型
     * @param parentId   关联 id
     */
    default LambdaUpdateWrapper<MediaFile> clearCover(String parentType, Long parentId) {
        return lambdaUpdate()
                .eq(MediaFile::getParentType, parentType)
                .eq(MediaFile::getParentId, parentId)
                .eq(MediaFile::getIsCover, true)
                .eq(MediaFile::getType, C.MEDIA_TYPE_IMAGE)
                .set(MediaFile::getIsCover, false);
    }

    /**
     * 查询最新图片
     * - 索引：(parentType, parentId, type, createTime)
     * - 结果类型：[MediaInfo(id)]
     *
     * @param parentType 关联类型
     * @param parentId   关联 id
     */
    default LambdaQueryWrapper<MediaFile> queryLatestImageId(String parentType, Long parentId) {
        return lambdaQuery()
                .eq(MediaFile::getParentType, parentType)
                .eq(MediaFile::getParentId, parentId)
                .eq(MediaFile::getType, C.MEDIA_TYPE_IMAGE)
                .orderByDesc(MediaFile::getCreateTime)
                .select(MediaFile::getId);
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
    default LambdaQueryWrapper<MediaFile> queryLatestImageId(String parentType, Long parentId, Long exceptImageId) {
        return lambdaQuery()
                .eq(MediaFile::getParentType, parentType)
                .eq(MediaFile::getParentId, parentId)
                .eq(MediaFile::getType, C.MEDIA_TYPE_IMAGE)
                .orderByDesc(MediaFile::getCreateTime)
                .ne(MediaFile::getId, exceptImageId)
                .select(MediaFile::getId);
    }

    @Override
    default String getMissingMessage() {
        return "图片/视频不存在";
    }
}
