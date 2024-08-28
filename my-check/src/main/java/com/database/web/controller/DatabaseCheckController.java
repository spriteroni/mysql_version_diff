package com.database.web.controller;

import com.alibaba.fastjson.JSON;
import com.database.common.annotation.Log;
import com.database.common.core.controller.BaseController;
import com.database.common.core.domain.AjaxResult;

import com.database.common.utils.DateUtils;
import com.database.common.utils.StringUtils;
import com.database.web.entity.viewmodel.*;
import com.database.common.enums.BusinessType;
import com.database.web.service.DatabaseCheckService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.List;
import java.util.stream.IntStream;


/**
 * dbcheck 数据库检查控制器
 */
@RestController
@RequestMapping("dbcheck")
public class DatabaseCheckController extends BaseController {

    /**
     * 服务对象
     */
    @Resource
    private DatabaseCheckService databaseCheckService;


    /**
     * 数据库检查
     *
     * @param userDatabases
     * @return
     */
    @Log(title = "MySQL连接和版本检查", businessType = BusinessType.OTHER)
    @ResponseBody
    @PostMapping ("/cv")
    public AjaxResult checkVersion(@Validated UserDatabasesModel userDatabases) {
            DBMessage dbMessage = databaseCheckService.checkMysqlVersion(userDatabases);
            if (dbMessage.getSqlState().equals("000000")) {
                return AjaxResult.success(dbMessage);
            }
            return AjaxResult.error(dbMessage);
    }

    /**
     * 获取指定数据库服务的所有数据库
     *
     * @param userDatabases
     * @return
     */
    @Log(title = "获取所有数据库", businessType = BusinessType.OTHER)
    @ResponseBody
    @PostMapping ("/getdbs")
    public AjaxResult getAllDb(@Validated UserDatabasesModel userDatabases) {
        DBMessage dbMessage = databaseCheckService.getDatabases(userDatabases);
        if (dbMessage.getSqlState().equals("000000")) {
            return AjaxResult.success(dbMessage);
        }
        return AjaxResult.error(dbMessage);
    }

    /**
     * 比较版本号
     * @param sourceVersion
     * @param targetVersion
     * @return
     */
    @Log(title = "比较版本", businessType = BusinessType.OTHER)
    @ResponseBody
    @PostMapping ("/compare")
    public AjaxResult compareVersion(String sourceVersion,String targetVersion) {
        DBMessage dbMessage = databaseCheckService.compareMysqlVersion(sourceVersion, targetVersion);
        if (dbMessage.getSqlState().equals("000000")) {
            return AjaxResult.success(dbMessage);
        }
        return AjaxResult.error(dbMessage);
    }


    @ResponseBody
    @PostMapping ("/checkall")
    public AjaxResult checkAllItems(@RequestBody DataJsonModel jsonData) {
        List<DBMessage> items = databaseCheckService.checkInAll(jsonData.getSourceDB(), jsonData.getTargetDB());
        if(items.size() == 1){
         return items.get(0).getSqlState().equals("000000") ? AjaxResult.success(items) : AjaxResult.error(items);
        }
        return AjaxResult.success(items);
    }




    @ResponseBody
    @PostMapping ("/export")
    public ResponseEntity<byte[]> exportFile(@RequestBody String postData) {
        try(Workbook workbook = new XSSFWorkbook();ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("数据库版本检查");
            // 创建一个字体对象
            Font font = workbook.createFont();
            font.setBold(true); // 设置字体加粗
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);

            Row headRow = sheet.createRow(0);
            Cell cell = headRow.createCell(0);
            cell.setCellValue("数据库版本兼容性检查");
            cell.setCellStyle(cellStyle);

            sheet.setColumnWidth(0, 50*256);
            sheet.setColumnWidth(1, 70*256);
            sheet.setColumnWidth(3, 80*256);
            CellRangeAddress mergedRegion= new CellRangeAddress(0, 0, 0, 4);
            sheet.addMergedRegion(mergedRegion);
            Row row_column = sheet.createRow(1);
            Cell cell1=row_column.createCell(0);
            cell1.setCellStyle(cellStyle);
            Cell cell2=row_column.createCell(1);
            cell2.setCellStyle(cellStyle);
            Cell cell3=row_column.createCell(2);
            cell3.setCellStyle(cellStyle);
            Cell cell4=row_column.createCell(3);
            cell4.setCellStyle(cellStyle);


            cell1.setCellValue("检查项");
            cell2.setCellValue("检查内容");
            cell3.setCellValue("结果");
            cell4.setCellValue("详情");

            List<CheckModel> items = JSON.parseArray(postData, CheckModel.class);

            for (int i = 0; i < items.size(); i++){
                CheckModel item = items.get(i);
                List<DBKVModel> kvModels =  item.getMsgList();
                Row row_item=sheet.createRow(i+2);
                Cell cell_one=row_item.createCell(0);
                Cell cell_two=row_item.createCell(1);
                Cell cell_three=row_item.createCell(2);
                Cell cell_four=row_item.createCell(3);
                cell_one.setCellValue(item.getCheckCode());
                cell_two.setCellValue(item.getCheckContent());
                if(item.getSqlState().equals("000000")){
                    cell_three.setCellValue("通过");
                    cell_four.setCellValue(item.getMessage());
                }else{
                    cell_three.setCellValue("不通过");
                    if(item.getKeyStr().equals("WHOLE_PARAMETER_FIT")){
                        cell_four.setCellValue("请查看《附录1》");
                        Sheet sheet_paramter = workbook.createSheet("附录1");
                        sheet_paramter.setColumnWidth(0, 100*256);
                        sheet_paramter.setColumnWidth(1, 100*256);

                        try {
                            Row row_title = sheet_paramter.createRow(0);
                            Cell title_one=row_title.createCell(0);
                            Cell title_two=row_title.createCell(1);
                            title_one.setCellStyle(cellStyle);
                            title_two.setCellStyle(cellStyle);
                            title_one.setCellValue("源库");
                            title_two.setCellValue("目标库");
                            IntStream.range(0, kvModels.size()).forEach(j -> {
                                Row row_paramter=sheet_paramter.createRow(j+1);
                                row_paramter.createCell(0).setCellValue(kvModels.get(j).getSourceDBMsg());
                                row_paramter.createCell(1).setCellValue(kvModels.get(j).getTargetDBMsg());
                            });
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }

                    }else {
                        StringBuilder sb = new StringBuilder();
                        sb.append(item.getMessage());
                        sb.append("\n");
                        kvModels.forEach(k -> {
                            sb.append("源数据库：");
                            sb.append(k.getSourceDBMsg());
                            sb.append(" ");
                            sb.append("目标数据库：");
                            sb.append(k.getTargetDBMsg());
                            sb.append("\n");
                        });
                        cell_four.setCellValue(sb.toString());
                    }
                }
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", StringUtils.format("{}" + ".xlsx", DateUtils.dateTimeNow()));

            workbook.write(outputStream);
            return new ResponseEntity<>(outputStream.toByteArray(),headers,200);
        }
        catch (IOException e ) {
            throw new RuntimeException("导出Excel失败", e);
        }
    }
}
