package com.example.backend.entity;

import com.example.backend.entity.property.ActionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteJob implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 文件路径
     * *非空 varchar(255)*
     */
    private String path;

    /**
     * 任务状态
     * *非空 varchar(20)*
     */
    private ActionStatus status;

    /**
     * 创建时间
     * *非空 timestamp*
     */
    private Date createTime;

    /**
     * 开始时间
     * *非空 timestamp*
     */
    private Date startTime;

    /**
     * 结束时间
     * *非空 timestamp*
     */
    private Date finishTime;

    public static DeleteJob create(String path) {
        return new DeleteJob(null,
                path,
                ActionStatus.WAITING,
                new Date(),
                null,
                null);
    }

    public static DeleteJob create(Path path) {
        return create(path.toString());
    }
}
