package com.example.backend.mapper;

import com.example.backend.entity.DeleteJob;
import com.example.backend.entity.property.ActionStatus;
import com.example.backend.util.MPLambdaQuery;
import com.example.backend.util.MPLambdaUpdate;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;

/**
 * 索引：
 * - (status, createTime)
 */
@Mapper
public interface DeleteJobMapper extends IBaseMapper<DeleteJob> {

    default MPLambdaQuery<DeleteJob> query(int count, ActionStatus status) {
        return new MPLambdaQuery<>(this)
                .eq(DeleteJob::getStatus, status)
                .desc(DeleteJob::getCreateTime)
                .limit(count);
    }

    default MPLambdaUpdate<DeleteJob> start(DeleteJob job) {
        return new MPLambdaUpdate<>(this)
                .eq(DeleteJob::getId, job.getId())
                .set(DeleteJob::getStartTime, new Date())
                .set(DeleteJob::getStatus, ActionStatus.WORKING);
    }

    default MPLambdaUpdate<DeleteJob> success(DeleteJob job) {
        return new MPLambdaUpdate<>(this)
                .eq(DeleteJob::getId, job.getId())
                .set(DeleteJob::getFinishTime, new Date())
                .set(DeleteJob::getStatus, ActionStatus.SUCCESS);
    }

    default MPLambdaUpdate<DeleteJob> fail(DeleteJob job) {
        return new MPLambdaUpdate<>(this)
                .eq(DeleteJob::getId, job.getId())
                .set(DeleteJob::getFinishTime, new Date())
                .set(DeleteJob::getStatus, ActionStatus.FAILED);
    }

    @Override
    default Class<DeleteJob> getEntityClass() {
        return DeleteJob.class;
    }
}
