package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.backend.entity.MediaFile;
import com.example.backend.entity.property.ParentType;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Set;

import static com.example.backend.entity.property.MediaType.IMAGE;

/**
 * 索引：<br>
 * - (parentType, parentId, isCover, type)<br>
 * - (parentType, parentId, type, createTime)
 */
@Mapper
public interface MediaFileMapper extends IBaseMapper<MediaFile> {

    /**
     * 查询指定类型的 id 和文件名<br>
     * - 索引：(parentType, parentId)
     * - 查询：[MediaInfo(id, filename)]
     */
    default LambdaQueryWrapper<MediaFile> queryFile(ParentType parentType, Long parentId) {
        return lambdaQuery()
                .eq(MediaFile::getParentType, parentType)
                .eq(MediaFile::getParentId, parentId)
                .select(MediaFile::getId, MediaFile::getFilename);
    }

    /**
     * 查找封面文件名<br>
     * - 索引：(parentType, parentId, isCover, type)
     * - 查询：[MediaInfo(parentId, filename)]
     */
    default LambdaQueryWrapper<MediaFile> queryCover(ParentType parentType, Long parentId) {
        return lambdaQuery()
                .eq(MediaFile::getParentType, parentType)
                .eq(MediaFile::getParentId, parentId)
                .eq(MediaFile::getIsCover, true)
                .eq(MediaFile::getType, IMAGE)
                .select(MediaFile::getParentId, MediaFile::getFilename);
    }

    /**
     * 批量查找封面文件名<br>
     * - 索引：(parentType, parentId, isCover, type)<br>
     * - 查询：[MediaInfo(parentId, filename)]
     */
    default LambdaQueryWrapper<MediaFile> queryCovers(ParentType parentType, Collection<Long> parentId) {
        return lambdaQuery()
                .eq(MediaFile::getParentType, parentType)
                .in(MediaFile::getParentId, parentId)
                .eq(MediaFile::getIsCover, true)
                .eq(MediaFile::getType, IMAGE)
                .select(MediaFile::getParentId, MediaFile::getFilename);
    }

    /**
     * 查找封面，排除指定图片<br>
     * - 索引：(parentType, parentId, isCover, type)<br>
     * - 查询：[MediaInfo]
     */
    default LambdaQueryWrapper<MediaFile> queryCover(ParentType parentType, Long parentId, Long exceptImageId) {
        return lambdaQuery()
                .eq(MediaFile::getParentType, parentType)
                .eq(MediaFile::getParentId, parentId)
                .eq(MediaFile::getIsCover, true)
                .eq(MediaFile::getType, IMAGE)
                .ne(MediaFile::getId, exceptImageId);
    }

    /**
     * 清除封面<br>
     * - 索引：(parentType, parentId, isCover, type)<br>
     * - 更新：[MediaInfo] isCover=false
     */
    default LambdaUpdateWrapper<MediaFile> clearCover(ParentType parentType, Long parentId) {
        return lambdaUpdate()
                .eq(MediaFile::getParentType, parentType)
                .eq(MediaFile::getParentId, parentId)
                .eq(MediaFile::getIsCover, true)
                .eq(MediaFile::getType, IMAGE)
                .set(MediaFile::getIsCover, false);
    }

    /**
     * 查询最新图片<br>
     * - 索引：(parentType, parentId, type, createTime)<br>
     * - 查询：[MediaInfo(id)]
     */
    default LambdaQueryWrapper<MediaFile> queryLatestImageId(ParentType parentType, Long parentId) {
        return lambdaQuery()
                .eq(MediaFile::getParentType, parentType)
                .eq(MediaFile::getParentId, parentId)
                .eq(MediaFile::getType, IMAGE)
                .orderByDesc(MediaFile::getCreateTime)
                .select(MediaFile::getId);
    }

    /**
     * 查询最新图片，排除指定图片<br>
     * - 索引：(parentType, parentId, type, createTime)<br>
     * - 查询：[MediaInfo(id)]
     */
    default LambdaQueryWrapper<MediaFile> queryLatestImageId(ParentType parentType, Long parentId, Long exceptImageId) {
        return lambdaQuery()
                .eq(MediaFile::getParentType, parentType)
                .eq(MediaFile::getParentId, parentId)
                .eq(MediaFile::getType, IMAGE)
                .ne(MediaFile::getId, exceptImageId)
                .orderByDesc(MediaFile::getCreateTime)
                .select(MediaFile::getId);
    }

    /**
     * 查询媒体<br>
     * - 索引：(parentType, parentId)<br>
     * - 查询：[MediaInfo]
     */
    default LambdaQueryWrapper<MediaFile> queryByParents(ParentType parentType, Set<Long> parentIds) {
        return lambdaQuery()
                .eq(MediaFile::getParentType, parentType)
                .in(MediaFile::getParentId, parentIds);
    }

    @Override
    default String getMissingMessage() {
        return "图片/视频不存在";
    }
}
