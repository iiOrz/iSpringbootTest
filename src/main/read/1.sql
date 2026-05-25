-- ============================================
-- 表1：标签识别打印流程主表
-- ============================================
CREATE TABLE label_print_process_flow (
                                          id BIGINT IDENTITY(1,1) NOT NULL,
                                          flow_no VARCHAR(30) NOT NULL,
                                          type TINYINT NOT NULL,
                                          part_no NVARCHAR(100) NULL,
                                          origin_part_no NVARCHAR(100) NULL,
                                          uaeess_part_no NVARCHAR(100) NULL,
                                          status TINYINT NOT NULL DEFAULT 0,
                                          start_time DATETIME2(0) NOT NULL,
                                          end_time DATETIME2(0) NULL,
                                          operator NVARCHAR(64) NULL,
                                          created_at DATETIME2(0) NOT NULL DEFAULT GETDATE(),
                                          updated_at DATETIME2(0) NOT NULL DEFAULT GETDATE(),
                                          CONSTRAINT PK_label_print_process_flow PRIMARY KEY CLUSTERED (id)
);
GO

-- 主流程表扩展属性（表和列注释）
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'标签识别打印流程主表', @level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'label_print_process_flow';
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'主键ID', @level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'label_print_process_flow', @level2type=N'COLUMN', @level2name=N'id';
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'流程编号，业务唯一标识：{yyyyMMddHHmmss}{毫秒3位}{13位随机数}', @level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'label_print_process_flow', @level2type=N'COLUMN', @level2name=N'flow_no';
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'流程分类：1-主数据导入；2-摄像头识别；3-扫描枪识别', @level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'label_print_process_flow', @level2type=N'COLUMN', @level2name=N'type';
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'(CAT)料号', @level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'label_print_process_flow', @level2type=N'COLUMN', @level2name=N'part_no';
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'原厂料号', @level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'label_print_process_flow', @level2type=N'COLUMN', @level2name=N'origin_part_no';
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'最终生成的uaeess料号（成功后写入，失败则为NULL）', @level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'label_print_process_flow', @level2type=N'COLUMN', @level2name=N'uaeess_part_no';
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'流程整体状态：0-进行中，1-成功，2-失败', @level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'label_print_process_flow', @level2type=N'COLUMN', @level2name=N'status';
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'流程开始时间', @level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'label_print_process_flow', @level2type=N'COLUMN', @level2name=N'start_time';
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'流程结束时间', @level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'label_print_process_flow', @level2type=N'COLUMN', @level2name=N'end_time';
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'操作员', @level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'label_print_process_flow', @level2type=N'COLUMN', @level2name=N'operator';
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'记录创建时间', @level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'label_print_process_flow', @level2type=N'COLUMN', @level2name=N'created_at';
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'记录更新时间', @level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'label_print_process_flow', @level2type=N'COLUMN', @level2name=N'updated_at';
GO

-- 索引
CREATE UNIQUE NONCLUSTERED INDEX uk_flow_no ON label_print_process_flow(flow_no);
CREATE NONCLUSTERED INDEX idx_uaeess_part_no ON label_print_process_flow(uaeess_part_no);
CREATE NONCLUSTERED INDEX idx_type ON label_print_process_flow(type);
CREATE NONCLUSTERED INDEX idx_status ON label_print_process_flow(status);
GO


-- ============================================
-- 表2：标签识别打印步骤日志表
-- ============================================
CREATE TABLE label_print_step_log (
                                      id BIGINT IDENTITY(1,1) NOT NULL,
                                      flow_id BIGINT NOT NULL,
                                      step TINYINT NOT NULL,
                                      status TINYINT NOT NULL DEFAULT 0,
                                      input_data VARCHAR(3000) NULL,
                                      output_data VARCHAR(3000) NULL,
                                      error_message VARCHAR(3000) NULL,
                                      log_time DATETIME2(0) NOT NULL DEFAULT GETDATE(),
                                      CONSTRAINT PK_label_print_step_log PRIMARY KEY CLUSTERED (id)
);
GO

-- 步骤日志表扩展属性（表和列注释）
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'标签识别打印步骤日志表', @level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'label_print_step_log';
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'主键ID', @level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'label_print_step_log', @level2type=N'COLUMN', @level2name=N'id';
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'关联流程主表ID', @level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'label_print_step_log', @level2type=N'COLUMN', @level2name=N'flow_id';
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'当前步骤：1-上传主数据；2-上传图片；3-识别图片；4-匹配数据；5-打印标签', @level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'label_print_step_log', @level2type=N'COLUMN', @level2name=N'step';
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'步骤状态：0-待执行，1-成功，2-失败', @level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'label_print_step_log', @level2type=N'COLUMN', @level2name=N'status';
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'步骤输入数据', @level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'label_print_step_log', @level2type=N'COLUMN', @level2name=N'input_data';
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'步骤输出数据', @level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'label_print_step_log', @level2type=N'COLUMN', @level2name=N'output_data';
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'失败时的错误信息', @level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'label_print_step_log', @level2type=N'COLUMN', @level2name=N'error_message';
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'记录创建时间', @level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'label_print_step_log', @level2type=N'COLUMN', @level2name=N'log_time';
GO

-- 索引
CREATE NONCLUSTERED INDEX idx_flow_id ON label_print_step_log(flow_id);
GO