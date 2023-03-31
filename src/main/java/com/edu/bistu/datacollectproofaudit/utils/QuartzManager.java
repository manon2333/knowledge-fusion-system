package com.edu.bistu.datacollectproofaudit.utils;

import com.edu.bistu.datacollectproofaudit.config.DbConfig;
import com.edu.bistu.datacollectproofaudit.mapper.UnstructureDataMapper;
import com.edu.bistu.datacollectproofaudit.pojo.UnstructureData;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository
public class QuartzManager {
    private static final Logger log = LoggerFactory.getLogger(QuartzManager.class);

    private final SchedulerFactory schedulerFactory = new StdSchedulerFactory();

    private final Scheduler scheduler = schedulerFactory.getScheduler();

    private final UnstructureDataMapper unstructureDataMapper;

    private final DbConfig dbConfig;

    public QuartzManager(@Autowired UnstructureDataMapper unstructureDataMapper,
                         @Autowired DbConfig dbConfig) throws SchedulerException {
        this.unstructureDataMapper = unstructureDataMapper;
        this.dbConfig = dbConfig;
    }

    /**
     * 添加一个定时任务
     * @param id 数据源id
     */
    public void addJob(int id) {
        try {
            //查看同步周期
            int syncCycle = unstructureDataMapper.getSyncCycle(id);
            UnstructureData unstructureData=unstructureDataMapper.selectUnstructDataDatasourceById(id);
            //根据id更新同步列表
            if(unstructureData==null){
                log.info("根据ID查询数据源失败！");
                return;
            }
            String synchronizetable = unstructureData.getSyncTable();
            if (synchronizetable==null || synchronizetable.equals("")){
                log.info("同步表不存在！");
                return;
            }
//            //把Date格式的数据转换成（yyyy-MM-dd HH:mm:ss）格式的字符串
//            String syncPosition = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(unstructureData.getSynchronizePosition());
            // 任务名，任务组，任务执行类
            JobDetail jobDetail = JobBuilder.newJob(SyncJob.class)
                    .withIdentity("job" + id, "jobGroup" + id)
                    .usingJobData("tableName", unstructureData.getTablename())
                    .usingJobData("syncListStr", unstructureData.getSynList())
                    .usingJobData("syncTable", unstructureData.getSyncTable())
//                    .usingJobData("syncPosition", syncPosition)
                    .usingJobData("host", unstructureData.getHost())
                    .usingJobData("port", unstructureData.getPort())
                    .usingJobData("dbName", unstructureData.getDbname())
                    .usingJobData("dbType", String.valueOf(unstructureData.getDbtype()))
                    .usingJobData("userName", unstructureData.getUsername())
                    .usingJobData("password", unstructureData.getPassword())
                    .usingJobData("id", unstructureData.getId())
                    .usingJobData("localUrl", dbConfig.getUrl())
                    .usingJobData("localUserName", dbConfig.getUserName())
                    .usingJobData("localPassword", dbConfig.getPassword())
                    .build();            // 触发器
            TriggerBuilder<Trigger> triggerBuilder =   TriggerBuilder.newTrigger();
            // 触发器名,触发器组
            triggerBuilder.withIdentity("trigger" + id, "triggerGroup" + id);
            triggerBuilder.startNow();
            // 触发器时间设定
            triggerBuilder.withSchedule(SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInMinutes(syncCycle)//每隔*分钟执行一次
                    .repeatForever());//一直执行
            // 创建Trigger对象
            Trigger trigger = triggerBuilder.build();
            // 调度容器设置JobDetail和Trigger
            scheduler.scheduleJob(jobDetail, trigger);
            // 启动
            if (!scheduler.isShutdown()) {
                scheduler.start();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 修改一个任务的触发时间
     * @param id 数据源id
     */
    public void modifyJobTime(int id) {
        try {
            //查看同步周期
            int syncCycle = unstructureDataMapper.getSyncCycle(id);
            TriggerKey triggerKey = TriggerKey.triggerKey("trigger" + id, "triggerGroup" + id);
            Trigger trigger = scheduler.getTrigger(triggerKey);
            if (trigger == null) {
                return;
            }
            // 调用 rescheduleJob 开始
            // 触发器
            TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
            // 触发器名,触发器组
            triggerBuilder.withIdentity("trigger" + id, "triggerGroup" + id);
            triggerBuilder.startNow();
            // 触发器时间设定
            triggerBuilder.withSchedule(SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInMinutes(syncCycle)//每隔*分钟执行一次
                    .repeatForever());//一直执行
            // 创建Trigger对象
            trigger = triggerBuilder.build();
            // 修改任务的触发时间
            scheduler.rescheduleJob(triggerKey, trigger);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 移除一个任务
     * @param id 数据源id
     */
    public void removeJob(int id) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey("trigger" + id, "triggerGroup" + id);
            scheduler.pauseTrigger(triggerKey);// 停止触发器
            scheduler.unscheduleJob(triggerKey);// 移除触发器
            scheduler.deleteJob(JobKey.jobKey("job" + id, "jobGroup" + id));// 删除任务
            log.info("id为[{}]的数据暂停同步成功", id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 启动所有定时任务
     */
    public void startJobs() {
        try {
            scheduler.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 关闭所有定时任务
     */
    public void shutDownJobs() {
        try {
            if (!scheduler.isShutdown()) {
                scheduler.shutdown();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



}